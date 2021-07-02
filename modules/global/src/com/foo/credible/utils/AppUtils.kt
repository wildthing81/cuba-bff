/*
 * The code is copyright ©2021
 */

package com.foo.credible.utils

import org.apache.commons.lang3.RandomStringUtils
import com.anzi.credible.constants.CKEditorConstants
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

object AppUtils {

    infix fun Boolean.then(action: () -> Unit): Boolean {
        if (this) {
            action.invoke()
        }
        return this
    }

    infix fun Boolean.elze(action: () -> Unit) {
        if (!this) {
            action.invoke()
        }
    }

    fun <E> MutableSet<E>?.createIfNull(elem: E): MutableSet<E>? {
        return (this?.plus(elem) ?: mutableSetOf(elem)) as MutableSet<E>?
    }

    fun Boolean.toInt() = if (this) 1 else 0

    fun Int.toBoolean() = this != 0

    fun displayRef(alphacount: Int, numCount: Int) =
        "${RandomStringUtils.randomAlphabetic(alphacount).toUpperCase()}-${RandomStringUtils.randomNumeric(numCount)}"

    fun createJWTToken(
        payload: Map<String, Any>,
        algorithm: Algorithm
    ): String = JWT.create()
        .withPayload(payload)
        .withIssuer(CKEditorConstants.ISSUER)
        .sign(algorithm)

    fun String.nullNotEqual(elem: String?): Boolean {
        return elem.isNullOrEmpty() || this != elem
    }

    fun ByteArray?.isNotNullAndEmpty(): Boolean {
        return this != null && this.isNotEmpty()
    }
}
