/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.sql.Timestamp
import java.util.Date
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.dto.NotificationSession
import com.anzi.credible.dto.SubscriberDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Notification
import com.anzi.credible.helpers.NotificationHelper
import com.anzi.credible.repository.NotificationRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationServiceBeanTest {
    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var notificationRepository: NotificationRepository

    @InjectMockKs
    var testService = NotificationServiceBean()

    private val testUserId = UUID.randomUUID()
    private val testInstitution = mockk<Institution>()
    private val testNotificationDto = mockk<NotificationDto>()
    private val testSubscriberDto = mockk<SubscriberDto>()
    private val testUserDto = mockk<UserDto>()
    private val testUser = mockk<AppUser>()
    private val testUser2 = mockk<AppUser>()
    private val notification = spyk<Notification>()

    @BeforeAll
    fun setUp() {
        testInstitution.apply {
            every { name } returns "anz"
        }

        notification.apply {
            text = "A submission is assigned to me"
            createTs = Timestamp.valueOf("2021-05-01 3:36:17")
        }
    }

    @BeforeEach
    fun common() {
        every { userService.fetchLoggedUser() } returns testUser
        every { userService.fetchUsersByLogin(any()) } returns listOf(testUser)

        testUser.apply {
            every { id } returns testUserId
            every { name } returns "JohnC"
            every { institution } returns testInstitution
            every { createTs } returns Timestamp.valueOf("2021-05-01 3:36:17")
        }

        testUserDto.apply {
            every { id } returns testUserId
        }

        testSubscriberDto.apply {
            every { isHidden } returns false
            every { isRead } returns false
            every { user } returns testUserDto
        }

        testNotificationDto.apply {
            every { text } returns "A submission is assigned to me"
        }

        mockkObject(NotificationHelper)
        every { NotificationHelper getProperty "sessionsStore" } returns ConcurrentHashMap<String,
            NotificationSession>().apply {
            put(
                "session_1",
                mockk {
                    every { session } returns mockk {
                        every { id } returns "session_1"
                        every { basicRemote.sendText(any()) } just Runs
                    }
                    every { user } returns testUser
                }
            )
        }

        testUser2.apply {
            every { id } returns testUserId
            every { name } returns "BruceB"
            every { institution } returns testInstitution
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

    @Test
    fun testCreateNotificationSuccess() {
        every {
            notificationRepository.createNotification(testNotificationDto, testInstitution, any())
        } returns notification

        val result = testService.createNotification(testNotificationDto, listOf(testUserId.toString()))

        verify {
            userService.fetchLoggedUser()
            notificationRepository.createNotification(
                testNotificationDto,
                testInstitution,
                listOf(testUserId.toString())
            )
        }

        expect {
            that(result).isA<Notification>() and {
                get { text }.isEqualTo("A submission is assigned to me")
            }
        }
    }

    @Test
    fun testCreateNotificationFailure() {
        every {
            notificationRepository.createNotification(testNotificationDto, testInstitution, any())
        } throws Exception("Unable to create entity")

        val result = testService.createNotification(testNotificationDto, listOf(testUserId.toString()))

        verifyOrder {
            userService.fetchLoggedUser()
            notificationRepository.createNotification(
                testNotificationDto,
                testInstitution,
                listOf(testUserId.toString())
            )
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Notification")
                get { errorMessage }.isEqualTo(ErrorConstants.CREATE_ENTITY)
                get { errorMessage }.isEqualTo("Unable to create entity")
            }
        }
    }

    @Test
    fun testFetchNotificationsSuccess() {
        every {
            notificationRepository.fetchNotificationByTimeFrame(testUser, ofType(Date::class), ofType(Date::class))
        } returns listOf(notification)
        every {
            userService.updateUser(testUserId.toString())
        } returns testUser

        every {
            NotificationHelper.buildDto(ofType(Notification::class), ofType(AppUser::class))
        } returns testNotificationDto

        val result = testService.fetchNotifications(
            "2020-02-05T04:20:00.945Z",
            "2022-02-05T04:20:00.945Z",
            null
        )

        verifyOrder {
            userService.fetchLoggedUser()
            userService.updateUser(testUserId.toString())
        }

        expect {

            that(result).isA<List<NotificationDto>>() and {
                get { this.size }.isEqualTo(1)
                get { this[0].text }.isEqualTo("A submission is assigned to me")
            }
        }
    }

    @Test
    fun testFetchNotificationsCountSuccess() {
        every {
            notificationRepository.fetchNotificationByTimeFrame(testUser, ofType(Date::class), ofType(Date::class))
        } returns listOf(notification)
        every {
            userService.updateUser(testUserId.toString())
        } returns testUser

        every {
            NotificationHelper.buildDto(ofType(Notification::class), testUser)
        } returns testNotificationDto

        val result = testService.fetchNotifications(
            "2020-02-05T04:20:00.945Z",
            "2022-02-05T04:20:00.945Z",
            AppConstants.COUNT
        )

        verify(atMost = 1) {
            userService.fetchLoggedUser()
        }
        verify(inverse = true) {
            userService.updateUser(testUserId.toString())
        }

        expectThat(result).isEqualTo(1)
    }

    @Test
    fun testFetchNotificationsFailure() {
        every {
            notificationRepository.fetchNotificationByTimeFrame(testUser, ofType(Date::class), ofType(Date::class))
        } throws Exception("Unable to find entity")

        val result = testService.fetchNotifications(
            "2020-02-05T04:20:00.945Z",
            "2022-02-05T04:20:00.945Z",
            null
        )

        verify(atMost = 1) {
            userService.fetchLoggedUser()
        }

        verify(inverse = true) {
            userService.updateUser(testUserId.toString())
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Notification")
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { errorMessage }.isEqualTo("Unable to find existing entity")
            }
        }
    }

    @Test
    fun testMarkAllAsReadSuccess() {
        every {
            notificationRepository.markAllAsRead(ofType(AppUser::class))
        } returns true

        val result = testService.markAllAsRead()

        verifyOrder {
            userService.fetchLoggedUser()
            notificationRepository.markAllAsRead(testUser)
        }

        expect {
            that(result).isA<Boolean>() and {
                get { this }.isEqualTo(true)
            }
        }
    }

    @Test
    fun testMarkAllAsReadFailure() {
        every {
            notificationRepository.markAllAsRead(ofType(AppUser::class))
        } throws Exception("Unable to update entity")

        val result = testService.markAllAsRead()

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Notification")
                get { errorMessage }.isEqualTo(ErrorConstants.UPDATE_ENTITY)
                get { errorMessage }.isEqualTo("Unable to update entity")
            }
        }
    }

    @Test
    fun testUserConnectNotifySuccess() {
        val testSession = mockk<NotificationSession> {
            every { session } returns spyk {
                every { id } returns "session_2"
            }
            every { user } returns testUser2
        }

        testService.notifyUserConnect(testSession)

        verify(exactly = 1) {
            NotificationHelper.pushAll(
                withArg {
                    JSONAssert.assertEquals(
                        """{
                        "type":"USER_CONNECTED",
                        "payload":[{"key":"id","value":"$testUserId"},{"key":"name","value":"BruceB"}]
                        }""",
                        it,
                        false
                    )
                },
                testSession
            )
        }
    }

    @Test
    fun testUserDisConnectNotify() {
        val testSession = mockk<NotificationSession> {
            every { session } returns spyk {
                every { id } returns "session_2"
            }
            every { user } returns testUser2
        }

        testService.notifyUserDisconnect(testSession)

        verify(exactly = 1) {
            NotificationHelper.pushAll(
                withArg {
                    JSONAssert.assertEquals(
                        """{
                        "type":"USER_DISCONNECTED",
                        "payload":[{"key":"id","value":"$testUserId"},{"key":"name","value":"BruceB"}]
                        }""",
                        it,
                        false
                    )
                },
                testSession
            )
        }
    }
}
