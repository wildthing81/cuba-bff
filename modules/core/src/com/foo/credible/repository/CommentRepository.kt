/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import com.anzi.credible.dto.CommentDto
import com.anzi.credible.entity.Comment
import com.anzi.credible.entity.Submission

interface CommentRepository {

    fun createComment(submission: Submission, commentDto: CommentDto): Comment

    fun fetchCommentById(id: String): Comment
}
