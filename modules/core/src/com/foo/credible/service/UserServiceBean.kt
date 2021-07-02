/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.util.Date
import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.config.AppConfig
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.helpers.UserHelper
import com.anzi.credible.repository.UserRepository
import com.anzi.credible.utils.AppUtils.createJWTToken
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.addon.restapi.rest.ServerTokenStore
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.app.UserSessionsAPI
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.security.entity.User
import mu.KotlinLogging

@Service(UserService.NAME)
open class UserServiceBean : UserService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userSessionSource: UserSessionSource

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var serverTokenStore: ServerTokenStore

    @Inject
    private lateinit var userSessionsAPI: UserSessionsAPI

    @Inject
    private lateinit var appConfig: AppConfig

    /**
     * Creates login details of User for UI
     *
     */
    override fun userDetails() = try {
        log.info { "Create login details" }
        buildDto(fetchLoggedUser())
    } catch (dbe: Exception) {
        log.error(dbe) { "Error creating login details" }
        ErrorDto(
            User::class.simpleName!!,
            userSessionSource.userSession.user.login,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     *  This will fetch User entity
     *
     * @return AppUser
     */
    override fun fetchLoggedUser(): AppUser {
        val loggedUser = userSessionSource.userSession.user
        log.debug { "Logged in user: ${loggedUser.login}" }
        return userRepository.getUsersFromLogins(listOf(loggedUser.login))[0]
    }

    /**
     *  This will fetch User entities by login
     *
     * @return list of AppUser
     */
    override fun fetchUsersByLogin(logins: List<String>): List<AppUser> {
        log.debug { "Users: ${logins.joinToString()}" }
        return userRepository.getUsersFromLogins(logins)
    }

    /**
     *  This will fetch User entities by Id
     *
     * @return list of AppUser
     */
    override fun fetchUsersById(ids: List<String>): List<AppUser> {
        log.debug { "Users: ${ids.joinToString()}" }
        return userRepository.getUsersFromIds(ids)
    }

    /**
     * Generates jwt token based on the param
     *
     * @param accessToken
     * @return
     */
    override fun generateJWTToken(accessToken: String): Any = try {
        if (serverTokenStore.getAccessTokenByTokenValue(accessToken).isNotEmpty()) {
            serverTokenStore.getSessionInfoByTokenValue(accessToken).let {
                userSessionsAPI.get(it.id)?.user
            }?.let { user ->
                val algorithm: Algorithm = Algorithm.HMAC256(appConfig.ckEditorSecret)
                val payload = UserHelper.tokenPayload(
                    appConfig.ckEditorEnvironmentId,
                    user.login,
                    user.email,
                    user
                        .name
                )
                createJWTToken(payload, algorithm)
            }
        } else {
            log.error("Error generating token")
            ErrorDto(
                User::class.simpleName!!,
                userSessionSource.userSession.user.login,
                ErrorConstants.INVALID_ACCESS_TOKEN,
                "Invalid access token"
            )
        }
    } catch (ex: Exception) {
        log.error(ex) { "Error generating token" }
        ErrorDto(
            User::class.simpleName!!,
            userSessionSource.userSession.user.login,
            ErrorConstants.GENERATE_TOKEN,
            ex.message
        )
    }!!

    /**
     * Assign role to User
     *
     * @return User
     */
    override fun assignRoleToUser(role: Role?, user: AppUser) {
        log.info { "Assigning role:${role?.name} to user:${user.login}" }
        role?.run { userRepository.assignRoleToUser(this, user) }
    }

    /**
     * Fetch all Users for an Institution
     *
     * @return
     */
    override fun fetchInstitutionUsers() = try {
        val institution = fetchLoggedUser().institution!!
        log.info { "Fetching all users for institution: $institution" }
        userRepository.fetchInstitutionUsers(institution).filter { s ->
            !s.login.equals("anonymous", true) and
                !s.login.equals("admin", true)
        }.map {
            UserDto(
                it.id,
                name = if (it.name == null) UserHelper.userFullName(it).toString() else it.name,
                position = it.position,
                cadLevel = it.cadLevel
            )
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error finding users for institution" }
        ErrorDto(
            User::class.simpleName!!,
            "",
            ErrorConstants.FIND_USER,
            dbe.message
        )
    }

    /**
     * Create a new User
     *
     * @return New User
     */
    override fun createUser(userDto: UserDto): Any = try {
        log.info { "Creating a new user" }
        userRepository.createUser(userDto, fetchLoggedUser().institution!!).let {
            log.info { "New User ${it.id}" }
            UserDto(it.id)
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error creating new user" }
        ErrorDto(
            AppUser::class.simpleName!!,
            null,
            ErrorConstants.CREATE_ENTITY,
            dbe.message
        )
    }

    /**
     * logout user - remove user session & access tokens
     *
     * @return
     */
    override fun logout(): Any {
        val loggedUser = userSessionSource.userSession.user.login
        log.debug { "Logging out user $loggedUser" }
        return try {
            serverTokenStore.getAccessTokenValuesByUserLogin(loggedUser)
                .forEach { token -> serverTokenStore.removeAccessToken(token) }
            log.info { "$loggedUser has been logged out" }
            AppConstants.SUCCESS
        } catch (dbe: Exception) {
            log.error(dbe) { "Error logging out $loggedUser" }
            ErrorDto(
                User::class.simpleName!!,
                loggedUser,
                ErrorConstants.LOGOUT_ERROR,
                dbe.message
            )
        }
    }

    /**
     * Updates credible user details
     *
     * @param userDto
     * @return
     */
    override fun updateUser(userId: String): Any = try {
        userRepository.getUsersFromIds(listOf(userId)).let { users ->
            users[0].lastNotifiedAt = Date()
            log.info { "Updating user $userId" }
            userRepository.updateUser(users[0])
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error updating user" }
        ErrorDto(
            AppUser::class.simpleName!!,
            null,
            ErrorConstants.UPDATE_ENTITY,
            dbe.message
        )
    }

    private fun buildDto(user: AppUser) = UserDto(
        user.id,
        user.login,
        if (user.name == null) UserHelper.userFullName(user).toString() else user.name,
        user.firstName,
        user.middleName,
        user.lastName,
        user.position,
        user.email,
        user.profileImage,
        user.userRoles.filter { r -> !r.roleName.isNullOrEmpty() }.map { it -> it.roleName!! },
        user.scope,
        user.cadLevel,
        user.preferences?.let { ObjectMapper().readTree(it) }
    )
}
