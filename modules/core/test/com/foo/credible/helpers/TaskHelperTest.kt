/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import com.anzi.credible.constants.TaskStatus
import com.anzi.credible.constants.TaskType
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.Task
import com.anzi.credible.entity.TaskUpdate
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskHelperTest {

    var helper = TaskHelper

    private val testUpdateTask = mockk<Task>()
    private val testSubmission = mockk<Submission>()
    private val testTask1 = mockk<Task>()
    private val testTask2 = mockk<Task>()

    @BeforeAll
    fun setUp() {
    }

    @BeforeEach
    fun common() {
        every { testSubmission.tasks } returns mutableListOf(testTask1, testTask2)
        every { testUpdateTask.submission } returns testSubmission
        every { testUpdateTask.getTaskType() } returns TaskType.FORMAL
        every { testUpdateTask.category } returns "decision"
        every { testUpdateTask.description } returns "test-description"
        every { testUpdateTask.getTaskStatus() } returns TaskStatus.PENDING
    }

    @Test
    fun testCreateTaskUpdateEntitiesNullStatus() {
        every { testTask1.getTaskType() } returns TaskType.FORMAL
        every { testTask1.getTaskStatus() } returns TaskStatus.APPROVED
        every { testTask2.getTaskType() } returns TaskType.INFORMAL
        every { testTask2.getTaskStatus() } returns TaskStatus.PENDING

        val updateList = helper.createTaskUpdateEntities(
            testUpdateTask,
            null,
            "added a note"
        )

        expect {
            that(updateList).isA<MutableList<TaskUpdate>>()
            that(updateList.size).isEqualTo(1)
            that(updateList[0].status).isNull()
            that(updateList[0].note).isEqualTo("added a note")
        }
    }

    @Test
    fun testCreateTaskUpdateEntities() {
        every { testTask1.getTaskType() } returns TaskType.FORMAL
        every { testTask1.getTaskStatus() } returns TaskStatus.APPROVED
        every { testTask2.getTaskType() } returns TaskType.INFORMAL
        every { testTask2.getTaskStatus() } returns TaskStatus.PENDING

        val updateList = helper.createTaskUpdateEntities(
            testUpdateTask,
            TaskStatus.REWORK,
            "test note"
        )

        expect {
            that(updateList).isA<MutableList<TaskUpdate>>()
            that(updateList.size).isEqualTo(3)
            that(updateList[0].getTaskStatus()).isEqualTo(TaskStatus.REWORK)
            that(updateList[1].getTaskStatus()).isEqualTo(TaskStatus.OUTDATED)
            that(updateList[2].getTaskStatus()).isEqualTo(TaskStatus.WITHDRAWN)
        }
    }

    @AfterEach
    fun reset() {
        // clearAllMocks()
    }

    @AfterAll
    fun tearDown() {
        unmockkAll()
    }
}
