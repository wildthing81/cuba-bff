/*
 * The code is copyright ©2021
 */
package com.foo.credible.repository.impl

import javax.inject.Inject
import org.springframework.stereotype.Repository
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.exceptions.CrdUserException
import com.anzi.credible.helpers.UserHelper
import com.anzi.credible.repository.QueryConstants
import com.anzi.credible.repository.UserRepository
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.TypedQuery
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.PasswordEncryption
import com.haulmont.cuba.security.entity.Group
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.security.entity.UserRole
import mu.KotlinLogging

@Repository
open class UserRepositoryImpl : UserRepository {

    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var dataManager: DataManager

    @Inject
    private val persistence: Persistence? = null

    @Inject
    private val passwordEncryption: PasswordEncryption? = null

    /**
     * associate User with new role
     *
     * @param role
     * @param user
     * @return updated User
     */
    override fun assignRoleToUser(role: Role, user: AppUser) {
        val userRole: UserRole = dataManager.create(UserRole::class.java)
        userRole.user = user
        userRole.role = role
        userRole.roleName = role.name
        dataManager.commit(CommitContext(user, role, userRole))
    }

    /**
     *  Query database to fetch all Institution users
     *
     * @param institution
     * @return user list
     */
    override fun fetchInstitutionUsers(institution: Institution): List<AppUser> = try {
        val users = dataManager.load(AppUser::class.java)
            .query(QueryConstants.FETCH_INSTITUTION_USERS)
            .parameter("institution", institution)
            .list()

        log.debug { "user list: ${users.joinToString { it.login }}" }
        users
    } catch (dbe: Exception) {
        log.error(dbe) { ErrorConstants.DB_ERROR }
        throw CrdUserException(ErrorConstants.DB_ERROR)
    }

    /**
     *
     * @return All users, registered in the system
     */
    override fun getAllSystemUsers(): List<User>? {
        try {
            val em: EntityManager = persistence!!.entityManager
            val query: TypedQuery<User> = em.createQuery("select u from sec\$User u", User::class.java)
            return query.resultList
        } catch (dbe: Exception) {
            log.error(dbe) { ErrorConstants.DB_ERROR }
            throw CrdUserException(ErrorConstants.DB_ERROR)
        }
    }

    /**
     * Get user entities for list of usernames
     *
     * @return user list
     */
    override fun getUsersFromLogins(logins: List<String>, viewName: String): List<AppUser> = try {
        val users = dataManager.load(AppUser::class.java)
            .query(QueryConstants.FETCH_USERS_BY_LOGIN)
            .parameter("logins", logins)
            .view(viewName).list()

        log.debug { "user list: ${users.joinToString { it.login }}" }
        users
    } catch (dbe: Exception) {
        log.error(dbe) { ErrorConstants.DB_ERROR }
        throw CrdUserException(ErrorConstants.DB_ERROR)
    }

    /**
     * Get user entities for list of user ids
     *
     * @return user list
     */
    override fun getUsersFromIds(ids: Collection<String>): List<AppUser> {
        val userList: List<AppUser>
        try {
            persistence!!.createTransaction().use {
                val query = QueryConstants.FETCH_USERS_BY_ID
                val em: EntityManager = persistence.entityManager
                userList = em.createQuery(query, AppUser::class.java)
                    .setParameter("ids", ids).resultList
                it.commit()
                log.debug { "user list: $userList" }
            }
        } catch (dbe: Exception) {
            log.error(dbe) { ErrorConstants.DB_ERROR }
            throw CrdUserException(ErrorConstants.DB_ERROR)
        }
        return userList
    }

    /**
     * Creates credible user
     *
     * @param userDto
     * @param institution
     * @return
     */
    override fun createUser(userDto: UserDto, institution: Institution): AppUser {
        val commitContext = CommitContext()
        val user = dataManager.create(AppUser::class.java)
        commitContext.addInstanceToCommit(user)

        addDetails(user, userDto)
        addAccessGroup(user)
        createRelations(user, institution)
        dataManager.commit(commitContext)
        return user
    }

    /**
     * Updates existing credible user details
     *
     * @param user
     * @return user
     */
    override fun updateUser(user: AppUser): AppUser = dataManager.commit(user)

    private fun addAccessGroup(user: AppUser) = dataManager.load(Group::class.java)
        .query(QueryConstants.FETCH_GROUP_BY_NAME)
        .parameter("groupName", AppConstants.DEFAULT_GRP_NAME)
        .one().also { user.group = it }

    private fun createRelations(user: AppUser, institution: Institution) {
        user.institution = institution
    }

    private fun addDetails(user: AppUser, userDto: UserDto) {
        user.login = userDto.login
        user.password = passwordEncryption!!.getPasswordHash(user.id, userDto.password!!)
        user.firstName = userDto.firstName
        user.middleName = userDto.middleName
        user.lastName = userDto.lastName
        user.name = UserHelper.userFullName(user).toString()
        user.email = userDto.email
        user.position = userDto.position
        user.profileImage = userDto.profileImage
        user.cadLevel = userDto.cadLevel
    }
}
