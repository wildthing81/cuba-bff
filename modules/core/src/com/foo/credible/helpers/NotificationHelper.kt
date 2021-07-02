/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.websocket.Session
import com.anzi.credible.constants.NotificationConstants.PARALLEL_NOTIFY_THREADS
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.dto.NotificationSession
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Notification
import com.anzi.credible.exceptions.CrdWebSocketException
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.haulmont.cuba.security.entity.User
import mu.KotlinLogging

object NotificationHelper {

    private val log = KotlinLogging.logger { }

    // A backing field has been used instead of initializer because of
    // https://github.com/mockk/mockk/issues/202
    private var sessionsStore: ConcurrentHashMap<String, NotificationSession>? = null
        get() {
            if (field == null) {
                field = ConcurrentHashMap<String, NotificationSession>()
            }
            return field
        }

    /**
     * Wrap Tomcat websocket session into NotificationSession object and
     * add to sessions store
     *
     * @param session
     * @return NotificationSession
     */
    fun addSession(session: Session, user: User) = try {
        NotificationSession(user, session).also { sessionsStore!![session.id] = it }
    } catch (e: Exception) {
        log.error { "Error adding notification session ${session.id} to store" }
        throw CrdWebSocketException(e.message, e)
    }

    /**
     * Remove NotificationSession object from sessions store
     *
     * @param session
     * @return NotificationSession
     */
    fun removeSession(session: Session) = try {
        sessionsStore!!.remove(session.id)!!
    } catch (e: Exception) {
        log.error { "Error removing notification session ${session.id} from store" }
        throw CrdWebSocketException(e.message, e)
    }

    /**
     * Push message to targeted websocket sessions
     * @param message
     */
    fun push(message: String, userIds: List<UUID>) {
        sessionsStore?.forEachValue(PARALLEL_NOTIFY_THREADS) { value ->
            if (userIds.contains(value.user.id)) {
                value.session.basicRemote.sendText(message)
            }
        }
    }

    /**
     * Push message to all websocket sessions except originator of message
     * @param message
     */
    fun pushAll(message: String, session: NotificationSession) {
        sessionsStore?.forEachValue(PARALLEL_NOTIFY_THREADS) { value ->
            if (value != session) value.session.basicRemote.sendText(message)
        }
    }

    /**
     * Build Notification DTO of immediate attributes
     *
     * @param notification
     */
    fun buildDto(notification: Notification, user: AppUser) =
        NotificationDto(
            id = notification.id,
            text = notification.text,
            payload = notification.payload?.let { ObjectMapper().registerKotlinModule().readValue(it) },
            isRead = notification.subscribers.find { sub -> sub.user == user }?.isRead,
            isHidden = notification.subscribers.find { sub -> sub.user == user }?.isHidden,
            createdAt = notification.createTs.formatTo(),
            to = notification.subscribers.find { sub -> sub.user == user }?.user?.id
        )
}
