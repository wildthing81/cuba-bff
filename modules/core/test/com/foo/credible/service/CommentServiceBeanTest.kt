/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.util.Date
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.CommentDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Comment
import com.anzi.credible.entity.Submission
import com.anzi.credible.repository.CommentRepository
import com.anzi.credible.repository.SubmissionRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentServiceBeanTest {
    @MockK
    lateinit var userService: UserService

    @InjectMockKs
    var testService = CommentServiceBean()

    @MockK
    lateinit var commentRepository: CommentRepository

    @MockK
    lateinit var submissionRepository: SubmissionRepository

    var testCommentId: UUID = UUID.randomUUID()
    private var testSubmissionId: UUID = UUID.randomUUID()
    var testUserId: UUID = UUID.randomUUID()
    private val testComment1 = mockk<Comment>()
    private val testSubmission = mockk<Submission>()
    private val testAppUser = mockk<AppUser>()

    @BeforeAll
    fun setUp() {
        testSubmission.apply {
            every { id } returns testSubmissionId
        }
    }

    @BeforeEach
    fun common() {
        testAppUser.apply {
            every { id } returns testUserId
            every { name } returns "John Cave"
        }

        testComment1.apply {
            every { id } returns testCommentId
            every { text } returns "First comment"
            every { submission } returns testSubmission
            every { createdBy } returns testAppUser.name
            every { createTs } returns Date()
            every { updateTs } returns Date()
        }

        every { userService.fetchUsersByLogin(any()) } returns listOf(testAppUser)

        every {
            submissionRepository.fetchSubmissionById(ofType(String::class), ofType(String::class))
        } returns testSubmission
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
    fun testFetchCommentSuccess() {
        every {
            commentRepository.fetchCommentById(ofType(String::class))
        } returns testComment1

        val result = testService.fetchComment(testCommentId.toString())

        expect {
            that(result).isA<CommentDto>()
        }
    }

    @Test
    fun testFetchCommentFail() {
        every {
            commentRepository.fetchCommentById(ofType(String::class))
        } throws Exception("Database Error")

        val result = testService.fetchComment(testCommentId.toString())

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { entity }.isEqualTo("Comment")
            }
        }
    }

    @Test
    fun testFetchSubmissionCommentsSuccess() {
        val testCommentId2 = UUID.randomUUID()
        val testComment2 = mockk<Comment>()

        every { testComment1.submission } returns testSubmission
        testComment2.apply {
            every { id } returns testCommentId2
            every { text } returns "Second comment"
            every { submission } returns testSubmission
            every { createdBy } returns testAppUser.name
            every { createTs } returns Date()
            every { updateTs } returns Date()
        }

        every { testSubmission.comments } returns mutableListOf(testComment1, testComment2)

        val commentsList = testService.fetchSubmissionComments(testSubmissionId.toString())

        expect {
            that(commentsList).isA<List<CommentDto>>().and {
                get { this.size }.isEqualTo(2)
                get { this[0].commentId }.isEqualTo(testCommentId)
                get { this[1].commentId }.isEqualTo(testCommentId2)
            }
        }
    }

    @Test
    fun testFetchSubmissionCommentsFail() {
        every {
            submissionRepository.fetchSubmissionById(ofType(String::class))
        } throws Exception("Database Error")

        val result = testService.fetchSubmissionComments(testSubmissionId.toString())

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { entity }.isEqualTo("Submission")
            }
        }
    }

    @Test
    fun testCreateCommentSuccess() {
        val commentDto = CommentDto(testCommentId, testSubmissionId.toString(), "First comment")

        every { submissionRepository.fetchSubmissionById(ofType(String::class)) } returns testSubmission
        every {
            commentRepository.createComment(
                ofType(Submission::class),
                ofType(CommentDto::class)
            )
        } returns testComment1

        val result = testService.createComment(commentDto)

        expect {
            that(result).isA<CommentDto>() and {
                get { commentId }.isEqualTo(testCommentId)
            }
        }
    }

    @Test
    fun testCreateCommentFail() {
        val commentDto = CommentDto(testCommentId, testSubmissionId.toString(), "First comment")

        every {
            commentRepository.createComment(ofType(Submission::class), ofType(CommentDto::class))
        } throws Exception("Database Error")

        val result = testService.createComment(commentDto)

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.CREATE_ENTITY)
                get { entity }.isEqualTo("Comment")
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }
}
