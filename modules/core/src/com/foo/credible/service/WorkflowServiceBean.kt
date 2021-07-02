package com.foo.credible.service

import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.entity.Workflow
import com.anzi.credible.helpers.WorkflowHelper
import com.anzi.credible.repository.WorkflowRepository
import mu.KotlinLogging

@Service(WorkflowService.NAME)
open class WorkflowServiceBean : WorkflowService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var workflowRepository: WorkflowRepository

    /**
     *  Creates a new Workflow & related entities
     *
     * @param workflowDto
     */
    override fun createWorkFlow(workflowDto: WorkflowDto): Any? = try {
        log.info { "Creating a new workflow: ${workflowDto.name}" }
        val loggedUser = userService.fetchLoggedUser()
        workflowRepository.createWorkFlow(workflowDto, loggedUser.institution!!)?.let {
            log.info { "New workflow ${it.id}" }
            WorkflowHelper.buildDto(it, userService.fetchUsersByLogin(listOf(it.createdBy))[0])
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error creating new workflow: ${workflowDto.name}" }
        ErrorDto(
            Workflow::class.simpleName!!,
            null,
            ErrorConstants.CREATE_ENTITY,
            dbe.message
        )
    }

    /**
     * Fetch all Workflows for an Institution
     *
     */
    override fun fetchWorkFlows() = try {
        workflowRepository.fetchWorkFlows(userService.fetchLoggedUser().institution!!).map {
            WorkflowHelper.buildDto(it, userService.fetchUsersByLogin(listOf(it.createdBy))[0])
        }
    } catch (dbe: Exception) {
        log.error(dbe) { ErrorConstants.ERR_FETCH_WORKFLOWS }
        ErrorDto(
            Workflow::class.simpleName!!,
            null,
            ErrorConstants.ERR_FETCH_WORKFLOWS,
            dbe.message
        )
    }
}
