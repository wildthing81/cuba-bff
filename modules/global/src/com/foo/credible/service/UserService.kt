/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.AppUser
import com.haulmont.cuba.security.entity.Role

interface UserService {
    companion object {
        const val NAME = "crd_UserService"
    }

    fun userDetails(): Any

    fun fetchLoggedUser(): AppUser

    fun fetchUsersByLogin(logins: List<String>): List<AppUser>

    fun createUser(userDto: UserDto): Any

    fun logout(): Any

    fun assignRoleToUser(role: Role?, user: AppUser)

    fun fetchInstitutionUsers(): Any

    fun fetchUsersById(ids: List<String>): List<AppUser>

    fun generateJWTToken(accessToken: String): Any

    fun updateUser(userId: String): Any
}
