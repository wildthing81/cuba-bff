/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Workflow

interface WorkflowRepository {

    fun fetchWorkFlowById(workFlowId: String, viewName: String): Workflow?

    fun updateWorkFlow(workflow: Workflow): Workflow

    fun createWorkFlow(workflowDto: WorkflowDto, institution: Institution): Workflow?

    fun fetchWorkFlowBySubmissionType(submissionType: String, viewName: String = ViewConstants.LOCAL): Workflow?

    fun fetchWorkFlows(institution: Institution, viewName: String = ViewConstants.WORKFLOW_FETCH): List<Workflow>
}
