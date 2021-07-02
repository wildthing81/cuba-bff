/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class TaskType(private val id: String) : EnumClass<String> {
    FORMAL("formal"),
    INFORMAL("informal"),
    WORK("work");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): TaskType? = values().find { it.id == id }
    }
}
