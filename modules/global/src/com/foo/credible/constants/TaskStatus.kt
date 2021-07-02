/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class TaskStatus(private val id: String) : EnumClass<String> {
    PENDING("pending"),
    APPROVED("approved"),
    DECLINED("declined"),
    REWORK("rework"),
    OUTDATED("outdated"),
    WITHDRAWN("withdrawn"),
    ABSTAINED("abstained"),
    ENDORSED("endorsed"),
    COMPLETE("complete");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String?): TaskStatus? = values().find { it.id == id }
    }
}
