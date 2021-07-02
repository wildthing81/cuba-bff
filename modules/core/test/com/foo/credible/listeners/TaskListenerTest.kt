/*
 * The code is copyright ©2021
 */

package com.foo.credible.listeners

import com.anzi.credible.constants.TaskCategory
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.entity.Activity
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Submission
import com.haulmont.cuba.core.entity.contracts.Id
import com.anzi.credible.entity.Task
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.UserService
import com.anzi.credible.utils.DateUtils.toUTC
import com.haulmont.cuba.core.app.events.EntityChangedEvent
import com.haulmont.cuba.core.global.DataManager
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.util.UUID

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskListenerTest {

    @RelaxedMockK
    lateinit var userService: UserService

    @MockK
    lateinit var txDM: DataManager

    @MockK
    lateinit var activityService: ActivityService

    @InjectMockKs
    var testListener = TaskListener()

    private val testDate = "2013-09-29T18:46:19.345Z"
    private val testInstitution = mockk<Institution>()
    private val testUser = mockk<AppUser>()
    private val testId = UUID.randomUUID()
    private val testSubmission = Submission()
    private val testTaskId = UUID.randomUUID()
    private val testTask = Task()

    @BeforeAll
    fun setUp() {
        testSubmission.apply {
            id = UUID.randomUUID()
            status = "approved"
            note = "test-note-1"
            createdBy = "test-user"
            createTs = testDate.toUTC()
        }
    }

    @BeforeEach
    fun common() {
        every { testInstitution.name } returns "anz"

        testUser.apply {
            every { testUser.id } returns testId
            every { testUser.name } returns "test-name"
            every { testUser.firstName } returns "test-firstname"
            every { testUser.lastName } returns "test-lastname"
            every { institution } returns testInstitution
        }

        testTask.apply {
            id = testTaskId
            submission = testSubmission
            setTaskCategory(TaskCategory.WORK)
        }

        every { userService.fetchUsersByLogin(any()) } returns listOf(testUser)
    }

    @Test
    fun testAfterTaskCreateSuccess() {
        val testActivity = mockk<Activity>()
        val testEvent = mockk<com.haulmont.cuba.core.app.events.EntityChangedEvent<Task, UUID>>()

        testActivity.apply {
            every { details } returns """{
                "type": "work",
                "message": "test-firstname test-lastname assigned a work task to test2-firstname test2-lastname 
                {action}",
                "timestamp": "2021-04-15T04:12:36.816Z",
                "action": {
                    "label": "View Work Task",
                    "payload": [
                        {
                            "key": "id",
                            "value": "$testTaskId"
                        }
                    ]
                },
                "priority": "success",
                "status": "Created"
            }"""
        }

        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.CREATED
            every { entityId } returns mockk<Id<Task, UUID>>()
        }

        every { txDM.load(ofType(Id::class)).view(ViewConstants.TASK_FETCH).one() } returns testTask
        every {
            activityService.addActivity(ofType(String::class), ofType(String::class))
        } returns testActivity

        val result = testListener.afterEntityCommit(testEvent)

        expect {
            that(result).isA<kotlin.Unit>()
        }
    }

    @Test
    fun testAfterTaskCreateFailure() {
        val testEvent = mockk<EntityChangedEvent<Task, UUID>>()

        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.CREATED
            every { entityId } returns mockk<Id<Task, UUID>>()
        }

        every {
            txDM.load(ofType(Id::class))
                .view(ViewConstants.TASK_FETCH).one()
        } throws Exception("Database error")

        val expected = assertThrows<Exception> { testListener.afterEntityCommit(testEvent) }

        expectThat(expected).get { message }.isEqualTo("Database error")
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
