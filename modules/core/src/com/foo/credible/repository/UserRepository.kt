/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.security.entity.User

interface UserRepository {

    fun getAllSystemUsers(): List<User>?

    fun getUsersFromLogins(logins: List<String>, viewName: String = ViewConstants.USER_FETCH): List<AppUser>

    fun getUsersFromIds(ids: Collection<String>): List<AppUser>

    fun createUser(userDto: UserDto, institution: Institution): AppUser

    fun assignRoleToUser(role: Role, user: AppUser)

    fun fetchInstitutionUsers(institution: Institution): List<AppUser>

    fun updateUser(user: AppUser): AppUser
}
