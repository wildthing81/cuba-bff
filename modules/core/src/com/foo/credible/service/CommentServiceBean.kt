/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import javax.inject.Inject
import org.springframework.stereotype.Service
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.constants.ViewConstants.COMMENT_LIST
import com.anzi.credible.dto.CommentDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.entity.Comment
import com.anzi.credible.entity.Submission
import com.anzi.credible.helpers.CommentHelper
import com.anzi.credible.repository.CommentRepository
import com.anzi.credible.repository.SubmissionRepository
import mu.KotlinLogging

@Service(CommentService.NAME)
open class CommentServiceBean : CommentService {
    private val log = KotlinLogging.logger { }

    @Inject
    private lateinit var userService: UserService

    @Inject
    private lateinit var submissionRepository: SubmissionRepository

    @Inject
    private lateinit var commentRepository: CommentRepository

    /**
     * Fetch a comment
     *
     * @param commentId
     * @return
     */
    override fun fetchComment(commentId: String) = try {
        commentRepository.fetchCommentById(commentId).let {
            CommentHelper.buildDto(
                it,
                userService.fetchUsersByLogin(listOf(it.createdBy))[0]
            )
        }
    } catch (dbe: Exception) {
        log.error { "No comment exists for: $commentId" }
        ErrorDto(
            Comment::class.simpleName!!,
            commentId,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * Fetches comments for the submission
     *
     * @param submissionId
     * @return
     */
    override fun fetchSubmissionComments(submissionId: String) = try {
        submissionRepository.fetchSubmissionById(submissionId, COMMENT_LIST)?.let {
            it.comments.map { comment ->
                CommentHelper.buildDto(
                    comment,
                    userService.fetchUsersByLogin(listOf(comment.createdBy))[0]
                )
            }
        }
    } catch (dbe: Exception) {
        log.error { "Error fetching submission comments: $submissionId" }
        ErrorDto(
            Submission::class.simpleName!!,
            submissionId,
            ErrorConstants.FIND_ENTITY,
            dbe.message
        )
    }

    /**
     * Creates a new comment
     *
     * @param commentDto
     * @return
     * @throws
     */
    override fun createComment(commentDto: CommentDto) = try {
        log.info { "Creating a new comment for submission: ${commentDto.submissionId}" }

        submissionRepository.fetchSubmissionById(commentDto.submissionId!!)?.let {
            commentRepository.createComment(it, commentDto)
        }?.let {
            log.info { "New comment ${it.id}" }
            CommentDto(it.id)
        }
    } catch (dbe: Exception) {
        log.error(dbe) { "Error creating new comment for submission: ${commentDto.submissionId}" }
        ErrorDto(Comment::class.simpleName!!, null, ErrorConstants.CREATE_ENTITY, dbe.message)
    }
}
