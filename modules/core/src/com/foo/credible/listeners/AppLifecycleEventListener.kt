/*
 * The code is copyright ©2021
 */
package com.foo.credible.listeners

import javax.inject.Inject
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import com.anzi.credible.config.AppConfig
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.AppConstants.OCP_REST_ENABLED
import com.anzi.credible.constants.AppConstants.PROFILE_DEV
import com.anzi.credible.constants.AppConstants.PROFILE_STAG
import com.anzi.credible.constants.CKEditorConstants.ENVIRONMENT_ID
import com.anzi.credible.constants.CKEditorConstants.SECRET
import com.foo.credible.core.role.CredibleUIRole
import com.anzi.credible.service.UserService
import com.anzi.credible.utils.AppUtils.nullNotEqual
import com.anzi.credible.utils.AppUtils.then
import com.haulmont.cuba.core.app.ConfigStorage
import com.haulmont.cuba.core.sys.events.AppContextInitializedEvent
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent
import com.haulmont.cuba.security.role.RolesService
import mu.KotlinLogging

@Component("crd_AppLifecycleEventListener")
open class AppLifecycleEventListener {

    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var rolesService: RolesService

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var appConfig: AppConfig

    @Inject
    private lateinit var configStorage: ConfigStorage

    @EventListener
    open fun applicationContextInitialized(event: AppContextInitializedEvent) {
        log.info { "AppContextInitializedEvent" }
    }

    @EventListener
    open fun applicationContextStarted(event: AppContextStartedEvent) {
        log.info { "AppContextStartedEvent" }
    }

    @EventListener
    open fun servletContextInitialized(event: ServletContextInitializedEvent) {
        log.info { "ServletContextInitializedEvent" }
        userService.fetchUsersByLogin(listOf(AppConstants.ADMIN)).forEach { user ->
            user.userRoles
                .none { it.roleName == com.foo.credible.core.role.CredibleUIRole.NAME }.then {
                    userService.assignRoleToUser(
                        rolesService.getRoleDefinitionAndTransformToRole(com.foo.credible.core.role.CredibleUIRole.NAME),
                        user
                    )
                }
        }

        setHotReloadProperties()
    }

    private fun setHotReloadProperties() {
        if (appConfig.ocpRestEnabled.isNullOrEmpty()) {
            configStorage.setDbProperty(OCP_REST_ENABLED, "true")
        }
        when (appConfig.profile) {
            // When this value is changed, you need to update on condition as well as setDbProperty line
            PROFILE_DEV -> {
                "dev".nullNotEqual(appConfig.ckEditorEnvironmentId).then {
                    configStorage.setDbProperty(ENVIRONMENT_ID, "dev")
                }
                "secret".nullNotEqual(appConfig.ckEditorSecret).then {
                    configStorage.setDbProperty(SECRET, "secret")
                }
            }
            PROFILE_STAG -> {
                "stag".nullNotEqual(appConfig.ckEditorEnvironmentId).then {
                    configStorage.setDbProperty(ENVIRONMENT_ID, "stag")
                }
                "secret".nullNotEqual(appConfig.ckEditorSecret).then {
                    configStorage.setDbProperty(SECRET, "secret")
                }
            }
            else -> {
                "local".nullNotEqual(appConfig.ckEditorEnvironmentId).then {
                    configStorage.setDbProperty(ENVIRONMENT_ID, "local")
                }
                "secret".nullNotEqual(appConfig.ckEditorSecret).then {
                    configStorage.setDbProperty(SECRET, "secret")
                }
            }
        }
    }
}
