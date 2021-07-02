/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class StepTrigger(private val id: String) : EnumClass<String> {
    USER_INITIATED("user-initiated"),
    REWORK("rework");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): StepTrigger? = values().find { it.id == id }
    }
}
