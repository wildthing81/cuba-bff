/*
 * The code is copyright ©2021
 */

package com.foo.credible.websocket

import javax.websocket.CloseReason
import javax.websocket.EndpointConfig
import javax.websocket.OnClose
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.helpers.NotificationHelper
import com.anzi.credible.service.NotificationService
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.sys.AppContext
import com.haulmont.cuba.core.sys.SecurityContext
import com.haulmont.cuba.security.auth.AnonymousSessionHolder
import com.haulmont.cuba.security.global.UserSession
import mu.KotlinLogging

@ServerEndpoint(value = "/notifications", configurator = SessionConfigurator::class)
open class NotificationsHandler {

    private val log = KotlinLogging.logger { }
    private var userSession: UserSession? = null

    /**
     * Callback method  when websocket connection is opened
     * @param wSession
     * @param config
     */
    @OnOpen
    open fun onOpen(wSession: Session, config: EndpointConfig) {
        log.info { "Open websocket connection ..." }
        userSession = config.userProperties[AppConstants.USER_SESSION] as UserSession
        val nSession = NotificationHelper.addSession(wSession, userSession!!.user)
        AppContext.withSecurityContext(
            SecurityContext(userSession!!),
            Runnable {
                val service = AppBeans.get(NotificationService::class.java)
                service.notifyUserConnect(nSession)
            }
        )
        log.info { "Websocket session ${wSession.id} created for ${userSession!!.user.login}" }
    }

    @OnMessage
    open fun onMessage(message: String): String? {
        log.info { "Message : $message" }
        return "Echo from the server : $message"
    }

    @OnError
    open fun onError(session: Session, e: Throwable) {
        log.info { "On Error" }
//        when (e) {
//           e as CrdWebSocketException -> {
//               log.error { "Unable to create websocket connection" }
//               // return error response
//           }
//        }
    }

    /**
     * Callback method when websocket connection is closed
     * @param session
     * @param reason
     */
    @OnClose
    open fun onClose(session: Session, reason: CloseReason) {
        // An anonymous oauth session is needed to enable clean removal of user's websocket
        // session since original session expires on logout
        val anonymousSession = AppBeans.get(AnonymousSessionHolder::class.java).anonymousSession
        AppContext.withSecurityContext(
            SecurityContext(anonymousSession),
            Runnable {
                AppBeans.get(NotificationService::class.java)
                    .notifyUserDisconnect(NotificationHelper.removeSession(session))
            }
        )
        log.info { "Websocket session ${session.id} closed because of $reason" }
    }
}
