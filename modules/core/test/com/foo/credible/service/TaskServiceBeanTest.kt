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
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.Task
import com.anzi.credible.helpers.TaskHelper
import com.anzi.credible.repository.SubmissionRepository
import com.anzi.credible.repository.TaskRepository
import com.anzi.credible.utils.DateUtils.toUTC
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
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskServiceBeanTest {

    @RelaxedMockK
    lateinit var userService: UserService

    @MockK
    lateinit var taskRepository: TaskRepository

    @MockK
    lateinit var submissionRepository: SubmissionRepository

    @InjectMockKs
    var testService = TaskServiceBean()

    private val testUser = mockk<AppUser>()
    private val testBorrower = spyk<Borrower>()
    private val testSubmission = spyk<Submission>()
    private val testTask = mockk<Task>()

    private val testId = UUID.randomUUID()
    private lateinit var testDto: TaskDto

    @BeforeAll
    fun setUp() {
        testDto = TaskDto(
            null, "1234",
            "Work", "WorkRequest",
            null, "test task", "OUTDATED", "2018-09-10 22:01:00",
            "2018-09-10 22:01:00", UserDto(testId, "creator"),
            UserDto(testId, "assignee"), true
        )

        testBorrower.apply {
            every { id } returns UUID.randomUUID()
            every { name } returns "test-borrower"
        }

        testSubmission.apply {
            every { id } returns UUID.randomUUID()
            every { borrower } returns testBorrower
        }
    }

    @BeforeEach
    fun common() {
        mockkObject(TaskHelper)

        testTask.apply {
            every { id } returns testId
            every { submission } returns testSubmission
            every { status } returns "pending"
            every { assignee } returns testUser
            every { type } returns "formal"
            every { category } returns "decision"
            every { description } returns "test-description"
            every { note } returns null
            every { due } returns "2021-01-22T01:53:42.000Z".toUTC()
            every { createTs } returns "2021-01-22T01:53:42.345Z".toUTC()
            every { updateTs } returns "2021-01-22T01:53:42.555Z".toUTC()
            every { createdBy } returns "test-user"
            every { flaggedUsers } returns null
            every { viewedUsers } returns null
        }

        testUser.apply {
            every { id } returns testId
            every { name } returns "test-user"
        }

        userService.apply {
            every { fetchLoggedUser() } returns testUser
            every { fetchUsersByLogin(any()) } returns listOf(testUser)
        }
    }

    @Test
    fun testCreateTaskSuccess() {
        val submission = mockk<Submission>()
        every { submissionRepository.fetchSubmissionById(ofType(String::class), any()) } returns
            submission
        every { taskRepository.createTask(ofType(Submission::class), testDto) } returns
            testTask

        every { TaskHelper.buildDto(ofType(Task::class), ofType(User::class)) } returns testDto

        val result = testService.createTask(testDto)

        verify(exactly = 1) {
            submissionRepository.fetchSubmissionById(testDto.submissionId!!)
            taskRepository.createTask(submission, testDto)
        }

        expect {
            that(result).isA<TaskDto>() and {
                get { status }.isEqualTo("pending")
            }
        }
    }

    @Test
    fun testCreateTaskFailure() {
        every { submissionRepository.fetchSubmissionById(ofType(String::class), any()) } returns
            mockk()
        every {
            taskRepository.createTask(
                ofType(Submission::class),
                testDto
            )
        } throws Exception("Error saving task to database")

        val result = testService.createTask(testDto)

        verify(exactly = 1) { submissionRepository.fetchSubmissionById(testDto.submissionId!!) }
        verify {
            taskRepository.createTask(ofType(Submission::class), testDto) wasNot Called
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.CREATE_ENTITY)
                get { errorDetails }.isEqualTo("Error saving task to database")
            }
        }
    }

    @Test
    fun testFetchTaskSuccess() {
        val updatedTestTask = mockk<Task>()
        updatedTestTask.apply {
            every { id } returns testId
            every { description } returns "test-description"
            every { viewedUsers } returns mutableSetOf(testUser)
        }

        testTask.apply {
            every { id } returns testId
            every { viewedUsers } returns mutableSetOf()
        }

        every { taskRepository.fetchTaskById(ofType(String::class)) } returns testTask
        every { taskRepository.updateTask(ofType(Task::class)) } returns updatedTestTask
        val result = testService.fetchTask(testId.toString())

        verify(exactly = 1) {
            userService.fetchLoggedUser()
            taskRepository.fetchTaskById(testId.toString())
            taskRepository.updateTask(testTask)
            TaskHelper.buildDto(testTask, testUser, testUser)
        }

        expect {
            that(result).isA<TaskDto>() and {
                get { taskId }.isEqualTo(testId)
                get { description }.isEqualTo("test-description")
            }
        }
    }

    @Test
    fun testFetchTaskFailure() {
        val testId = UUID.randomUUID()
        every { taskRepository.fetchTaskById(ofType(String::class)) } throws Exception("Database Error")

        val result = testService.fetchTask(testId.toString())

        verify {
            userService.fetchLoggedUser() wasNot Called
            taskRepository.fetchTaskById(testId.toString())
            taskRepository.updateTask(testTask) wasNot Called
            TaskHelper.buildDto(testTask, testUser) wasNot Called
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Task")
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { id }.isEqualTo(testId.toString())
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }

    @Test
    fun testFetchTasksSuccess() {
        every { testTask.id } returns testId
        every { taskRepository.fetchTasks(ofType(Boolean::class), any(), any()) } returns listOf(testTask)

        val result = testService.fetchTasks(true, null)

        verify(exactly = 1) {
            taskRepository.fetchTasks(true, testUser)
            TaskHelper.buildDto(testTask, testUser, testUser)
        }

        expect {
            that(result).isA<List<TaskDto>>() and {
                get { this[0].taskId }.isEqualTo(testId)
                get { this[0].description }.isEqualTo("test-description")
                get { this[0].status }.isEqualTo("pending")
                get { this[0].type }.isEqualTo("formal")
                get { this[0].category }.isEqualTo("decision")
                get { this[0].creator }.isA<UserDto>() and {
                    get { id }.isEqualTo(testId)
                    get { name }.isEqualTo("test-user")
                }
                get { this[0].due }.isEqualTo("2021-01-22T01:53:42.000Z")
            }
        }
    }

    @Test
    fun testFetchTasksCount() {
        val testId = UUID.randomUUID()
        every { testTask.id } returns testId
        every { testTask.due } returns "2035-01-22T01:53:42.000Z".toUTC()
        every { testTask.viewedUsers } returns mutableSetOf()
        every { taskRepository.fetchTasks(ofType(Boolean::class), any(), any()) } returns listOf(testTask)

        val result = testService.fetchTasks(true, COUNT)

        verify(exactly = 1) {
            taskRepository.fetchTasks(true, testUser)
            TaskHelper.buildDto(testTask, testUser) wasNot Called
        }

        expect {
            that(result).isA<Int>()
            that(result).isEqualTo(1)
        }
    }

    @Test
    fun testFetchTasksFailure() {
        val testId = UUID.randomUUID()
        every { testTask.id } returns testId
        every { taskRepository.fetchTasks(ofType(Boolean::class), any(), any()) } throws Exception("Database Error")

        val result = testService.fetchTasks(true, null)

        verify {
            TaskHelper.buildDto(testTask, testUser) wasNot Called
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { id }.isNull()
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
