/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class SubmissionStatus(private val id: String) : EnumClass<String> {
    DRAFTING("drafting"),
    PENDING("pending"),
    APPROVED("approved"),
    DECLINED("declined"),
    WITHDRAWN("withdrawn");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): SubmissionStatus? = values().find { it.id == id }
    }
}
