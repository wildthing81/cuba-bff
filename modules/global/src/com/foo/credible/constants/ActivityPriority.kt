/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class ActivityPriority(private val id: String) : EnumClass<String> {
    SUCCESS("success"),
    WARNING("warning"),
    INFO("info"),
    ERROR("error");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): ActivityPriority? = values().find { it.id == id }
    }
}
