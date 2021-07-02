/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import java.util.Date
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.SectionDto
import com.anzi.credible.dto.SubmissionActionDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.SubmissionSection
import com.anzi.credible.utils.AppUtils.elze
import com.anzi.credible.utils.AppUtils.then
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.JsonNode
import com.haulmont.cuba.security.entity.User
import mu.KotlinLogging

object SubmissionHelper {
    private val log = KotlinLogging.logger { }

    /**
     * Build a complete Submission DTO with child entities
     *
     * @param submission
     * @param sections
     * @param loggedUser
     * @param creator
     */
    fun buildDto(submission: Submission, sections: List<SectionDto>, loggedUser: User, creator: User? = null) =
        SubmissionDto(
            submission.id,
            submission.borrower?.run {
                BorrowerDto(id, name)
            },
            submission.submissionPurpose.map { it.type!! },
            submission.submissionPurpose.map { it.purpose!! },
            submission.due!!.formatTo(),
            submission.note,
            submission.getStatus()?.id,
            null,
            submission.workflow?.run {
                WorkflowHelper.submissionWorkFlow(this, submission.workflowStep)
            },
            sections,
            submission.tasks.map { task -> TaskHelper.buildDto(task, loggedUser) },
            submission.team.map { user ->
                UserDto(
                    id = user.id,
                    name = user.name,
                    position = user.position,
                    cadLevel = user.cadLevel
                )
            }.toMutableSet(),
            creator?.run { UserDto(this.id, name = this.name) },
            loggedUser.let { submission.flaggedUsers.any { loggedUser == it } },
            displayRef = submission.displayRef
        )

    /**
     * Build Submission DTO of immediate attributes
     *
     * @param submission
     */
    fun buildDtoForList(submission: Submission, loggedUser: User) =
        SubmissionDto(
            submission.id,
            submission.borrower?.run {
                BorrowerDto(id, name)
            },
            submission.submissionPurpose.map { it.type!! },
            submission.submissionPurpose.map { it.purpose!! },
            submission.due!!.formatTo(),
            submission.note,
            submission.getStatus()?.id,
            submission.workflowStep,
            flagged = submission.flaggedUsers.any { loggedUser == it },
            createdAt = submission.createTs.formatTo(),
            actions = actions(submission),
            isViewed = submission.viewedUsers?.any { user -> loggedUser == user },
            workflowStepName = submission.workflow?.steps?.get(submission.workflowStep!! - 1)?.name
        )

    /**
     * Create Purpose entities
     *
     * @param types
     * @param submissionDto
     */
    fun createSubmissionPurposes(types: JsonNode, submissionDto: SubmissionDto): HashMap<String, String> {
        val purpose = HashMap<String, String>()
        submissionDto.types!!.withIndex()
            .forEach { subType ->
                types.find { it["slug"].asText() == subType.value }?.get("title")?.asText()?.let {
                    purpose[submissionDto.purposes!![subType.index]] = it
                }
            }
        return purpose
    }

    /**
     * Update/Remove Submission Sections
     *
     * @param sections
     * @param submissionDto
     * @return
     */
    fun updateSections(sections: MutableList<SubmissionSection>, submissionDto: SubmissionDto):
        MutableList<SubmissionSection> {
            val deleteSections = mutableListOf<SubmissionSection>()
            sections.forEach { section ->
                submissionDto.sections?.find { it.slug == section.slug }?.also {
                    log.debug { "New content: ${it.content}" }
                    (it.content != null).then {
                        log.info { "Section: ${it.slug} has been updated" }
                        section.content = it.content
                    }.elze {
                        log.info { "Section: ${it.slug} has been deleted" }
                        deleteSections.add(section)
                    }
                }
            }
            return deleteSections
        }

    private fun actions(submission: Submission): SubmissionActionDto? {
        var count: Int
        return if (Date().after(submission.due)) {
            SubmissionActionDto("Submission overdue", "Overdue")
        } else {
            when {
                submission.tasks.count { it.getTaskStatus() == TaskStatus.OUTDATED }.also { count = it } > 0 ->
                    SubmissionActionDto(
                        "$count ${AppConstants.ACTION_OVERDUE.format(
                            when (count) {
                                1 -> "task"
                                else -> "tasks"
                            }
                        )}",
                        "Overdue"
                    )
                submission.tasks.count { it.getTaskStatus() == TaskStatus.PENDING }.also { count = it } > 0 ->
                    SubmissionActionDto(
                        "$count ${AppConstants.ACTION_PENDING.format(
                            when (count) {
                                1 -> "task"
                                else -> "tasks"
                            }
                        )}",
                        "Pending"
                    )
                else -> null
            }
        }
    }
}
