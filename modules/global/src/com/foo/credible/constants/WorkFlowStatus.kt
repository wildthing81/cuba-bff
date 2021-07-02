/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class WorkFlowStatus(private val id: String) : EnumClass<String> {
    DRAFTING("drafting"),
    PROGRESS("progress"),
    APPROVED("approved");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): WorkFlowStatus? = values().find { it.id == id }
    }
}
