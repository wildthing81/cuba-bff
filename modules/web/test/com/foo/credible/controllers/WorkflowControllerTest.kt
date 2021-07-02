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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.service.WorkflowService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowControllerTest {

    @MockK
    lateinit var workflowService: WorkflowService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = WorkflowController()

    private lateinit var testJson: String

    private lateinit var testDto: WorkflowDto

    @BeforeAll
    fun setUp() {
        mockMvc = standaloneSetup(testController)
            .setMessageConverters(MappingJackson2HttpMessageConverter()).build()
        testJson = """{
                "name": "test-workflow",
                "initialStatus": "drafting",
                "submissionTypes": ["new-facility","risk-grading"],
                "steps": [
                {
                    "index": 1,
                    "name": "test-step",
                    "layout":{
                        "commentsVisible": true,
                        "submissionAnalysisEditable": false
                    },
                    "transitions":[
                        {
                            "to": 3,
                            "trigger" : "user-initiated",
                            "label": "test-label",
                            "submissionStatus": "done",
                            "borrowerRefresh": false
                        }
                    ]
                }
             ]           
        }"""

        testDto = ObjectMapper().registerKotlinModule().readValue(testJson, WorkflowDto::class.java)
    }

    @Test
    fun testCreateWorkFlowSuccess() {
        every {
            workflowService
                .createWorkFlow(ofType(WorkflowDto::class))
        } returns testDto.copy(id = UUID.fromString("5d5f1bed-95a1-8e88-10e1-2250052479d1"))

        mockMvc.perform(
            post("/workflow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(
                content().json(
                    """{
                    "id": "5d5f1bed-95a1-8e88-10e1-2250052479d1",
                    "name": "test-workflow",
                    "initialStatus": "drafting",
                    "submissionTypes": ["new-facility","risk-grading"],
                    "steps": [
                    {
                        "index": 1,
                        "name": "test-step",
                        "layout":{
                            "commentsVisible": true,
                            "submissionAnalysisEditable": false
                        },
                        "transitions":[
                            {
                                "to": 3,
                                "trigger" : "user-initiated",
                                "label": "test-label",
                                "submissionStatus": "done",
                                "borrowerRefresh": false
                            }
                        ]
                    }
                 ]           
            }"""
                )
            ).andReturn()
    }

    @Test
    fun testCreateWorkFlowFailure() {
        every {
            workflowService
                .createWorkFlow(ofType(WorkflowDto::class))
        } returns ErrorDto(
            "Workflow",
            null,
            ErrorConstants.CREATE_ENTITY,
            "Error creating Workflow"
        )

        mockMvc.perform(
            post("/workflow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Workflow",
                    "id": null,
                    "errorMessage": "Unable to create entity",
                    "errorDetails": "Error creating Workflow"
                }"""
                )
            )
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
