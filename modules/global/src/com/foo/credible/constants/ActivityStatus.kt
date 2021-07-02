/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class ActivityStatus(private val id: String) : EnumClass<String> {
    CREATED("Created"),
    APPROVED("Approved"),
    ENDORSED("Endorsed"),
    UPDATED("Updated"),
    WITHDREW("Withdrew"),
    DECLINED("Declined"),
    DELETED("Deleted"),
    ERROR("Error");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): ActivityStatus? = values().find { it.id == id }
    }
}
