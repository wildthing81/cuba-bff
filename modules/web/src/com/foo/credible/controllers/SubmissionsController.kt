/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.CommentDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.CommentService
import com.anzi.credible.service.SubmissionService
import com.anzi.credible.utils.AppUtils.elze
import com.anzi.credible.utils.DateUtils.formatTo
import com.anzi.credible.utils.AppUtils.then
import com.anzi.credible.utils.DateUtils.toUTC
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject
import com.anzi.credible.constants.AppConstants.COUNT

@ApiIgnore
@Api(tags = ["Submission APIs"], description = "Operations on submission")
@RestController
class SubmissionsController {

    @Inject
    private lateinit var submissionService: SubmissionService

    @Inject
    private lateinit var activityService: ActivityService

    @Inject
    private lateinit var commentService: CommentService

    @ApiOperation(value = "Create a submission", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 201, message = "Submission created", response = SubmissionDto::class),
            ApiResponse(code = 500, message = "Error creating submission", response = ErrorDto::class)
        ]
    )
    @PostMapping("/submission")
    fun createSubmission(@RequestBody submissionDto: SubmissionDto):
        ResponseEntity<out Any> {
            val response = submissionService.createSubmission(submissionDto)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(response as SubmissionDto, HttpStatus.CREATED)
        }

    @DeleteMapping("/submission/{submissionId}")
    fun deleteSubmission(@PathVariable submissionId: String):
        ResponseEntity<out Any> {
        /*val response = submissionService.deleteSubmission(submissionId)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(HttpStatus.NO_CONTENT)*/
            TODO()
        }

    @ApiOperation(value = "Step transition", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Valid/Invalid transition", response = ObjectNode::class),
            ApiResponse(code = 500, message = ErrorConstants.ERR_STEP_TRANSITION, response = ErrorDto::class)
        ]
    )
    @PutMapping("/submission/{submissionId}/transition/{to}")
    fun actionTransition(@PathVariable submissionId: String, @PathVariable("to") toStepIndex: Int):
        ResponseEntity<Any?> {
            var response = submissionService.actionStepTransition(submissionId, toStepIndex)

            return if (response is Boolean) {
                response.then { response = ObjectMapper().createObjectNode().put("status", "Valid Transition") } elze
                    { response = ObjectMapper().createObjectNode().put("status", "InValid Transition") }
                ResponseEntity(response, HttpStatus.NO_CONTENT)
            } else ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        }

    @ApiOperation(value = "Fetch a submission by id", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Submission fetched", response = SubmissionDto::class),
            ApiResponse(code = 400, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping("/submission/{submissionId}")
    fun fetchSubmission(@PathVariable submissionId: String): ResponseEntity<out Any> {
        val response = submissionService.fetchSubmission(submissionId)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.BAD_REQUEST)
        else ResponseEntity(response as SubmissionDto, HttpStatus.OK)
    }

    @ApiOperation(value = "Fetch submission activities", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Activities fetched",
                response = JsonNode::class,
                responseContainer = "List"
            ),
            ApiResponse(code = 500, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping("/submission/{submissionId}/activities")
    fun fetchActivities(
        @PathVariable submissionId: String,
        @RequestParam(required = false) startAt: String?,
        @RequestParam(required = false) endAt: String?,
    ): ResponseEntity<out Any> {

        var startDate: Date = LocalDateTime.now().minusWeeks(1).formatTo().toUTC()
        if (startAt != null) {
            startDate = startAt.toUTC()
        }
        var endDate: Date = LocalDateTime.now().formatTo().toUTC()
        if (endAt != null) {
            endDate = endAt.toUTC()
        }

        val response = activityService.fetchActivities(submissionId, startDate, endDate)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(response as List<*>, HttpStatus.OK)
    }

    @ApiOperation(
        value = "Fetch assigned submissions",
        produces = "application/json"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Submissions fetched",
                response = SubmissionDto::class,
                responseContainer = "List"
            ),
            ApiResponse(code = 404, message = "Incorrect path parameter"),
            ApiResponse(code = 500, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping(value = ["/submissions/{pathVar}", "/submissions"])
    fun fetchSubmissions(
        @ApiParam(
            required = false,
            value = "Path parameter for API variant",
            allowableValues = COUNT
        )
        @PathVariable(required = false) pathVar: String?
    ): ResponseEntity<out Any> {
        if (pathVar != null && pathVar != COUNT) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        val response = submissionService.fetchAssignedSubmissions(pathVar)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(if (response is Int) response else response as List<*>, HttpStatus.OK)
    }

    @ApiOperation(value = "Fetch watched submissions", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Submission fetched",
                response = SubmissionDto::class,
                responseContainer = "List"
            ),
            ApiResponse(code = 500, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping(value = ["/submissions/watching", "/submissions/watching/{pathVar}"])
    fun fetchWatchedSubmissions(
        @ApiParam(
            required = false,
            value = "Path parameter for API variant",
            allowableValues = "count"
        )
        @PathVariable(required = false) pathVar: String?
    ):
        ResponseEntity<out Any> {
            if (pathVar != null && pathVar != COUNT) {
                return ResponseEntity(HttpStatus.NOT_FOUND)
            }

            val response = submissionService.fetchWatchedSubmissions(pathVar)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(if (response is Int) response else response as List<*>, HttpStatus.OK)
        }

    @ApiOperation(value = "Update a submission", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 204, message = "Submission updated", response = SubmissionDto::class),
            ApiResponse(code = 500, message = ErrorConstants.UPDATE_ENTITY, response = ErrorDto::class)
        ]
    )
    @PatchMapping("/submission/{submissionId}")
    fun updateSubmission(@PathVariable submissionId: String, @RequestBody requestBody: JsonNode):
        ResponseEntity<out Any> {
            val flag = requestBody.get("isFlagged")
            val response = if (flag != null) {
                submissionService.flaggedForUser(submissionId, flag.asBoolean())
            } else {
                submissionService.updateSubmission(
                    submissionId,
                    ObjectMapper().registerKotlinModule()
                        .treeToValue(requestBody, SubmissionDto::class.java)
                )
            }

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(HttpStatus.NO_CONTENT)
        }

    @GetMapping("/submission/{submissionId}/comments")
    fun fetchSubmissionComments(
        @PathVariable(required = true) submissionId: String
    ): ResponseEntity<out Any> {
        val response = commentService.fetchSubmissionComments(submissionId)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.BAD_REQUEST)
        else ResponseEntity(response as List<CommentDto>, HttpStatus.OK)
    }
}
