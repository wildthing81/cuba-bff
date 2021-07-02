/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import java.util.Date
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.AppConstants.COUNT
import com.anzi.credible.constants.AppConstants.ENCODING_UTF_8
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.StepTrigger
import com.anzi.credible.dto.CommentDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.CommentService
import com.anzi.credible.service.SubmissionService
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
class SubmissionsControllerTest {

    @MockK
    lateinit var submissionService: SubmissionService

    @MockK
    lateinit var activityService: ActivityService

    @MockK
    lateinit var commentService: CommentService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = SubmissionsController()

    private val testId = UUID.randomUUID().toString()
    private val testCommentId = UUID.randomUUID()
    private lateinit var testJson: String
    private lateinit var testDto: SubmissionDto

    private lateinit var testActivitiesJson: String
    private lateinit var testActivitiesResponse: List<Any>

    @BeforeAll
    fun setUp() {
        mockMvc = standaloneSetup(testController)
            .setMessageConverters(MappingJackson2HttpMessageConverter()).build()

        testJson = """{
	            "borrower":"$testId",
	            "purposes":["test-purpose"],
                "types":["test-type"],
                "due":"2020-12-14T00:40:09Z",
	            "note":"test note",
                "team":[
                   {
                     "id": "$testId",
                     "name": "test_user"
                    }
                 ]
        }"""

        testDto = ObjectMapper().registerKotlinModule().readValue(testJson, SubmissionDto::class.java)

        testActivitiesJson = """
           [
                {
                    "action": {
                        "type": "",
                        "label": "",
                        "payload": [
                            {
                                "key": "createdAt",
                                "value": "2021-01-22T02:01:53Z"
                            },
                            {
                                "key": "createdBy",
                                "value": "jay"
                            },
                            {
                                "key": "type",
                                "value": ""
                            }
                        ]
                    },
                    "status": "SUBMITTED",
                    "message": "jay created this submission",
                    "priority": "INFO",
                    "timestamp": "2021-01-22T02:01:53Z"
                },
                {
                    "action": {
                        "type": "",
                        "label": "",
                        "payload": [
                            {
                                "key": "id",
                                "value": "0ecb0a9b-093b-466b-7d10-ab29985474f2"
                            },
                            {
                                "key": "type",
                                "value": "FORMAL"
                            },
                            {
                                "key": "category",
                                "value": "DECISION"
                            },
                            {
                                "key": "note",
                                "value": ""
                            },
                            {
                                "key": "submission_id",
                                "value": "eaa54484-f0d3-c3bf-8bc9-d037d05006ed"
                            },
                            {
                                "key": "due",
                                "value": "1970-01-01T00:40:09Z"
                            }
                        ]
                    },
                    "status": "CREATED",
                    "message": "jay created the task and assigned to Administrator",
                    "priority": "INFO",
                    "timestamp": "2021-01-22T02:02:34Z"
                },
                {
                    "action": {
                        "type": "",
                        "label": "",
                        "payload": [
                            {
                                "key": "id",
                                "value": "6c7c81f9-07e7-acd1-25db-81a9fee4b6aa"
                            },
                            {
                                "key": "status",
                                "value": "DECLINED"
                            }
                        ]
                    },
                    "status": "UPDATED",
                    "message": "jay updated the task and the task status is DECLINED",
                    "priority": "INFO",
                    "timestamp": "2021-01-22T02:03:06Z"
                }
            ]
        """
        testActivitiesResponse = ObjectMapper().readTree(testActivitiesJson).toList()
    }

    @Test
    fun testFetchSubmissionSuccess() {
        every {
            submissionService
                .fetchSubmission(testId)
        } returns testDto.copy(
            id = UUID.fromString(testId),
            borrower = ObjectMapper().createObjectNode()
                .put("id", testId),
            status = "Open",
            sections = listOf()
        )

        mockMvc.perform(
            get("/submission/$testId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """{
                    "borrower": { "id":$testId },
                    "purposes": ["test-purpose"],
                    "types":["test-type"],
                    "due": "2020-12-14T00:40:09Z",
                    "note": "test note",
                    "status": "Open",
                    "sections": [],
                    "id": $testId
                }"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchSubmissionNoSubmission() {
        every {
            submissionService.fetchSubmission(testId)
        } returns ErrorDto(
            "Submission",
            testId,
            ErrorConstants.FIND_ENTITY,
            "Invalid Id"
        )

        mockMvc.perform(
            get("/submission/$testId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Submission",
                    "id": $testId,
                    "errorMessage": "Unable to find existing entity",
                    "errorDetails": "Invalid Id"
                }"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchAssignedSubmissionsSuccess() {
        every {
            submissionService
                .fetchAssignedSubmissions(any())
        } returns listOf(
            testDto.copy(
                id = UUID.fromString(testId),
                borrower = ObjectMapper().createObjectNode()
                    .put("id", testId),
                status = "Open",
                sections = listOf(),
                workflowStep = 1,
                workflowStepName = "test-workflow-step-name"
            )
        )

        mockMvc.perform(
            get("/submissions")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """[{
                    "borrower": { "id":$testId },
                    "purposes": ["test-purpose"],
                    "types":["test-type"],
                    "due": "2020-12-14T00:40:09Z",
                    "note": "test note",
                    "status": "Open",
                    "id": $testId,
                    "workflowStep": 1,
                    "workflowStepName": "test-workflow-step-name"
                }]"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchAssignedSubmissionsCountSuccess() {
        every {
            submissionService
                .fetchAssignedSubmissions(COUNT)
        } returns 2

        mockMvc.perform(get("/submissions/count"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string("2"))
            .andReturn()
    }

    @Test
    fun testFetchWatchedSubmissionsSuccess() {
        every {
            submissionService
                .fetchWatchedSubmissions(any())
        } returns listOf(
            testDto.copy(
                id = UUID.fromString(testId),
                borrower = ObjectMapper().createObjectNode()
                    .put("id", testId),
                status = "Open",
                sections = listOf(),
                workflowStep = 1,
                workflowStepName = "test-workflow-step-name"
            )
        )

        mockMvc.perform(
            get("/submissions/watching")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """[{
                    "borrower": { "id":$testId },
                    "purposes": ["test-purpose"],
                    "types":["test-type"],
                    "due": "2020-12-14T00:40:09Z",
                    "note": "test note",
                    "status": "Open",
                    "id": $testId,
                    "workflowStep": 1,
                    "workflowStepName": "test-workflow-step-name"
                }]"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchWatchedSubmissionsCountSuccess() {
        every {
            submissionService
                .fetchWatchedSubmissions(COUNT)
        } returns 2

        mockMvc.perform(get("/submissions/watching/count"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string("2"))
            .andReturn()
    }

    @Test
    fun testCreateSubmissionSuccess() {
        every {
            submissionService
                .createSubmission(ofType(SubmissionDto::class))
        } returns testDto.copy(
            id = UUID.fromString(testId),
            borrower = ObjectMapper().createObjectNode()
                .put("id", testId),
            status = "Open",
            sections = listOf()
        )

        mockMvc.perform(
            post("/submission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
                .characterEncoding(ENCODING_UTF_8)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(
                content().json(
                    """{
                    "borrower": { "id":$testId },
                    "types":["test-type"],
                    "purposes": ["test-purpose"],
                    "due": "2020-12-14T00:40:09Z",
                    "note": "test note",
                    "status": "Open",
                    "sections": [],
                    "id": $testId
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { submissionService.createSubmission(testDto) }
    }

    @Test
    fun testCreateSubmissionFailure() {
        every {
            submissionService
                .createSubmission(ofType(SubmissionDto::class))
        } returns ErrorDto(
            "Submission",
            null,
            ErrorConstants.CREATE_ENTITY,
            "Error fetching borrower defaults"
        )

        mockMvc.perform(
            post("/submission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Submission",
                    "id": null,
                    "errorMessage": "Unable to create entity",
                    "errorDetails": "Error fetching borrower defaults"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { submissionService.createSubmission(testDto) }
    }

    @Test
    fun testPerformTransitionSuccess() {
        every {
            submissionService
                .actionStepTransition(ofType(String::class), ofType(Int::class), ofType(StepTrigger::class))
        } returns true

        mockMvc.perform(
            put("/submission/$testId/transition/2")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent)
            .andExpect(
                content().json(
                    """{
                    "status": "Valid Transition"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { submissionService.actionStepTransition(testId, 2) }
    }

    @Test
    fun testPerformTransitionFailure() {
        every {
            submissionService
                .actionStepTransition(ofType(String::class), ofType(Int::class))
        } returns ErrorDto(
            "Submission",
            testId,
            ErrorConstants.UPDATE_ENTITY,
            "Error updating workflow step"
        )

        mockMvc.perform(
            put("/submission/$testId/transition/2")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Submission",
                    "id": $testId,
                    "errorMessage": "Unable to update entity",
                    "errorDetails": "Error updating workflow step"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { submissionService.actionStepTransition(testId, 2) }
    }

    @Test
    fun testFetchActivitiesSuccess() {
        every {
            activityService
                .fetchActivities(
                    ofType(String::class),
                    ofType(Date::class),
                    ofType(Date::class)
                )
        } returns ObjectMapper().readTree(testActivitiesJson).toList()

        mockMvc.perform(
            get("/submission/$testId/activities")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().json(testActivitiesJson))

        mockMvc.perform(
            get("/submission/$testId/activities?startAt=2020-02-05T04:20:00.945Z")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().json(testActivitiesJson))

        mockMvc.perform(
            get("/submission/$testId/activities?endAt=2022-02-05T04:20:00.945Z")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().json(testActivitiesJson))

        mockMvc.perform(
            get("/submission/$testId/activities?startAt=2020-02-05T04:20:00.945Z&endAt=2022-02-05T04:20:00.945Z")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().json(testActivitiesJson))
    }

    @Test
    fun testFetchActivitiesFailure() {
        every {
            activityService
                .fetchActivities(ofType(String::class), ofType(Date::class), ofType(Date::class))
        } returns ErrorDto(
            "Activity",
            testId,
            ErrorConstants.FIND_ENTITY,
            "Unable to find existing entity"
        )

        mockMvc.perform(
            get("/submission/$testId/activities?startAt=2020-02-05T04:20:00.945Z&endAt=2022-02-05T04:20:00.945Z")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Activity",
                    "id": $testId,
                    "errorMessage": "Unable to find existing entity",
                    "errorDetails": "Unable to find existing entity"
                }"""
                )
            )
            .andReturn()
    }

    @Test
    fun testUpdateSubmissionFlag() {
        val updateJson = """{
	        "isFlagged": true
        }"""
        every {
            submissionService
                .flaggedForUser(ofType(String::class), ofType(Boolean::class))
        } returns AppConstants.SUCCESS

        mockMvc.perform(
            patch("/submission/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent)
            .andReturn()
    }

    @Test
    fun testUpdateSubmissionFlagFailure() {
        val updateJson = """{
	        "isFlagged": false
        }"""
        every {
            submissionService
                .flaggedForUser(ofType(String::class), ofType(Boolean::class))
        } returns ErrorDto(
            "Submission",
            testId,
            ErrorConstants.UPDATE_ENTITY,
            "Database Error"
        )

        mockMvc.perform(
            patch("/submission/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Submission",
                    "id": $testId,
                    "errorMessage": "Unable to update entity",
                    "errorDetails": "Database Error"
                }"""
                )
            )
            .andReturn()
    }

    @Test
    fun testUpdateSubmissionTeamSuccess() {
        val updateJson = """{
	        "team": [
                "facddb17-227a-b813-797c-1e61d25d57c6"
            ]
        }"""

        every {
            submissionService.updateSubmission(ofType(String::class), any())
        } returns AppConstants.SUCCESS

        mockMvc.perform(
            patch("/submission/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent)
            .andReturn()
    }

    @Test
    fun testUpdateSubmissionTeamFailure() {
        val updateJson = """{
	        "team": [
                "facddb17-227a-b813-797c-1e61d25d57c6"
            ]
        }"""

        every {
            submissionService.updateSubmission(ofType(String::class), any())
        } returns ErrorDto(
            "Submission",
            testId,
            ErrorConstants.UPDATE_ENTITY,
            "Database Error"
        )

        mockMvc.perform(
            patch("/submission/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Submission",
                    "id": $testId,
                    "errorMessage": "Unable to update entity",
                    "errorDetails": "Database Error"
                }"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchSubmissionCommentsSuccess() {
        val testUserId = UUID.randomUUID()
        val testDtoList: List<CommentDto> = listOf(
            CommentDto(
                testCommentId,
                null,
                "First comment",
                "2021-03-10T06:19:14.833Z",
                UserDto(id = testUserId, name = "Anna Analyst"),
                "2021-03-10T06:19:14.833Z"
            ),
            CommentDto(
                testCommentId,
                null,
                "Second comment",
                "2021-04-12T06:19:14.833Z",
                UserDto(id = testUserId, name = "Anna Analyst"),
                "2021-04-12T06:19:14.833Z"
            )
        )

        every {
            commentService.fetchSubmissionComments(testId)
        } returns testDtoList

        mockMvc.perform(
            get("/submission/$testId/comments")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """[
                {
                    "id": $testCommentId,
                    "text": "First comment",
                    "createdAt": "2021-03-10T06:19:14.833Z",
                    "createdBy": {
                        "id": $testUserId,
                        "name": "Anna Analyst"
                    },
                    "updatedAt": "2021-03-10T06:19:14.833Z"
                }, 
                {
                    "id": $testCommentId,
                    "text": "Second comment",
                    "createdAt": "2021-04-12T06:19:14.833Z",
                    "createdBy": {
                        "id": $testUserId,
                        "name": "Anna Analyst"
                    },
                    "updatedAt": "2021-04-12T06:19:14.833Z"
                }]"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchSubmissionCommentsThatReturnsNoComments() {
        every {
            commentService.fetchSubmissionComments(testId)
        } returns ErrorDto(
            "Submission",
            testId,
            ErrorConstants.FIND_ENTITY,
            "No comment exists for: $testId"
        )

        mockMvc.perform(
            get("/submission/$testId/comments")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Submission",
                    "id": $testId,
                    "errorMessage": "Unable to find existing entity",
                    "errorDetails": "No comment exists for: $testId"
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
