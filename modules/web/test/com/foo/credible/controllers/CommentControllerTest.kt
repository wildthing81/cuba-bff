/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.CommentDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.service.CommentService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import java.util.UUID
import javax.inject.Inject

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentControllerTest {
    @MockK
    lateinit var commentService: CommentService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = CommentController()

    private val testId = UUID.randomUUID().toString()
    private val testCommentId = UUID.randomUUID()

    @BeforeAll
    fun setUp() {
        mockMvc = standaloneSetup(testController)
            .setMessageConverters(MappingJackson2HttpMessageConverter()).build()
    }

    @Test
    fun testCreateCommentSuccess() {
        val reqJson = """{ "submissionId" : "$testId", "text": "First comment" }"""
        val testDto = CommentDto(testCommentId, testId, "First comment")

        every {
            commentService.createComment(ofType(CommentDto::class))
        } returns testDto

        mockMvc.perform(
            MockMvcRequestBuilders.post("/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(content().json("""{ "id": $testCommentId }"""))
            .andReturn()
    }

    @Test
    fun testCreateCommentFailure() {
        val reqJson = """{ "submissionId" : "$testId", "text": "First comment" }"""

        every {
            commentService.createComment(ofType(CommentDto::class))
        } returns ErrorDto(
            "Comment",
            null,
            ErrorConstants.CREATE_ENTITY,
            "Database Error"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/comment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Comment",
                    "errorMessage": "Unable to create entity",
                    "errorDetails": "Database Error"
                }"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchCommentSuccess() {
        val testUserId = UUID.randomUUID()
        val testDto = CommentDto(
            testCommentId,
            null,
            "First comment",
            "2021-03-10T06:19:14.833Z",
            UserDto(id = testUserId, name = "Anna Analyst"),
            "2021-03-10T06:19:14.833Z"
        )

        every {
            commentService.fetchComment(ofType(String::class))
        } returns testDto

        mockMvc.perform(
            get("/comment/$testCommentId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """{
                    "id": $testCommentId,
                    "text": "First comment",
                    "createdAt": "2021-03-10T06:19:14.833Z",
                    "createdBy": {
                        "id": $testUserId,
                        "name": "Anna Analyst"
                    },
                    "updatedAt": "2021-03-10T06:19:14.833Z"
                }"""
                )
            )
            .andReturn()
    }

    @Test
    fun testFetchCommentThatReturnsNoComment() {
        val testCommentId = UUID.randomUUID()

        every {
            commentService.fetchComment(testCommentId.toString())
        } returns ErrorDto(
            "Comment",
            testCommentId.toString(),
            ErrorConstants.FIND_ENTITY,
            "No comment exists for: $testCommentId"
        )

        mockMvc.perform(
            get("/comment/$testCommentId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError)
            .andExpect(
                content().json(
                    """{
                    "entity": "Comment",
                    "id": $testCommentId,
                    "errorMessage": "Unable to find existing entity",
                    "errorDetails": "No comment exists for: $testCommentId"
                }"""
                )
            )
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
