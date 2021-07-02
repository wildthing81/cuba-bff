/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import javax.inject.Inject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import com.anzi.credible.constants.AppConstants.COUNT
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.service.TaskService
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import springfox.documentation.annotations.ApiIgnore

@ApiIgnore
@Api(tags = ["Task APIs"], description = "Operations on task")
@RestController
class TaskController {

    @Inject
    private lateinit var taskService: TaskService

    @ApiOperation(value = "Create a task", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 201, message = "Task created", response = TaskDto::class),
            ApiResponse(code = 500, message = "Error creating task", response = ErrorDto::class)
        ]
    )
    @PostMapping("/task")
    fun createTask(@RequestBody taskDto: TaskDto):
        ResponseEntity<out Any> {
            val response = taskService.createTask(taskDto)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(response as TaskDto, HttpStatus.CREATED)
        }

    @ApiOperation(value = "Update a task", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 204, message = "Task updated", response = TaskDto::class),
            ApiResponse(code = 500, message = ErrorConstants.UPDATE_ENTITY, response = ErrorDto::class)
        ]
    )
    @PatchMapping("/task/{taskId}")
    fun updateTask(@PathVariable taskId: String, @RequestBody requestBody: JsonNode):
        ResponseEntity<out Any> {
            val flag = requestBody.get("isFlagged")
            val response = if (flag != null) {
                taskService.flaggedForUser(taskId, flag.asBoolean())
            } else {
                taskService.updateTask(taskId, requestBody)
            }

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(HttpStatus.NO_CONTENT)
        }

    @ApiOperation(value = "Fetch a task by id", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Task fetched", response = TaskDto::class),
            ApiResponse(code = 400, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping("/task/{taskId}")
    fun fetchTask(@PathVariable taskId: String): ResponseEntity<out Any> {
        val response = taskService.fetchTask(taskId)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.BAD_REQUEST)
        else ResponseEntity(response as TaskDto, HttpStatus.OK)
    }

    @ApiOperation(
        value = "Fetch user created tasks",
        produces = "application/json"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Tasks fetched",
                response = TaskDto::class,
                responseContainer = "List"
            ),
            ApiResponse(code = 404, message = "Incorrect path parameter"),
            ApiResponse(code = 500, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping(value = ["/tasks", "/tasks/{pathVar}"])
    fun fetchUserCreatedTasks(@PathVariable(required = false) pathVar: String?): ResponseEntity<out Any> {
        if (pathVar != null && pathVar != COUNT) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        val response = taskService.fetchTasks(pathVar = pathVar)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(if (response is Int) response else response as List<*>, HttpStatus.OK)
    }

    @ApiOperation(
        value = "Fetch assigned tasks",
        produces = "application/json"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Tasks fetched",
                response = TaskDto::class,
                responseContainer = "List"
            ),
            ApiResponse(code = 404, message = "Incorrect path parameter"),
            ApiResponse(code = 500, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping(value = ["/tasks/assigned", "/tasks/assigned/{pathVar}"])
    fun fetchUserAssignedTasks(@PathVariable(required = false) pathVar: String?): ResponseEntity<out Any> {
        if (pathVar != null && pathVar != COUNT) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        val response = taskService.fetchTasks(true, pathVar)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(if (response is Int) response else response as List<*>, HttpStatus.OK)
    }
}
