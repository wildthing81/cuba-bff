/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import java.util.Date
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Notification
import com.anzi.credible.entity.Subscriber

interface NotificationRepository {

    fun createNotification(notificationDto: NotificationDto, institution: Institution, users: List<String>):
        Notification

    fun fetchNotificationByTimeFrame(
        user: AppUser,
        startAt: Date,
        endAt: Date
    ): List<Notification>

    fun fetchNotificationById(notificationId: String, viewName: String): Notification

    fun updateNotification(notification: Notification): Notification

    fun updateSubscriber(subscriber: Subscriber): Subscriber

    fun markAllAsRead(user: AppUser): Any
}
