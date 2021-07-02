/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import com.anzi.credible.constants.CKEditorConstants
import com.haulmont.cuba.security.entity.User

object UserHelper {
    /**
     * returns full name of the user considering only no null values. [firstname, middlename, lastname]
     *
     * @param user
     * @return
     */
    fun userFullName(user: User): Any {
        return sequenceOf(user.firstName, user.lastName).filter { s ->
            !s.isNullOrEmpty()
        }.joinToString(separator = " ")
    }

    /**
     * Creates a JWT token payload
     * @param environmentId
     * @param subject
     * @param name
     * @param email
     *
     * @return Token payload map
     */
    fun tokenPayload(environmentId: String, subject: String, email: String, name: String): Map<String, Any> {
        val payloadAuth: Map<String, Any> = mapOf(
            Pair(
                CKEditorConstants.AUTH_COLLABORATION,
                mapOf(
                    Pair(
                        "*",
                        mapOf(
                            Pair(CKEditorConstants.ROLE, CKEditorConstants.ROLE_WRITTER)
                        )
                    )
                )
            )
        )
        return mapOf(
            Pair(CKEditorConstants.ENVIRONMENT, environmentId),
            Pair(CKEditorConstants.ISSUED_AT, System.currentTimeMillis() / 1000),
            Pair(CKEditorConstants.SUBJECT, subject),
            Pair(
                CKEditorConstants.USER,
                mapOf(
                    Pair(CKEditorConstants.EMAIL, email),
                    Pair(CKEditorConstants.NAME, name)
                )
            ),
            Pair(CKEditorConstants.AUTH, payloadAuth)
        )
    }
}
