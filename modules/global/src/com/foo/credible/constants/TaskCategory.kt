/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class TaskCategory(private val id: String) : EnumClass<String> {
    DECISION("decision"),
    WORK("work");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): TaskCategory? = values().find { it.id == id }
    }
}
