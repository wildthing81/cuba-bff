/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import com.anzi.credible.dto.CommentDto

interface CommentService {
    companion object {
        const val NAME = "crd_CommentService"
    }

    fun createComment(commentDto: CommentDto): Any?

    fun fetchSubmissionComments(submissionId: String): Any?

    fun fetchComment(commentId: String): Any?
}
