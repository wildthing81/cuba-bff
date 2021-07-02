package com.foo.credible.interceptors

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.HandlerInterceptor
import com.anzi.credible.config.AppConfig
import com.anzi.credible.constants.AppConstants.OCP_TOKEN_API
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.sys.AppContext
import com.haulmont.cuba.core.sys.SecurityContext
import com.haulmont.cuba.web.security.WebAnonymousSessionHolder
import mu.KotlinLogging

@Component
class OCPRequestInterceptor : HandlerInterceptor {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var appConfig: AppConfig

    @Inject
    private lateinit var ocpRestTemplate: RestTemplate

    /**
     * Validating token to authenticate request.
     *
     * @param request
     * @param response
     * @param handler
     * @return true if valid token else false
     */
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse?,
        handler: Any?
    ): Boolean {
        log.info { "preHandle: Intercepting request ${request.requestURI}" }

        if (!isOCPEnabled(request.requestURI) ||
            request.requestURI.matches(Regex("^.*?(swagger|webjars|api-docs).*$"))
        ) {
            return true
        }

        val authUrl = appConfig.ocpRestBaseUrl + OCP_TOKEN_API
        log.debug { "Validating OCP secure token at $authUrl" }

        return try {
            ObjectMapper().createObjectNode().put("token", request.getHeader("token")).let { body ->
                val authResponse = ocpRestTemplate.postForEntity(authUrl, body, String::class.java)
                HttpStatus.OK == authResponse.statusCode
            }
        } catch (e: Exception) {
            log.error(e) { "Issue in authenticating ocp token for request: ${request.requestedSessionId}" }
            throw InvalidTokenException(e.message, e)
        }
    }

    private fun isOCPEnabled(requestUri: String): Boolean {
        // an anonymous session context is needed for Properties bean access during
        // /oauth/token request
        return if (requestUri.contains("oauth")) {
            AppContext.withSecurityContext(
                SecurityContext(AppBeans.get(WebAnonymousSessionHolder::class.java).anonymousSession),
                AppContext.SecuredOperation {
                    appConfig.ocpRestEnabled.toBoolean()
                })
        } else appConfig.ocpRestEnabled.toBoolean()
    }
}
