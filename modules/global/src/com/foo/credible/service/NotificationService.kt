/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.dto.NotificationSession
import com.anzi.credible.dto.SubscriberDto

interface NotificationService {
    companion object {
        const val NAME = "crd_NotificationService"
    }

    fun createNotification(notificationDto: NotificationDto, team: List<String>): Any?

    fun notifyUserDisconnect(session: NotificationSession)

    fun notifyUserConnect(session: NotificationSession)

    fun pushNotification(session: NotificationSession, actionType: String)

    fun updateSubscriber(notificationId: String, subscriberDto: SubscriberDto): Any

    fun fetchNotifications(startAt: String?, endAt: String?, pathVar: String?): Any?

    fun markAllAsRead(): Any?
}
