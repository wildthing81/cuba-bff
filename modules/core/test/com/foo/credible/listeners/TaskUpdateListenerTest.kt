/*
 * The code is copyright ©2021
 */

package com.foo.credible.listeners

import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.entity.Task
import com.anzi.credible.entity.TaskUpdate
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.UserService
import com.haulmont.cuba.core.TransactionalDataManager
import com.haulmont.cuba.core.app.events.EntityChangedEvent
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.entity.contracts.Id
import com.haulmont.cuba.core.global.DataManager
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskUpdateListenerTest {

    @RelaxedMockK
    lateinit var userService: UserService

    @RelaxedMockK
    lateinit var dataManager: DataManager

    @MockK
    lateinit var txDM: TransactionalDataManager

    @MockK
    lateinit var activityService: ActivityService

    @InjectMockKs
    var testListener = TaskUpdateListener()

    private val testTaskUpdate = mockk<TaskUpdate>()
    private val testTask = Task()

    @BeforeAll
    fun setUp() {
        testTask.apply {
            status = "pending"
            type = "formal"
            category = "decision"
            description = "test-description"
        }
    }

    @BeforeEach
    fun common() {
        testTaskUpdate.apply {
            every { getTaskStatus() } returns TaskStatus.APPROVED
            every { note } returns "test-note"
            every { task } returns testTask
        }

        txDM.apply {
            every { save(ofType(Entity::class)) } answers { firstArg() }
        }
    }

    @Test
    fun testBeforeTaskUpdateCommitUpdateTask() {
        val testEvent = mockk<EntityChangedEvent<TaskUpdate, UUID>>()
        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.CREATED
            every { entityId } returns mockk<Id<TaskUpdate, UUID>>()
        }

        every { txDM.load(ofType(Id::class)).view(ViewConstants.TASKUPDATE_FETCH).one() } returns testTaskUpdate

        testListener.beforeEntityCommit(testEvent)

        verify { txDM.save(testTask) }

        expectThat(
            testTask,
            {
                get { status }.isEqualTo("approved")
                get { note }.isEqualTo("test-note")
            }
        )
    }

    @Test
    fun testBeforeTaskUpdateCommitNullStatus() {
        val testEvent = mockk<EntityChangedEvent<TaskUpdate, UUID>>()
        every { testTaskUpdate.getTaskStatus() } returns null

        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.CREATED
            every { entityId } returns mockk<Id<TaskUpdate, UUID>>()
        }

        every { txDM.load(ofType(Id::class)).view(ViewConstants.TASKUPDATE_FETCH).one() } returns testTaskUpdate

        testListener.beforeEntityCommit(testEvent)

        verify(exactly = 0) { txDM.save(testTask) }

        expectThat(testTask).get { status }.isEqualTo("pending")
    }

    @Test
    fun testBeforeTaskUpdateCommitFailure() {
        val testEvent = mockk<EntityChangedEvent<TaskUpdate, UUID>>()

        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.CREATED
            every { entityId } returns mockk<Id<TaskUpdate, UUID>>()
        }

        every {
            txDM.load(ofType(Id::class))
                .view(ViewConstants.TASKUPDATE_FETCH).one()
        } throws Exception("Database error")

        val expected = assertThrows<Exception> { testListener.beforeEntityCommit(testEvent) }

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
