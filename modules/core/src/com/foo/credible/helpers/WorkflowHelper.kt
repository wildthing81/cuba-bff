/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import com.anzi.credible.constants.StepTrigger
import com.anzi.credible.dto.StepTransitionDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.dto.WorkflowStepDto
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.Workflow
import com.anzi.credible.utils.DateUtils.formatTo
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.security.entity.User

object WorkflowHelper {

    /**
     * Build a small Submission-focused WorkFlow DTO
     *
     * @param currentStep
     * @param workflow
     * @return
     */
    fun submissionWorkFlow(workflow: Workflow, currentStep: Int?): WorkflowDto =
        WorkflowDto(
            workflow.id,
            workflow.name,
            workflow.steps?.sortedBy { it.index }?.map { it.name as String },
            transitions = workflow.steps!!.find { it.index == currentStep }
                ?.transitions?.filter { it.getStepTrigger() == StepTrigger.USER_INITIATED }
                ?.map { it ->
                    StepTransitionDto(it.toStepIndex, it.label)
                },
            current = currentStep
        )

    /**
     * Check if User initiated step transition is allowed
     *
     * @param it
     * @param toStepIndex
     * @param trigger
     */
    fun isValidTransition(
        it: Submission,
        toStepIndex: Int,
        trigger: StepTrigger
    ) = it.workflowStep?.let { currentStep ->
        it.workflow!!.steps?.find { it.index == currentStep }?.transitions
            ?.find { it.toStepIndex == toStepIndex && it.getStepTrigger() == trigger }
    }

    /**
     * Build a complete WorkFlow DTO with child entities
     *
     * @param workflow
     * @param author
     */
    fun buildDto(workflow: Workflow, author: User? = null) =
        WorkflowDto(
            workflow.id,
            workflow.name,
            workflow.steps!!.map { step ->
                WorkflowStepDto(
                    step.index,
                    step.name,
                    ObjectMapper().readTree(step.layout),
                    step.transitions?.map { transition ->
                        StepTransitionDto(
                            transition.toStepIndex,
                            transition.label,
                            transition.trigger,
                            transition.submissionStatus,
                            transition.borrowerRefresh
                        )
                    }
                )
            }.sortedBy { it.index },
            initialStatus = workflow.initialStatus,
            submissionTypes = workflow.submissionTypes,
            author = author?.run { UserDto(this.id, name = this.name) },
            createdAt = workflow.createTs.formatTo(),
            updatedAt = workflow.updateTs.formatTo(),
            version = workflow.version
        )
}
