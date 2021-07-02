/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import javax.inject.Inject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import com.anzi.credible.constants.AppConstants.COUNT
import com.anzi.credible.constants.AppConstants.MARK_ALL_READ
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.dto.SubscriberDto
import com.anzi.credible.service.NotificationService

@RestController
class NotificationController {
    @Inject
    private lateinit var notificationService: NotificationService

    @GetMapping(value = ["/notifications/{pathVar}", "/notifications"])
    fun fetchNotifications(
        @RequestParam(required = false) startAt: String?,
        @RequestParam(required = false) endAt: String?,
        @PathVariable(required = false) pathVar: String?
    ): ResponseEntity<out Any> {
        if (pathVar != null && pathVar != COUNT) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        val response = notificationService.fetchNotifications(startAt, endAt, pathVar)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(if (response is Int) response else response as List<*>, HttpStatus.OK)
    }

    @PatchMapping("/notification/{notificationId}")
    fun updateNotification(@PathVariable notificationId: String, @RequestBody subscriberDto: SubscriberDto):
        ResponseEntity<out Any> {
            val response = notificationService.updateSubscriber(notificationId, subscriberDto)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(HttpStatus.NO_CONTENT)
        }

    @PatchMapping("/notifications/{pathVar}")
    fun updateAllNotifications(@PathVariable(required = false) pathVar: String):
        ResponseEntity<out Any> {
            if (pathVar != MARK_ALL_READ) {
                return ResponseEntity(HttpStatus.NOT_FOUND)
            }
            val response = notificationService.markAllAsRead()

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(HttpStatus.NO_CONTENT)
        }

    @PostMapping("/notification")
    fun createNotification(@RequestBody notificationDto: NotificationDto):
        ResponseEntity<out Any> {
            // Notification subscribers empty list right now.need details on request structure
            val response = notificationService.createNotification(notificationDto, emptyList())

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(HttpStatus.CREATED)
        }
}
