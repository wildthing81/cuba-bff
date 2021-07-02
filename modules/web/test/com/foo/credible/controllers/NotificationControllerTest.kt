/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import java.sql.Timestamp
import java.util.UUID
import javax.inject.Inject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import com.anzi.credible.constants.AppConstants.COUNT
import com.anzi.credible.constants.AppConstants.ENCODING_UTF_8
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.NotificationType
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.KeyValueDto
import com.anzi.credible.dto.NotificationDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Notification
import com.anzi.credible.service.NotificationService
import com.anzi.credible.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.slugify.Slugify
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationControllerTest {

    @MockK
    lateinit var notificationService: NotificationService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = NotificationController()

    @MockK
    lateinit var userService: UserService

    private lateinit var testPayload: List<KeyValueDto>
    private val testInstitution = mockk<Institution>()
    private val testUser = mockk<AppUser>()
    private val testDto = spyk<NotificationDto>()
    private val testSubmissionId = UUID.randomUUID()
    private val testNotificationId = UUID.randomUUID()
    private val testUserId = UUID.randomUUID()

    @BeforeAll
    fun setUp() {
        mockMvc =
            MockMvcBuilders.standaloneSetup(testController).setMessageConverters(MappingJackson2HttpMessageConverter())
                .build()
        testInstitution.apply {
            every { name } returns "test-institution"
            every { siteId } returns Slugify().slugify("test-siteId")
        }
        testPayload = listOf(KeyValueDto("submissionId", "$testSubmissionId"))

        testDto.apply {
            type = "submission-created"
            text = "A new submission was created and you were added to its team"
            payload = testPayload
            isRead = true
            isHidden = false
            createdAt = "2021-05-11T02:59:29.522Z"
            id = testNotificationId
            to = testUserId
        }
    }

    @BeforeEach
    fun common() {
        every { userService.fetchLoggedUser() } returns testUser
        testUser.apply {
            every { id } returns testUserId
            every { name } returns "JohnC"
            every { createTs } returns Timestamp.valueOf("2021-04-04 3:36:17")
            every { lastNotifiedAt } returns Timestamp.valueOf("2020-04-06 3:36:17")
            every { institution } returns testInstitution
        }

        every { userService.updateUser(testUser.id.toString()) } returns testUser
    }

    @Test
    fun testFetchNotificationsSuccess() {
        every {
            notificationService.fetchNotifications(null, null, null)
        } returns listOf(testDto)

        every { testDto.type } returns NotificationType.SUBMISSION_CREATED.id

        mockMvc.perform(
            MockMvcRequestBuilders.get("/notifications")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """[{
                          "text":"A new submission was created and you were added to its team",
                          "payload":[
                             {
                                "key":"submissionId",
                                "value":"$testSubmissionId"
                             }
                          ],
                          "isRead":true,
                          "isHidden":false,
                          "id":"$testNotificationId",
                          "createdAt":"2021-05-11T02:59:29.522Z",
                          "to": "$testUserId"
                       }
                    ]"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchNotificationsCountSuccess() {
        every {
            notificationService.fetchNotifications(null, null, COUNT)
        } returns 2

        mockMvc.perform(
            MockMvcRequestBuilders.get("/notifications/count")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().string("2"))
            .andReturn()
    }

    @Test
    fun testFetchNotificationsFailure() {
        every {
            notificationService.fetchNotifications(null, null, null)
        } returns ErrorDto(
            "Notification",
            null,
            ErrorConstants.FIND_ENTITY,
            ""
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andReturn()

        verify(exactly = 1) { notificationService.fetchNotifications(null, null, null) }
    }

    @Test
    fun testUpdateAllNotificationsSuccess() {
        every {
            notificationService.markAllAsRead()
        } returns 0

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/notifications/markAllRead")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is2xxSuccessful)
            .andReturn()

        verify(exactly = 1) { notificationService.markAllAsRead() }
    }

    @Test
    fun testUpdateAllNotificationsFailure() {
        every {
            notificationService.markAllAsRead()
        } returns ErrorDto(
            "Notification",
            null,
            ErrorConstants.UPDATE_ENTITY,
            ""
        )

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/notifications/markAllRead")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andReturn()

        verify(exactly = 1) { notificationService.markAllAsRead() }
    }

    @Test
    fun testCreateNotificationsSuccess() {
        every {
            notificationService.createNotification(ofType(NotificationDto::class), ofType(List::class) as List<String>)
        } returns mockk<Notification>()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/notification")
                .characterEncoding(ENCODING_UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerKotlinModule().writeValueAsString(testDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andReturn()

        verify(exactly = 1) { notificationService.createNotification(eq(testDto), emptyList()) }
    }

    @Test
    fun testCreateNotificationsFailure() {
        every {
            notificationService.createNotification(ofType(NotificationDto::class), ofType(List::class) as List<String>)
        } returns ErrorDto(
            "Notification",
            null,
            ErrorConstants.CREATE_ENTITY,
            "database error"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/notification")
                .characterEncoding(ENCODING_UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().registerKotlinModule().writeValueAsString(testDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andReturn()
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
