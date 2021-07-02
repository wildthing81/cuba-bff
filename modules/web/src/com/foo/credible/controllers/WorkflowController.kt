package com.foo.credible.controllers

import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.service.WorkflowService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import javax.inject.Inject

@ApiIgnore
@Api(tags = ["Workflow APIs"], description = "Operations on workflow")
@RestController
class WorkflowController {

    @Inject
    private lateinit var workflowService: WorkflowService

    /**
     *  `Create new Workflow` API
     *
     * @param workflowDto
     * @return
     */
    @ApiOperation(value = "Create a workflow", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 201, message = "Workflow created", response = WorkflowDto::class),
            ApiResponse(code = 500, message = "Error creating workflow", response = ErrorDto::class)
        ]
    )
    @PostMapping("/workflow")
    fun createWorkFlow(@RequestBody workflowDto: WorkflowDto):
        ResponseEntity<out Any> {
            val response = workflowService.createWorkFlow(workflowDto)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.BAD_REQUEST)
            else ResponseEntity(response as WorkflowDto, HttpStatus.CREATED)
        }

    /**
     * `Fetch All Workflows` API
     *
     * @return
     */
    @ApiOperation(value = "Fetch all of an institution's workflows", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Workflows fetched",
                response = WorkflowDto::class,
                responseContainer =
                "List"
            ),
            ApiResponse(code = 500, message = ErrorConstants.ERR_FETCH_WORKFLOWS, response = ErrorDto::class)
        ]
    )
    @GetMapping("/workflows")
    fun fetchWorkFlows(): ResponseEntity<out Any> {
        val response = workflowService.fetchWorkFlows()

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(response as List<*>, HttpStatus.OK)
    }
}
