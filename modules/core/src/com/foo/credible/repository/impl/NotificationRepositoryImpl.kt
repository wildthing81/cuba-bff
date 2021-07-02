/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import java.util.Date
import java.util.UUID
import javax.inject.Inject
import org.json.JSONArray
import org.springframework.stereotype.Repository
import com.anzi.credible.constants.ViewConstants.NOTIFICATION_SUBSCRIBER_FETCH
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Notification
import com.anzi.credible.entity.Subscriber
import com.anzi.credible.repository.NotificationRepository
import com.anzi.credible.repository.QueryConstants
import com.anzi.credible.repository.QueryConstants.MARK_ALL_USER_NOTIFICATIONS_READ
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager

@Repository
open class NotificationRepositoryImpl : NotificationRepository {
    @Inject
    private lateinit var dataManager: DataManager

    @Inject
    private lateinit var persistence: Persistence

    /**
     * Creates notification
     *
     * @param notificationDto
     * @param institution
     * @return
     */
    override fun createNotification(
        notificationDto: NotificationDto,
        institution: Institution,
        users: List<String>
    ): Notification {
        val commitContext = CommitContext()
        val notification = dataManager.create(Notification::class.java)

        commitContext.addInstanceToCommit(notification)

        notification.text = notificationDto.text
        notification.payload = JSONArray(notificationDto.payload).toString()
        notification.institution = institution
        notification.type = notificationDto.type

        addSubscribers(notification, users, commitContext)
        dataManager.commit(commitContext)

        return notification
    }

    /**
     * Fetches notifications for the date-time range
     *
     * @param user
     * @param startAt
     * @param endAt
     * @return notificationList
     */
    override fun fetchNotificationByTimeFrame(
        user: AppUser,
        startAt: Date,
        endAt: Date
    ): List<Notification> = dataManager
        .load(Notification::class.java)
        .query(QueryConstants.FETCH_USER_NOTIFICATIONS)
        .setParameters(mapOf("user" to user, "startAt" to startAt, "endAt" to endAt))
        .view(NOTIFICATION_SUBSCRIBER_FETCH).list()

    /**
     * Fetches notification by Id
     *
     * @param notificationId
     * @return
     */
    override fun fetchNotificationById(notificationId: String, viewName: String): Notification = dataManager
        .load(Notification::class.java)
        .id(UUID.fromString(notificationId)).view(viewName).one()

    /**
     * Updates notification details
     *
     * @param notification
     * @return
     */
    override fun updateNotification(notification: Notification): Notification = dataManager.commit(notification)

    override fun updateSubscriber(subscriber: Subscriber): Subscriber = dataManager.commit(subscriber)

    /**
     * Updates all notifications of the user to read
     *
     * @param user
     * @return true
     */
    override fun markAllAsRead(user: AppUser): Any {
        persistence.createTransaction().use {
            val em: EntityManager = persistence.entityManager
            em.createQuery(MARK_ALL_USER_NOTIFICATIONS_READ).setParameter("user", user).executeUpdate()
            it.commit()
            return true
        }
    }

    private fun addSubscribers(
        notification: Notification,
        users: List<String>,
        commitContext: CommitContext
    ) =
        users.map {
            val subscriber = dataManager.create(Subscriber::class.java)
            subscriber.apply {
                isRead = false
                isHidden = false
                user = dataManager.load(AppUser::class.java).id(UUID.fromString(it)).one()
                this.notification = notification
            }
            commitContext.addInstanceToCommit(subscriber)
        }
}
