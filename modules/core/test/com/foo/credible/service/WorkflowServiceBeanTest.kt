/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.ViewConstants.WORKFLOW_FETCH
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Workflow
import com.anzi.credible.helpers.WorkflowHelper
import com.anzi.credible.repository.WorkflowRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.haulmont.cuba.security.entity.User
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowServiceBeanTest {

    @RelaxedMockK
    lateinit var userService: UserService

    @MockK
    lateinit var workflowRepository: WorkflowRepository

    @InjectMockKs
    var testService = WorkflowServiceBean()

    private lateinit var testDto: WorkflowDto
    private val testId = UUID.randomUUID()

    private val testWorkFlow = mockk<Workflow>()
    private val testInstitution = mockk<Institution>()
    private val testUser = mockk<AppUser>()

    @BeforeAll
    fun setUp() {
        testDto = ObjectMapper().registerKotlinModule().readValue(
            """{
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
        }""",
            WorkflowDto::class.java
        )
        every { testInstitution.name } returns "anz"
    }

    @BeforeEach
    fun common() {
        mockkObject(WorkflowHelper)

        every { userService.fetchLoggedUser() } returns testUser
        every { userService.fetchUsersByLogin(any()) } returns listOf(testUser)

        every { testUser.id } returns UUID.randomUUID()
        every { testUser.institution } returns testInstitution

        every { testWorkFlow.createdBy } returns "test-user"
        every {
            WorkflowHelper.buildDto(
                ofType(Workflow::class),
                ofType(User::class)
            )
        } returns testDto.copy(id = testId)
    }

    @Test
    fun testCreateWorkFlowSuccess() {
        every { testWorkFlow.id } returns testId
        every { workflowRepository.createWorkFlow(testDto, ofType(Institution::class)) } returns testWorkFlow

        val result = testService.createWorkFlow(testDto)

        verify(exactly = 1) {
            workflowRepository.createWorkFlow(testDto, testInstitution)
        }

        expect {
            that(result).isA<WorkflowDto>() and {
                get { id }.isEqualTo(testId)
                get { initialStatus }.isEqualTo("drafting")
                get { name }.isEqualTo("test-workflow")
            }
        }
    }

    @Test
    fun testCreateWorkFlowFailure() {
        every { workflowRepository.createWorkFlow(testDto, ofType(Institution::class)) } throws
            Exception("Error creating new Workflow")

        val result = testService.createWorkFlow(testDto)

        verify(exactly = 1) {
            workflowRepository.createWorkFlow(testDto, ofType(Institution::class))
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Workflow")
                get { errorMessage }.isEqualTo(ErrorConstants.CREATE_ENTITY)
                get { errorDetails }.isEqualTo("Error creating new Workflow")
            }
        }
    }

    @Test
    fun testFetchWorkFlowsSuccess() {
        every { workflowRepository.fetchWorkFlows(ofType(Institution::class)) } returns listOf(testWorkFlow)

        val result = testService.fetchWorkFlows()

        verify(exactly = 1) {
            workflowRepository.fetchWorkFlows(testInstitution, WORKFLOW_FETCH)
            userService.fetchUsersByLogin(listOf("test-user"))
            WorkflowHelper.buildDto(testWorkFlow, testUser)
        }

        expect {
            that(result).isA<List<WorkflowDto>>() and {
                get { this[0].initialStatus }.isEqualTo("drafting")
                get { this[0].name }.isEqualTo("test-workflow")
                get { this[0].steps?.size }.isEqualTo(1)
            }
        }
    }

    @Test
    fun testFetchWorkFlowsFailure() {
        every { workflowRepository.fetchWorkFlows(ofType(Institution::class)) } throws
            Exception("Database Error")

        val result = testService.fetchWorkFlows()

        verify(exactly = 1) {
            workflowRepository.fetchWorkFlows(testInstitution, WORKFLOW_FETCH)
        }
        verify {
            WorkflowHelper.buildDto(ofType(Workflow::class), ofType(AppUser::class)) wasNot Called
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Workflow")
                get { errorMessage }.isEqualTo(ErrorConstants.ERR_FETCH_WORKFLOWS)
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
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
