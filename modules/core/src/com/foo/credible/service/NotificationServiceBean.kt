/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.FluxActionConstants
import com.anzi.credible.constants.FluxActionConstants.ID
import com.anzi.credible.constants.FluxActionConstants.NAME
import com.anzi.credible.constants.ViewConstants.NOTIFICATION_SUBSCRIBER_FETCH
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.FluxActionDto
import com.anzi.credible.dto.KeyValueDto
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.dto.NotificationSession
import com.anzi.credible.dto.SubscriberDto
import com.anzi.credible.entity.Notification
import com.anzi.credible.helpers.NotificationHelper
import com.anzi.credible.repository.NotificationRepository
import com.anzi.credible.utils.DateUtils.formatTo
import com.anzi.credible.utils.DateUtils.toUTC
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging

@Service(NotificationService.NAME)
open class NotificationServiceBean : NotificationService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var notificationRepository: NotificationRepository

    /**
     * Create a notification record in database
     * @param notificationDto
     */
    override fun createNotification(notificationDto: NotificationDto, team: List<String>) = try {
        log.info { "Creating a new notification" }
        val institution = userService.fetchLoggedUser().institution!!

        notificationRepository.createNotification(notificationDto, institution, team)
    } catch (dbe: Exception) {
        log.error(dbe) { "Error creating new notification" }
        ErrorDto(Notification::class.simpleName!!, null, ErrorConstants.CREATE_ENTITY, dbe.message)
    }

    /**
     * Notify a user login to other connected users
     * @param session
     */
    override fun notifyUserConnect(session: NotificationSession) =
        pushNotification(session, FluxActionConstants.USER_CONNECTED)

    /**
     * Notify a user logout to other connected users
     * @param session
     */
    override fun notifyUserDisconnect(session: NotificationSession) = pushNotification(
        session,
        FluxActionConstants.USER_DISCONNECTED
    )

    /**
     * Push created notification if user is connected via websocket
     * @param session
     * @param actionType
     */
    override fun pushNotification(session: NotificationSession, actionType: String) {
        val message = ObjectMapper().registerKotlinModule().writeValueAsString(
            FluxActionDto(
                actionType,
                listOf(
                    KeyValueDto(ID, session.user.id.toString()),
                    KeyValueDto(NAME, session.user.name)
                )
            )
        )

        NotificationHelper.pushAll(message, session)
    }

    /**
     * Fetches all notifications for the user
     *
     * @param startAt
     * @param endAt
     * @return
     */
    override fun fetchNotifications(startAt: String?, endAt: String?, pathVar: String?): Any? = try {
        val loggedUser = userService.fetchLoggedUser()
        val startDate = startAt?.toUTC()
            ?: if (loggedUser.lastNotifiedAt != null) {
                loggedUser.lastNotifiedAt
            } else {
                loggedUser.createTs
            }
        var endDate: Date = LocalDateTime.now().formatTo().toUTC()
        if (endAt != null) {
            endDate = endAt.toUTC()
        }

        log.info { "Fetching notifications for ${loggedUser.id} between $startAt and $endAt" }

        startDate?.let { sDate ->
            notificationRepository.fetchNotificationByTimeFrame(loggedUser, sDate, endDate).let { notifications ->
                if (pathVar.isNullOrEmpty()) {
                    userService.updateUser(loggedUser.id.toString())
                    notifications
                        .map { notification ->
                            NotificationHelper.buildDto(notification, loggedUser)
                        }
                } else notifications.size
            }
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error fetching notifications" }
        ErrorDto(Notification::class.simpleName!!, null, ErrorConstants.FIND_ENTITY, dbe.message)
    }

    override fun markAllAsRead(): Any? = try {
        val loggedUser = userService.fetchLoggedUser()
        log.info { "Updating all user notifications to read: $loggedUser.id" }
        notificationRepository.markAllAsRead(loggedUser)
    } catch (dbe: Exception) {
        log.error(dbe) { "Error updating user notifications" }
        ErrorDto(Notification::class.simpleName!!, null, ErrorConstants.UPDATE_ENTITY, dbe.message)
    }

    /**
     * Updates notification subscribers details like isRead and isHidden flags
     *
     * @param notificationId
     * @param subscriberDto
     * @return
     */
    override fun updateSubscriber(notificationId: String, subscriberDto: SubscriberDto) = try {
        val loggedUser = userService.fetchLoggedUser()
        log.info { "Updating notification: $notificationId" }
        notificationRepository.fetchNotificationById(notificationId, NOTIFICATION_SUBSCRIBER_FETCH)
            .let { notification ->
                notification.subscribers.find { subscriber -> loggedUser.id == subscriber.user?.id }?.let {
                    it.isHidden = subscriberDto.isHidden
                    it.isRead = subscriberDto.isRead
                    notificationRepository.updateSubscriber(it)
                }
                notification
                // val subscriber = subscriberRepository.fetchSubscriber(userService.fetchLoggedUser(), notification)[0]
            }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error updating notification: $notificationId" }
        ErrorDto(
            Notification::class.simpleName!!,
            notificationId,
            ErrorConstants.UPDATE_ENTITY,
            dbe.message
        )
    }
}
