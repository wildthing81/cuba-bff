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
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.entity.Activity
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Submission
import com.anzi.credible.helpers.UserHelper
import com.anzi.credible.repository.ActivityRepository
import com.anzi.credible.service.ActivityService
import com.anzi.credible.service.UserService
import com.anzi.credible.utils.DateUtils.toUTC
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.core.app.events.EntityChangedEvent
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
class SubmissionListenerTest {

    @RelaxedMockK
    lateinit var userService: UserService

    @MockK
    lateinit var txDM: DataManager

    @RelaxedMockK
    lateinit var activityRepository: ActivityRepository

    @InjectMockKs
    var submissionListener = SubmissionListener()

    @MockK
    lateinit var activityService: ActivityService

    private val testDate = "2013-09-29T18:46:19.345Z"
    private val testInstitution = mockk<Institution>()
    private val testUser = mockk<AppUser>()
    private val testId = UUID.randomUUID()
    private val testSubmission = Submission()

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

        every { userService.fetchUsersByLogin(any()) } returns listOf(testUser)
    }

    @Test
    fun testAfterSubmissionCreateSuccess() {
        val testEvent = mockk<EntityChangedEvent<Submission, UUID>>()
        val testActivity = mockk<Activity>()
        val json = ObjectMapper().createObjectNode()
            .put("type", "submission")
            .put("timestamp", testDate)
            .put(
                "message",
                AppConstants.ACTIVITY_SUBMISSION_CREATE.format(
                    UserHelper.userFullName(testUser)
                )
            )
        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.CREATED
            every { entityId } returns mockk<Id<Submission, UUID>>()
        }

        testActivity.apply {
            every { details } returns """{
                        "type" : "submission",
                        "timestamp" : "2013-09-29T18:46:19.345Z",
                        "message" : "test-firstname test-lastname created this submission"
                    }"""
        }

        every { txDM.load(ofType(Id::class)).view(ViewConstants.SUBMISSION_FETCH).one() } returns testSubmission

        every {
            activityService.addActivity(ofType(String::class), ofType(String::class))
        } returns testActivity

        submissionListener.afterEntityCommit(testEvent)

        verify(exactly = 1) {
            activityService.addActivity(testSubmission.id.toString(), json.toPrettyString())
        }
    }

    @Test
    fun testAfterSubmissionCreateFailure() {
        val testEvent = mockk<com.haulmont.cuba.core.app.events.EntityChangedEvent<Submission, UUID>>()

        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.CREATED
            every { entityId } returns mockk<Id<Submission, UUID>>()
        }

        every {
            txDM.load(ofType(Id::class)).view(ViewConstants.SUBMISSION_FETCH).one()
        } throws Exception("Database error")

        val expected = assertThrows<Exception> { submissionListener.afterEntityCommit(testEvent) }

        expectThat(expected).get { message }.isEqualTo("Database error")
    }

    @Test
    fun testAfterSubmissionUpdateSuccess() {
        val testEvent = mockk<com.haulmont.cuba.core.app.events.EntityChangedEvent<Submission, UUID>>()
        val testActivity = mockk<Activity>()
        val json = ObjectMapper().createObjectNode()
            .put("type", "submission")
            .put("timestamp", "2013-09-30T10:26:12.123Z")
            .put(
                "message",
                AppConstants.ACTIVITY_SUBMISSION_UPDATE.format(
                    UserHelper.userFullName(testUser)
                )
            )
        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.UPDATED
            every { entityId } returns mockk<Id<Submission, UUID>>()
        }

        testActivity.apply {
            every { details } returns """{
                        "type" : "submission",
                        "timestamp" : "2013-09-30T10:26:12.123Z",
                        "message" : "test-firstname test-lastname updated this submission"
                    }"""
        }

        every { txDM.load(ofType(Id::class)).view(ViewConstants.SUBMISSION_FETCH).one() } returns testSubmission

        testSubmission.apply {
            updatedBy = testUser.id.toString()
            updateTs = "2013-09-30T10:26:12.123Z".toUTC()
        }
        every {
            activityService.addActivity(ofType(String::class), ofType(String::class))
        } returns testActivity

        submissionListener.afterEntityCommit(testEvent)

        verify(exactly = 1) {
            activityService.addActivity(testSubmission.id.toString(), json.toPrettyString())
        }
    }

    @Test
    fun testAfterSubmissionUpdateFailure() {
        val testEvent = mockk<com.haulmont.cuba.core.app.events.EntityChangedEvent<Submission, UUID>>()

        testEvent.apply {
            every { type } returns EntityChangedEvent.Type.UPDATED
            every { entityId } returns mockk<Id<Submission, UUID>>()
        }

        every {
            txDM.load(ofType(Id::class)).view(ViewConstants.SUBMISSION_FETCH).one()
        } throws Exception("Database error")

        val expected = assertThrows<Exception> { submissionListener.afterEntityCommit(testEvent) }

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
