/*
 * The code is copyright ©2021
 */

package com.foo.credible.constants

import com.haulmont.chile.core.datatypes.impl.EnumClass

enum class NotificationType(private val id: String) : EnumClass<String> {
    SUBMISSION_CREATED("submission-created"),
    SUBMISSION_TEAM_UPDATED("submission-team-updated"),
    TASK_ASSIGNED("task-assigned"),
    TASK_COMPLETED("task-completed"),
    DECISION_ASSIGNED("decision-assigned"),
    DECISION_APPROVED("decision-approved");

    override fun getId() = id

    companion object {
        @JvmStatic
        fun fromId(id: String): NotificationType? = values().find { it.id == id }
    }
}
