/*
 * The code is copyright ©2021
 */
package com.foo.credible.repository.impl

import com.anzi.credible.constants.ViewConstants
import java.util.UUID
import javax.inject.Inject
import org.springframework.stereotype.Repository
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.dto.WorkflowStepDto
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.StepTransition
import com.anzi.credible.entity.Workflow
import com.anzi.credible.entity.WorkflowStep
import com.anzi.credible.repository.QueryConstants
import com.anzi.credible.repository.WorkflowRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import mu.KotlinLogging

@Repository
open class WorkflowRepositoryImpl : WorkflowRepository {

    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var dataManager: DataManager

    /**
     * Query Workflow by Id
     *
     * @param workFlowId
     * @param viewName
     * @return
     */
    override fun fetchWorkFlowById(workFlowId: String, viewName: String): Workflow = dataManager
        .load(Workflow::class.java)
        .id(UUID.fromString(workFlowId))
        .view(viewName).one()

    /**
     * Query Workflow by Submission Type
     *
     * @param submissionType
     * @param viewName
     * @return
     */
    override fun fetchWorkFlowBySubmissionType(submissionType: String, viewName: String): Workflow = dataManager
        .load(Workflow::class.java)
        .query(QueryConstants.FETCH_WORKFLOW_BY_SUB_TYPE)
        .parameter("subType", submissionType)
        .view(viewName).one()

    /**
     * Query to fetch All Workflows for an Institution
     *
     * @param institution
     * @return list of workflows
     */
    override fun fetchWorkFlows(institution: Institution, viewName: String): List<Workflow> = dataManager
        .load(Workflow::class.java)
        .query(QueryConstants.FETCH_INSTITUTION_WORKFLOWS)
        .parameter("institution", institution)
        .view(viewName)
        .list()

    /**
     * Update existing Workflow
     *
     * @param workflow
     */
    override fun updateWorkFlow(workflow: Workflow): Workflow = dataManager.commit(workflow)

    /**
     * Persists a new WorkFlow
     *
     * @param workflowDto
     * @param institution
     * @return
     */
    override fun createWorkFlow(
        workflowDto: WorkflowDto,
        institution: Institution,
    ): Workflow {
        log.debug { "Start: Saving a new WorkFlow" }
        val commitContext = CommitContext()
        val workFlow = dataManager.create(Workflow::class.java)
        commitContext.addInstanceToCommit(workFlow)

        addBasicDetails(workFlow, workflowDto)
        workflowDto.steps?.map {
            log.info { "step: $it" }
            addStep(
                ObjectMapper().registerKotlinModule().convertValue(it, WorkflowStepDto::class.java),
                workFlow,
                commitContext
            )
        }
        createAssociations(workFlow, institution)
        dataManager.commit(commitContext)
        log.debug { "End: Saving a new WorkFlow" }
        return dataManager.reload(workFlow, ViewConstants.WORKFLOW_FETCH)
    }

    private fun addStep(dto: WorkflowStepDto, workflow: Workflow, commitContext: CommitContext) {
        val workFlowStep = dataManager.create(WorkflowStep::class.java)
        workFlowStep.workflow = workflow
        workFlowStep.index = dto.index
        workFlowStep.name = dto.name
        workFlowStep.layout = dto.layout?.toString()
        dto.transitions?.map {
            val transition = dataManager.create(StepTransition::class.java)
            transition.borrowerRefresh = it.borrowerRefresh
            transition.trigger = it.trigger
            transition.step = workFlowStep
            transition.toStepIndex = it.to
            transition.label = it.label
            transition.submissionStatus = it.submissionStatus
            commitContext.addInstanceToCommit(transition)
        }

        commitContext.addInstanceToCommit(workFlowStep)
    }

    private fun addBasicDetails(
        workflow: Workflow,
        workflowDto: WorkflowDto,
    ) {
        workflow.name = workflowDto.name
        workflow.initialStatus = workflowDto.initialStatus
        workflow.submissionTypes = workflowDto.submissionTypes as MutableList<String>?
    }

    private fun createAssociations(
        workflow: Workflow,
        institution: Institution?,
    ) {
        workflow.institution = institution
    }
}
