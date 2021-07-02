/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import java.util.UUID
import javax.inject.Inject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.AppConstants.COUNT
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.service.TaskService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import io.mockk.verify

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskControllerTest {

    @MockK
    lateinit var taskService: TaskService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = TaskController()

    private val testId = UUID.randomUUID().toString()
    private lateinit var testJson: String
    private lateinit var testDto: TaskDto

    @BeforeAll
    fun setUp() {
        mockMvc = standaloneSetup(testController)
            .setMessageConverters(MappingJackson2HttpMessageConverter()).build()
        testJson = """{
	            "submissionId": "$testId",
                "category": "decision",
                "due": "2020-12-14T00:40:09Z",
                "type": "formal",
	            "assignee": "test-user"
        }"""

        testDto = ObjectMapper().registerKotlinModule().readValue(testJson, TaskDto::class.java)
    }

    @Test
    fun testCreateTaskSuccess() {
        every {
            taskService
                .createTask(ofType(TaskDto::class))
        } returns testDto.copy(
            taskId = UUID.fromString(testId),
            status = "pending",
            assignee = UserDto(id = UUID.fromString(testId), name = "test-user")
        )

        mockMvc.perform(
            post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(
                content().json(
                    """{
                    "id": "$testId",
                    "submissionId": "$testId",
                    "category": "decision",
                    "type": "formal",
                    "status": "pending",
                    "assignee": {
                        "id": $testId,
                        "name":"test-user"
                     }
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { taskService.createTask(testDto) }
    }

    @Test
    fun testCreateTaskFailure() {
        every {
            taskService
                .createTask(ofType(TaskDto::class))
        } returns ErrorDto(
            "Task",
            null,
            ErrorConstants.CREATE_ENTITY,
            "Error fetching borrower defaults"
        )

        mockMvc.perform(
            post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Task",
                    "id": null,
                    "errorMessage": "Unable to create entity",
                    "errorDetails": "Error fetching borrower defaults"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { taskService.createTask(testDto) }
    }

    @Test
    fun testUpdateTaskSuccess() {
        every {
            taskService
                .updateTask(ofType(String::class), ofType(JsonNode::class))
        } returns AppConstants.SUCCESS

        mockMvc.perform(
            patch("/task/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent)
            .andReturn()

        verify(exactly = 1) { taskService.updateTask(testId, ObjectMapper().readTree(testJson)) }
    }

    @Test
    fun testUpdateTaskFailure() {
        every {
            taskService
                .updateTask(ofType(String::class), ofType(JsonNode::class))
        } returns ErrorDto(
            "Task",
            testId,
            ErrorConstants.UPDATE_ENTITY,
            "Database Error: SQLException"
        )

        mockMvc.perform(
            patch("/task/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Task",
                    "id": $testId,
                    "errorMessage": "Unable to update entity",
                    "errorDetails": "Database Error: SQLException"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { taskService.updateTask(testId, ObjectMapper().readTree(testJson)) }
    }

    @Test
    fun testFetchUserCreatedTasksSuccess() {
        every {
            taskService
                .fetchTasks(ofType(Boolean::class), any())
        } returns listOf(
            testDto.copy(
                taskId = UUID.fromString(testId),
                due = "2020-12-14T00:40:09Z",
                status = "pending",
                assignee = UserDto(
                    id = UUID.fromString(testId),
                    name = "test-user"
                )
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/tasks")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """[{
                    "id": $testId,
                    "submissionId": $testId,
                    "category": "decision",
                    "type": "formal",
                    "status": "pending",
                    "due": "2020-12-14T00:40:09Z",
                    "assignee": {
                        "id": $testId,
                        "name":"test-user"
                     }
                }]"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchUserCreatedTasksCountSuccess() {
        every {
            taskService
                .fetchTasks(ofType(Boolean::class), COUNT)
        } returns 2

        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/count"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string("2"))
            .andReturn()
    }

    @AfterEach
    fun reset() {
        clearAllMocks()
    }

    @AfterAll
    fun tearDown() {
        unmockkAll()
    }
}
