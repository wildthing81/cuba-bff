/*
 * The code is copyright ©2021
 */

package com.foo.credible.websocket

import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpointConfig
import javax.websocket.server.ServerEndpointConfig.Configurator
import com.anzi.credible.config.AppConfig
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.exceptions.CrdWebSocketException
import com.anzi.credible.utils.AppUtils.isNotNullAndEmpty
import com.haulmont.addon.restapi.rest.ServerTokenStore
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Configuration
import com.haulmont.cuba.security.app.UserSessionsAPI

open class SessionConfigurator : Configurator() {

    /**
     * Intercept Websocket handshake request to verify Oauth access token &
     * fetch User details
     *
     * @param config
     * @param request
     * @param response
     *
     */
    override fun modifyHandshake(config: ServerEndpointConfig, request: HandshakeRequest, response: HandshakeResponse) {
        val tokenStore = AppBeans.get(ServerTokenStore::class.java)
        request.parameterMap["token"]?.get(0)?.let {
            if (tokenStore.getAccessTokenByTokenValue(it).isNotNullAndEmpty()) {
                tokenStore.getSessionInfoByTokenValue(it)
            } else throw CrdWebSocketException("Invalid authentication token")
        }!!.let {
            val userSession = AppBeans.get(UserSessionsAPI::class.java).get(it.id)
            config.userProperties[AppConstants.USER_SESSION] = userSession!!
        }
    }

    /**
     * Origin check for Websocket connection
     *
     * @param originHeaderValue
     */
    override fun checkOrigin(originHeaderValue: String?): Boolean {
        // return super.checkOrigin(originHeaderValue)
        return AppBeans.get(Configuration::class.java)
            .getConfig(AppConfig::class.java).wsOrigins.split(",").contains(originHeaderValue)
    }
}
