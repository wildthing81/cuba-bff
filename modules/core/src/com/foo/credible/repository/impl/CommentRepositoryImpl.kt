/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository.impl

import com.anzi.credible.constants.ViewConstants
import com.anzi.credible.dto.CommentDto
import com.anzi.credible.entity.Comment
import com.anzi.credible.entity.Submission
import com.anzi.credible.repository.CommentRepository
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import org.springframework.stereotype.Repository
import java.util.UUID
import javax.inject.Inject

@Repository
open class CommentRepositoryImpl : CommentRepository {
    @Inject
    private lateinit var dataManager: DataManager

    /**
     * Creates new comment
     *
     * @param submission
     * @param commentDto
     * @return
     */
    override fun createComment(submission: Submission, commentDto: CommentDto): Comment {
        val commitContext = CommitContext()
        val comment = dataManager.create(Comment::class.java)

        commitContext.addInstanceToCommit(comment)
        comment.submission = submission
        comment.text = commentDto.text
        dataManager.commit(commitContext)

        return comment
    }

    /**
     * Fetches comment by id
     *
     * @param id
     * @return
     */
    override fun fetchCommentById(id: String): Comment = dataManager.load(Comment::class.java)
        .id(UUID.fromString(id))
        .view(ViewConstants.COMMENT_FETCH).one()
}
