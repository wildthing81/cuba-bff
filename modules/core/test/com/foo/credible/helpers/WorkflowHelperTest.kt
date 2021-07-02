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
import com.anzi.credible.constants.StepTrigger
import com.anzi.credible.entity.StepTransition
import com.anzi.credible.entity.Submission
import com.anzi.credible.entity.Workflow
import com.anzi.credible.entity.WorkflowStep
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkflowHelperTest {

    var helper = WorkflowHelper

    private val testWorkFlow = mockk<Workflow>()
    private val testSubmission = mockk<Submission>()
    private val testStep1 = mockk<WorkflowStep>()
    private val testStep2 = mockk<WorkflowStep>()

    @BeforeAll
    fun setUp() {
    }

    @BeforeEach
    fun common() {
        every { testWorkFlow.steps } returns mutableListOf(testStep1, testStep2)
        every { testStep1.index } returns 1

        every { testStep1.transitions } returns mutableListOf(
            mockk {
                every { toStepIndex } returns 2
                // every { trigger } returns "user-initiated"
                every { getStepTrigger() } returns StepTrigger.USER_INITIATED
                every { submissionStatus } returns "pending"
            },
            mockk {
                every { toStepIndex } returns 3
                // every { trigger } returns "rework"
                every { getStepTrigger() } returns StepTrigger.REWORK
                every { submissionStatus } returns "approved"
            }
        )

        every { testStep2.index } returns 2
        every { testSubmission.workflow } returns testWorkFlow
        every { testSubmission.workflowStep } returns 1
    }

    @Test
    fun testUITriggeredTransitionSuccess() {
        val transition = helper.isValidTransition(
            testSubmission,
            2,
            StepTrigger.USER_INITIATED
        )

        expect {
            that(transition).isA<StepTransition>() and {
                get { toStepIndex }.isEqualTo(2)
                get { submissionStatus }.isEqualTo("pending")
            }
        }
    }

    @Test
    fun testUITriggeredTransitionFailure() {
        val transition = helper.isValidTransition(
            testSubmission,
            3,
            StepTrigger.USER_INITIATED
        )

        expect {
            that(transition).isNull()
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
