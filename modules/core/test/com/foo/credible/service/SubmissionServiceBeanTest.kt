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
import com.anzi.credible.constants.AppConstants.COUNT
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.StepTrigger
import com.anzi.credible.constants.SubmissionStatus
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.dto.SectionDto
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.dto.WorkflowDto
import com.anzi.credible.entity.*
import com.anzi.credible.helpers.BorrowerHelper
import com.anzi.credible.helpers.InstitutionHelper
import com.anzi.credible.helpers.SubmissionHelper
import com.anzi.credible.helpers.SubmissionSectionsHelper
import com.anzi.credible.helpers.TaskHelper
import com.anzi.credible.helpers.WorkflowHelper
import com.anzi.credible.repository.BorrowerRepository
import com.anzi.credible.repository.SubmissionRepository
import com.anzi.credible.repository.TaskRepository
import com.anzi.credible.repository.WorkflowRepository
import com.anzi.credible.utils.AppUtils.displayRef
import com.anzi.credible.utils.DateUtils.toUTC
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.EntitySet
import com.haulmont.cuba.security.entity.User
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.isTrue

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubmissionServiceBeanTest {

    @MockK
    lateinit var userService: UserService

    @RelaxedMockK
    lateinit var submissionRepository: SubmissionRepository

    @RelaxedMockK
    lateinit var borrowerRepository: BorrowerRepository

    @MockK
    lateinit var workflowRepository: WorkflowRepository

    @MockK
    lateinit var taskRepository: TaskRepository

    @InjectMockKs
    var testService = SubmissionServiceBean()

    @MockK
    lateinit var notificationService: NotificationService

    private val testUser = mockk<AppUser>()
    private val testBorrower = spyk<Borrower>()
    private val testWorkflow = mockk<Workflow>()
    private val testInstitution = mockk<Institution>()

    private val testId = UUID.randomUUID()
    private val testDate = "2013-09-29T18:46:19.345Z"
    private val testSubmission = Submission()
    private val purposes = HashMap<String, String>()
    private lateinit var sections: List<SectionDto>

    @BeforeAll
    fun setUp() {
        sections = listOf(
            SectionDto(
                content = "The quick brown fox jumps over the lazy dog",
                slug = "",
                createdAt = "",
                updatedAt = " ",
                updatedBy = "test-user",
                comments = listOf()
            )
        )

        testBorrower.apply {
            every { id } returns testId
            every { name } returns "BHP"
        }

        testSubmission.apply {
            sections = mutableListOf(SubmissionSection().apply { updatedBy = "test-user" })
            borrower = testBorrower
            note = "test_note"
            due = testDate.toUTC()
            createTs = testDate.toUTC()
            displayRef = displayRef(3, 3)
        }
        testSubmission.setStatus(SubmissionStatus.DRAFTING)
        every { testInstitution.name } returns "anz"
    }

    @BeforeEach
    fun common() {
        mockkObject(SubmissionSectionsHelper)
        mockkObject(SubmissionHelper)
        mockkObject(InstitutionHelper)
        mockkObject(WorkflowHelper)
        mockkObject(BorrowerHelper)
        mockkObject(TaskHelper)

        every { userService.fetchLoggedUser() } returns testUser
        every { userService.fetchUsersByLogin(any()) } returns listOf(testUser)

        testUser.apply {
            every { id } returns testId
            every { name } returns "John Cave"
            every { firstName } returns "John"
            every { lastName } returns "Cave"
            every { institution } returns testInstitution
            every { position } returns "rm"
            every { cadLevel } returns 15000
        }

        testWorkflow.apply {
            every { id } returns testId
            every { name } returns "test-workflow"
            every { steps } returns mutableListOf()
        }

        every {
            SubmissionSectionsHelper.createDefaultSections(
                ofType(JsonNode::class),
                ofType(JsonNode::class),
                ofType(JsonNode::class),
                ofType(User::class)
            )
        } returns sections

        every {
            SubmissionSectionsHelper.buildDto(ofType(SubmissionSection::class), ofType(User::class))
        } returns sections[0]

        every {
            SubmissionHelper.createSubmissionPurposes(ofType(JsonNode::class), ofType(SubmissionDto::class))
        } returns purposes

        every { InstitutionHelper.getTemplates(ofType(Institution::class)) } returns mockk()
        every { InstitutionHelper.getTypes(ofType(Institution::class)) } returns mockk()
        every { InstitutionHelper.getExceptions(ofType(Institution::class)) } returns mockk()

        every {
            workflowRepository.fetchWorkFlowBySubmissionType(
                ofType(String::class),
                ViewConstants.WORKFLOW_FETCH
            )
        } returns testWorkflow
    }

    @Test
    fun testCreateSubmissionSuccess() {
        val testUserIdCarl = UUID.randomUUID()
        val testUserCarl = mockk<AppUser>()
        testUserCarl.apply {
            every { id } returns testUserIdCarl
            every { name } returns "Carl Credit"
            every { institution } returns testInstitution
            every { position } returns "co"
            every { cadLevel } returns 2500
        }

        val testDto = SubmissionDto(
            null,
            testBorrower.id.toString(),
            List(1) { "test_type" },
            List(1) { "test_purpose" },
            "2020-12-14T00:40:09Z",
            "test_note",
            "Open",
            team = mutableSetOf(testUserCarl.id.toString())
        )

        val testSubscriber = mockk<Subscriber>()
        testSubscriber.apply {
            every { user } returns testUserCarl
        }
        val testNotification = mockk<Notification>()
        testNotification.apply {
            every { text } returns "A new submission was created and you were added to its team"
            every { institution } returns testInstitution
            every { subscribers } returns mutableListOf(testSubscriber)
        }
        every {
            userService.fetchUsersById(any())
        } returns listOf(testUser)

        every {
            notificationService.createNotification(ofType(NotificationDto::class), any())
        } returns testNotification

        testSubmission.workflow = testWorkflow
        every {
            submissionRepository.createSubmission(
                ofType(SubmissionDto::class),
                purposes,
                sections,
                ofType(Institution::class),
                ofType(Workflow::class)
            )
        } returns testSubmission

        every {
            TaskHelper.buildDto(ofType(Task::class), testUser)
        } returns mockk()

        val result = testService.createSubmission(testDto)

        verifyOrder {
            borrowerRepository.fetchBorrowerById(testDto.borrower!! as String)
            InstitutionHelper.getTemplates(testInstitution)
            InstitutionHelper.getExceptions(testInstitution)
            InstitutionHelper.getTypes(testInstitution)
            SubmissionHelper.createSubmissionPurposes(ofType(JsonNode::class), testDto)
            submissionRepository.createSubmission(testDto, purposes, sections, testInstitution, testWorkflow)
            SubmissionHelper.buildDto(testSubmission, sections, testUser, testUser)
        }

        expect {
            that(result).isA<SubmissionDto>() and {
                get { sections }.isEqualTo(sections)
                get { workflow }.isA<WorkflowDto>() and {
                    get { id }.isEqualTo(testId)
                    get { name }.isEqualTo("test-workflow")
                }
                get { """[A-Z]{3}-\d{3}""".toRegex().matches(displayRef!!) }.isTrue()
            }
        }
    }

    @Test
    fun testCreateSubmissionFailure() {
        every {
            submissionRepository.createSubmission(
                ofType(SubmissionDto::class),
                purposes,
                sections,
                ofType(Institution::class),
                ofType(Workflow::class)
            )
        } throws Exception("Error saving submission to database")

        val testDto = SubmissionDto(
            null,
            testBorrower.id.toString(),
            List(1) { "test_type" },
            List(1) { "test_purpose" },
            "2020-12-14T00:40:09Z",
            "test_note", "Open",
            3,
            null, null,
            null,
            mutableSetOf(UserDto(UUID.randomUUID()))
        )

        val testSubscriber = mockk<Subscriber>()
        testSubscriber.apply {
            every { user } returns testUser
        }
        val testNotification = mockk<Notification>()
        testNotification.apply {
            every { text } returns "A submission is assigned to me"
            every { institution } returns testInstitution
            every { subscribers } returns mutableListOf(testSubscriber)
        }
        every {
            userService.fetchUsersById(any())
        } returns listOf(testUser)

        every {
            notificationService.createNotification(ofType(NotificationDto::class), any())
        } returns testNotification

        val result = testService.createSubmission(testDto)

        verifyAll {
            borrowerRepository.fetchBorrowerById(testDto.borrower!! as String)
            submissionRepository.createSubmission(
                testDto,
                purposes,
                sections,
                testInstitution,
                testWorkflow
            )
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.CREATE_ENTITY)
                get { errorDetails }.isEqualTo("Error saving submission to database")
            }
        }
    }

    @Test
    fun testFetchSubmissionSuccess() {
        testSubmission.id = testId
        testSubmission.tasks = mutableListOf()
        testSubmission.viewedUsers = mutableSetOf()
        every { submissionRepository.fetchSubmissionById(ofType(String::class), any()) } returns testSubmission

        val result = testService.fetchSubmission(testId.toString())

        verifyOrder {
            submissionRepository.fetchSubmissionById(testId.toString(), ViewConstants.SUBMISSION_FETCH)
            submissionRepository.updateSubmission(ofType(CommitContext::class))
            SubmissionSectionsHelper.buildDto(testSubmission.sections[0], testUser)
            SubmissionHelper.buildDto(testSubmission, sections, testUser, testUser)
        }

        expect {
            that(result).isA<SubmissionDto>() and {
                get { id }.isEqualTo(testId)
                get { (borrower as BorrowerDto).id }.isEqualTo(testBorrower.id)
                get { (borrower as BorrowerDto).name }.isEqualTo(testBorrower.name)
                get { sections }.isEqualTo(sections)
                get { note }.isEqualTo("test_note")
                get { status }.isEqualTo("drafting")
                get { due }.isEqualTo(testDate)
                get { creator }.isA<UserDto>() and {
                    get { id }.isEqualTo(testId)
                    get { name }.isEqualTo("John Cave")
                }
            }
        }
    }

    @Test
    fun testFetchSubmissionFailure() {
        every { submissionRepository.fetchSubmissionById(testId.toString(), any()) } throws Exception(
            "Invalid " +
                "Submission"
        )

        val result = testService.fetchSubmission(testId.toString())

        verify(exactly = 1) {
            submissionRepository.fetchSubmissionById(testId.toString())?.wasNot(Called)
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { id }.isEqualTo(testId.toString())
                get { errorDetails }.isEqualTo("Invalid Submission")
            }
        }
    }

    @Test
    fun testFetchAssignedSubmissionsSuccess() {
        val testSubmissionId = UUID.randomUUID()
        val testLongFutureDate = "2100-01-01T00:00:00.015Z"
        val testNoDueSubmission = Submission()
        val testWorkflowStep = mockk<WorkflowStep>()
        testWorkflowStep.apply {
            every { id } returns UUID.randomUUID()
            every { name } returns "test-workflow-step"
            every { index } returns 1
            every { workflow } returns testWorkflow
        }
        every { testWorkflow.steps } returns mutableListOf(testWorkflowStep)

        testNoDueSubmission.apply {
            sections = mutableListOf(SubmissionSection().apply { updatedBy = "test-user" })
            borrower = testBorrower
            note = "test_note"
            createTs = testDate.toUTC()
            workflow = testWorkflow
            workflowStep = 1
            due = testLongFutureDate.toUTC()
            id = testSubmissionId
            tasks = mutableListOf(
                mockk {
                    every { getTaskStatus() } returns TaskStatus.OUTDATED
                },
                mockk {
                    every { getTaskStatus() } returns TaskStatus.PENDING
                }
            )
        }
        testNoDueSubmission.setStatus(SubmissionStatus.DRAFTING)

        val loggedUser = mockk<AppUser>()
        every { userService.fetchLoggedUser() } returns loggedUser

        every { submissionRepository.fetchAssignedSubmissions(any()) } returns listOf(testNoDueSubmission)

        val result = testService.fetchAssignedSubmissions(null)

        verifyOrder {
            submissionRepository.fetchAssignedSubmissions(loggedUser)
            SubmissionHelper.buildDtoForList(testNoDueSubmission, loggedUser)
        }

        expect {
            that(result).isA<List<SubmissionDto>>() and {
                get { this[0].id }.isEqualTo(testSubmissionId)
                get { (this[0].borrower as BorrowerDto).id }.isEqualTo(testBorrower.id)
                get { (this[0].borrower as BorrowerDto).name }.isEqualTo(testBorrower.name)
                get { this[0].sections }.isNull()
                get { this[0].note }.isEqualTo("test_note")
                get { this[0].status }.isEqualTo("drafting")
                get { this[0].due }.isEqualTo(testLongFutureDate)
                get { this[0].createdAt }.isEqualTo(testDate)
                get { this[0].actions?.action }.isEqualTo("1 task overdue")
                get { this[0].actions?.status }.isEqualTo("Overdue")
            }
        }
    }

    @Test
    fun testFetchAssignedSubmissionsCount() {
        val loggedUser = mockk<AppUser>()
        val testSubmissionOverDue = Submission()
        testSubmissionOverDue.id = UUID.randomUUID()
        testSubmissionOverDue.due = "2042-01-29T18:46:19.345Z".toUTC()
        testSubmissionOverDue.viewedUsers = mutableSetOf(loggedUser)

        testSubmission.id = testId
        testSubmission.viewedUsers = mutableSetOf()
        every { userService.fetchLoggedUser() } returns loggedUser
        every { submissionRepository.fetchAssignedSubmissions(ofType(User::class)) } returns
            listOf(testSubmission, testSubmissionOverDue)

        val result = testService.fetchAssignedSubmissions(COUNT)

        verify(exactly = 1) {
            submissionRepository.fetchAssignedSubmissions(loggedUser)
        }

        verify { SubmissionHelper.buildDtoForList(testSubmission, loggedUser) wasNot Called }

        expect {
            that(result).isA<Int>()
            that(result).isEqualTo(1)
        }
    }

    @Test
    fun testFetchAssignedSubmissionsFailure() {
        testSubmission.id = testId

        val loggedUser = mockk<AppUser>()
        every { userService.fetchLoggedUser() } returns loggedUser

        every { submissionRepository.fetchAssignedSubmissions(ofType(User::class)) } throws Exception("Database Error")

        val result = testService.fetchAssignedSubmissions(null)

        verify(exactly = 1) {
            submissionRepository.fetchAssignedSubmissions(loggedUser)
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { id }.isEqualTo("")
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }

    @Test
    fun testFetchWatchedSubmissionsSuccess() {
        val testSubmissionId = UUID.randomUUID()
        val testWorkflowStep = mockk<WorkflowStep>()
        val testLongFutureDate = "2100-01-01T00:00:00.015Z"
        val testNoDueSubmission = Submission()
        testWorkflowStep.apply {
            every { id } returns UUID.randomUUID()
            every { name } returns "test-workflow-step"
            every { index } returns 1
            every { workflow } returns testWorkflow
        }
        every { testWorkflow.steps } returns mutableListOf(testWorkflowStep)

        testNoDueSubmission.apply {
            sections = mutableListOf(SubmissionSection().apply { updatedBy = "test-user" })
            borrower = testBorrower
            note = "test_note"
            due = testDate.toUTC()
            createTs = testDate.toUTC()
            workflow = testWorkflow
            workflowStep = 1
            due = testLongFutureDate.toUTC()
            id = testSubmissionId
            tasks = mutableListOf(
                mockk {
                    every { getTaskStatus() } returns TaskStatus.PENDING
                },
                mockk {
                    every { getTaskStatus() } returns TaskStatus.PENDING
                }
            )
        }
        testNoDueSubmission.setStatus(SubmissionStatus.DRAFTING)

        val loggedUser = mockk<AppUser>()
        every { userService.fetchLoggedUser() } returns loggedUser

        every { submissionRepository.fetchWatchedSubmissions(ofType(User::class)) } returns listOf(testNoDueSubmission)

        val result = testService.fetchWatchedSubmissions(null)

        verifyOrder {
            submissionRepository.fetchWatchedSubmissions(loggedUser)
            SubmissionHelper.buildDtoForList(testNoDueSubmission, loggedUser)
        }

        expect {
            that(result).isA<List<SubmissionDto>>() and {
                get { this[0].id }.isEqualTo(testSubmissionId)
                get { (this[0].borrower as BorrowerDto).id }.isEqualTo(testBorrower.id)
                get { (this[0].borrower as BorrowerDto).name }.isEqualTo(testBorrower.name)
                get { this[0].sections }.isNull()
                get { this[0].note }.isEqualTo("test_note")
                get { this[0].status }.isEqualTo("drafting")
                get { this[0].due }.isEqualTo(testLongFutureDate)
                get { this[0].createdAt }.isEqualTo(testDate)
                get { this[0].actions?.action }.isEqualTo("2 tasks pending")
                get { this[0].actions?.status }.isEqualTo("Pending")
            }
        }
    }

    @Test
    fun testFetchWatchedSubmissionsCount() {
        testSubmission.id = testId

        val loggedUser = mockk<AppUser>()
        every { userService.fetchLoggedUser() } returns loggedUser

        every { submissionRepository.fetchWatchedSubmissions(ofType(User::class)) } returns listOf(testSubmission)

        val result = testService.fetchWatchedSubmissions(COUNT)

        verify(exactly = 1) {
            submissionRepository.fetchWatchedSubmissions(loggedUser)
        }

        verify { SubmissionHelper.buildDtoForList(testSubmission, loggedUser) wasNot Called }

        expect {
            that(result).isA<Int>()
            that(result).isEqualTo(1)
        }
    }

    @Test
    fun testFetchWatchedSubmissionsFailure() {
        testSubmission.id = testId

        val loggedUser = mockk<AppUser>()
        every { userService.fetchLoggedUser() } returns loggedUser

        every { submissionRepository.fetchWatchedSubmissions(ofType(User::class)) } throws Exception("Database Error")

        val result = testService.fetchWatchedSubmissions(null)

        verify(exactly = 1) {
            submissionRepository.fetchWatchedSubmissions(loggedUser)
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { id }.isEqualTo("")
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }

    @Test
    fun testValidStepTransition() {
        val testTask1 = mockk<Task>()
        val testTask2 = mockk<Task>()
        val testTaskUpdate1 = mockk<TaskUpdate>()
        val testTaskUpdates = MutableList(1) { testTaskUpdate1 }
        testSubmission.id = UUID.randomUUID()
        testSubmission.tasks = mutableListOf(testTask1, testTask2)
        val refreshTransition = StepTransition()
        refreshTransition.borrowerRefresh = true
        refreshTransition.setStatus(SubmissionStatus.APPROVED)

        every { testTaskUpdate1.task } returns testTask1
        every { testTask1.getTaskStatus() } returns TaskStatus.APPROVED
        every { testTask2.getTaskStatus() } returns TaskStatus.PENDING
        every { testTask1.taskUpdates } returns testTaskUpdates

        every { submissionRepository.fetchSubmissionById(ofType(String::class), any()) } returns testSubmission
        every { submissionRepository.updateSubmission(ofType(CommitContext::class)) } returns mockk()
        every { borrowerRepository.updateBorrower(ofType(Borrower::class)) } returns mockk()
        every { BorrowerHelper.refreshBorrowerDefaults(ofType(Submission::class)) } returns testBorrower
        every {
            WorkflowHelper.isValidTransition(
                ofType(Submission::class),
                ofType(Int::class),
                ofType(StepTrigger::class)
            )
        } returns refreshTransition
        every { taskRepository.createTaskUpdates(ofType(List::class) as List<TaskUpdate>) } returns mockk<EntitySet>()
        every {
            TaskHelper.createTaskUpdateEntities(ofType(Task::class), ofType(TaskStatus::class), ofType(String::class))
        } returns testTaskUpdates
        val result = testService.actionStepTransition(testId.toString(), 2)

        verify(exactly = 1) {
            submissionRepository.fetchSubmissionById(testId.toString(), ViewConstants.SUBMISSION_WORKFLOW)
            submissionRepository.updateSubmission(ofType(CommitContext::class))
            borrowerRepository.updateBorrower(testBorrower)
        }

        expect {
            that(result).isA<Boolean>()
            that(result).isEqualTo(true)
            that(testSubmission.getStatus()).isEqualTo(SubmissionStatus.APPROVED)
        }
        // revert the change
        testSubmission.setStatus(SubmissionStatus.DRAFTING)
    }

    @Test
    fun testInvalidStepTransition() {
        testSubmission.id = testId
        val transition = StepTransition()
        transition.borrowerRefresh = false

        every { submissionRepository.fetchSubmissionById(ofType(String::class), any()) } returns testSubmission
        every { submissionRepository.updateSubmission(ofType(CommitContext::class)) } returns mockk()
        every { borrowerRepository.updateBorrower(ofType(Borrower::class)) } returns mockk()
        every { BorrowerHelper.refreshBorrowerDefaults(ofType(Submission::class)) } returns testBorrower
        every {
            WorkflowHelper.isValidTransition(
                ofType(Submission::class),
                ofType(Int::class),
                ofType
                (StepTrigger::class)
            )
        } returns null

        val result = testService.actionStepTransition(testId.toString(), 2)

        verify(exactly = 1) {
            submissionRepository.fetchSubmissionById(testId.toString(), ViewConstants.SUBMISSION_WORKFLOW)
        }
        verify(exactly = 0) {
            submissionRepository.updateSubmission(CommitContext(testSubmission))
            borrowerRepository.updateBorrower(testBorrower)
        }

        expect {
            that(result).isA<Boolean>()
            that(result).isEqualTo(false)
        }
    }

    @Test
    fun testActionStepTransitionFailure() {
        val refreshTransition = StepTransition()
        refreshTransition.borrowerRefresh = true
        refreshTransition.setStatus(SubmissionStatus.PENDING)
        every { submissionRepository.fetchSubmissionById(ofType(String::class), any()) } returns testSubmission
        every {
            WorkflowHelper.isValidTransition(
                ofType(Submission::class),
                ofType(Int::class),
                ofType(StepTrigger::class)
            )
        } returns refreshTransition
        every {
            submissionRepository.updateSubmission(ofType(CommitContext::class))
        } throws Exception("Error updating submission to database")

        val result = testService.actionStepTransition(testId.toString(), 2)

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Submission")
                get { id }.isEqualTo(testId.toString())
                get { errorMessage }.isEqualTo(ErrorConstants.ERR_STEP_TRANSITION)
                get { errorDetails }.isEqualTo("Error updating submission to database")
            }
        }
    }

    @Test
    fun testFlaggedForUserSuccess() {
        every { submissionRepository.fetchSubmissionById(testId.toString()) } returns testSubmission

        val result = testService.flaggedForUser(testId.toString(), true)
        val updatedFlaggedUsers = testSubmission.flaggedUsers
        verifyOrder {
            submissionRepository.fetchSubmissionById(testId.toString())
            userService.fetchLoggedUser()
        }

        expect {
            that(result).isA<EntitySet>()
            that(updatedFlaggedUsers.size).isEqualTo(1)
            that(updatedFlaggedUsers).isEqualTo(mutableSetOf(testUser))
        }

        // revert back update
        testSubmission.flaggedUsers = mutableSetOf()
    }

    @Test
    fun testFlaggedForUserFailure() {
        every { submissionRepository.fetchSubmissionById(testId.toString()) } returns testSubmission
        every {
            submissionRepository.updateSubmission(ofType(CommitContext::class))
        } throws Exception("Error updating submission to database")

        val result = testService.flaggedForUser(testId.toString(), true)

        verifyOrder {
            submissionRepository.fetchSubmissionById(testId.toString())
            userService.fetchLoggedUser()
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.UPDATE_ENTITY)
                get { errorDetails }.isEqualTo("Error updating submission to database")
            }
        }
    }

    @Test
    fun testFlaggedForUserForIncorrectSubmissionId() {
        every { submissionRepository.fetchSubmissionById(testId.toString()) } returns null
        val result = testService.flaggedForUser(testId.toString(), true)

        verifyOrder {
            submissionRepository.fetchSubmissionById(testId.toString())
        }

        expect {
            that(result).isNull()
        }
    }

    @Test
    fun testUpdateSubmissionTeamSuccess() {
        val testUserId = UUID.randomUUID()
        val updateJson = """{
	        "team": [
                "$testUserId"
            ]
        }"""
        val testSubmissionDto = ObjectMapper().registerKotlinModule().readValue(updateJson, SubmissionDto::class.java)

        every { submissionRepository.fetchSubmissionById(testId.toString()) } returns testSubmission
        every { userService.fetchUsersById(any()) } returns listOf(testUser)

        val testSubscriber = mockk<Subscriber>()
        testSubscriber.apply {
            every { user } returns testUser
        }
        val testNotification = mockk<Notification>()
        testNotification.apply {
            every { text } returns "A new submission was created and you were added to its team"
            every { institution } returns testInstitution
            every { subscribers } returns mutableListOf(testSubscriber)
        }
        every {
            userService.fetchUsersById(any())
        } returns listOf(testUser)

        every {
            notificationService.createNotification(ofType(NotificationDto::class), any())
        } returns testNotification

        val result = testService.updateSubmission(testId.toString(), testSubmissionDto)

        verifyOrder {
            submissionRepository.fetchSubmissionById(testId.toString())
            userService.fetchUsersById(listOf("$testUserId"))
            submissionRepository.updateSubmission(ofType(CommitContext::class))
        }

        expect {
            that(result).isA<CommitContext>()
        }
        // revert back update
        testSubmission.team = mutableSetOf()
    }

    @Test
    fun testUpdateSubmissionTeamFailure() {
        val updateJson = """{
	        "team": [
                "facddb17-227a-b813-797c-1e61d25d57c6"
            ]
        }"""
        val testSubmissionDto = ObjectMapper().registerKotlinModule().readValue(updateJson, SubmissionDto::class.java)
        every { submissionRepository.fetchSubmissionById(testId.toString()) } returns testSubmission
        every { userService.fetchUsersById(any()) } returns listOf(testUser)
        every {
            submissionRepository.updateSubmission(ofType(CommitContext::class))
        } throws Exception("Error updating submission to database")

        val testSubscriber = mockk<Subscriber>()
        testSubscriber.apply {
            every { user } returns testUser
        }
        val testNotification = mockk<Notification>()
        testNotification.apply {
            every { text } returns "A submission is assigned to me"
            every { institution } returns testInstitution
            every { subscribers } returns mutableListOf(testSubscriber)
        }
        every {
            userService.fetchUsersById(any())
        } returns listOf(testUser)

        every {
            notificationService.createNotification(ofType(NotificationDto::class), any())
        } returns testNotification

        val result = testService.updateSubmission(testId.toString(), testSubmissionDto)

        verify(exactly = 1) {
            submissionRepository.updateSubmission(ofType(CommitContext::class))
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.UPDATE_ENTITY)
                get { errorDetails }.isEqualTo("Error updating submission to database")
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
