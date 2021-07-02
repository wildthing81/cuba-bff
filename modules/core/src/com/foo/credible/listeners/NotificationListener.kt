/*
 * The code is copyright ©2021
 */

package com.foo.credible.listeners

import java.util.UUID
import org.springframework.stereotype.Component
import com.anzi.credible.entity.Notification
import com.haulmont.cuba.core.app.events.EntityChangedEvent
import mu.KotlinLogging

@Component("NotificationListener")
open class NotificationListener : StandardEntityListener<Notification> {
    private val log = KotlinLogging.logger { }

    override fun beforeEntityCommit(event: EntityChangedEvent<Notification, UUID>) {
        log.debug { "Before Notification entity commit" }
    }

    override fun afterEntityCommit(event: EntityChangedEvent<Notification, UUID>) {
        log.debug { "After Notification entity commit" }
    }
}
