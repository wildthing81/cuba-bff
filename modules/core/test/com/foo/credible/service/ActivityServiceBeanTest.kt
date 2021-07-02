/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.sql.Timestamp
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.entity.Activity
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Submission
import com.anzi.credible.repository.ActivityRepository
import com.github.slugify.Slugify
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityServiceBeanTest {
    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var activityRepository: ActivityRepository

    @InjectMockKs
    var testService = ActivityServiceBean()

    private val testInstitution = Institution()
    private val testUser = mockk<AppUser>()
    private val testSubmissionId = UUID.randomUUID().toString()

    @BeforeAll
    fun setUp() {
        testInstitution.name = "test-institution"
        testInstitution.siteId = Slugify().slugify("test-siteId")
    }

    @BeforeEach
    fun common() {
        every { userService.fetchLoggedUser() } returns testUser
        every { testUser.name } returns "JohnC"
        every { testUser.institution } returns testInstitution

        every { userService.fetchLoggedUser() } returns testUser
    }

    @AfterEach
    fun reset() {
        clearAllMocks()
    }

    @AfterAll
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testActivitiesSuccess() {
        val activity = mockk<Activity>()
        every { activity.institution } returns testInstitution
        every { activity.details } returns """{
                                                "message": "Jaykishan Parikh created this submission",
                                                "timestamp": "2021-02-10T02:16:31.880Z"
                                            }"""
        every { activity.idKey } returns Submission::class.toString()
        every { activity.idValue } returns testSubmissionId

        every {
            activityRepository.createActivity(ofType(Institution::class), ofType(String::class), ofType(String::class))
        } returns activity

        // create activity for submission create event
        val result = testService.addActivity(
            testSubmissionId,
            """{
                                "message": "Jaykishan Parikh created this submission",
                                "timestamp": "2021-02-10T02:16:31.880Z"
                            }"""
        )

        verify {
            userService.fetchLoggedUser()
            activityRepository.createActivity(
                testInstitution,
                testSubmissionId,
                """{
                                "message": "Jaykishan Parikh created this submission",
                                "timestamp": "2021-02-10T02:16:31.880Z"
                            }"""
            )
        }

        expect {
            that(result).isA<Activity>() and {
                get { result }.isEqualTo(activity)
            }
        }
    }

    @Test
    fun testFetchActivitiesSuccess() {
        val activity = mockk<Activity>()
        every { activity.institution } returns testInstitution
        every { activity.idKey } returns Submission::class.toString()
        every { activity.idValue } returns testSubmissionId
        every { activity.details } returns """{
                                                "type": "submission",
                                                "message": "Jaykishan Parikh created this submission",
                                                "timestamp": "2021-02-10T02:16:31.880Z"
                                            }"""

        every {
            activityRepository.fetchActivitiesBySubmissionAndTimeFrame(
                testSubmissionId,
                "activity-fetch",
                any(),
                any()
            )
        } returns listOf(activity)

        val fetchActivitiesResult = testService.fetchActivities(
            testSubmissionId,
            Timestamp.valueOf("2020-01-01 3:36:17"),
            Timestamp.valueOf("2022-01-01 3:36:17")
        )

        expect {
            that(fetchActivitiesResult).isA<List<Activity>>()
        }
    }

    @Test
    fun testAddActivityFailure() {
        every {
            activityRepository.createActivity(
                ofType(Institution::class),
                ofType(String::class),
                ofType(String::class)
            )
        } throws Exception("Database Error")

        val result = testService.addActivity(
            testSubmissionId,
            testSubmissionId
        )

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.CREATE_ENTITY)
                get { entity }.isEqualTo("Activity")
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }

    @Test
    fun testFetchActivitiesFailure() {
        every {
            activityRepository.fetchActivitiesBySubmissionAndTimeFrame(
                ofType(String::class),
                ofType(String::class),
                ofType(Timestamp::class),
                ofType(Timestamp::class)
            )
        } throws Exception("Database Error")

        val result = testService.fetchActivities(
            testSubmissionId,
            Timestamp.valueOf("2020-01-01 3:36:17"),
            Timestamp.valueOf("2022-01-01 3:36:17")
        )

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { entity }.isEqualTo("Activity")
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }
}
