/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import com.anzi.credible.dto.CommentDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.Comment
import com.anzi.credible.utils.DateUtils.formatTo
import com.haulmont.cuba.security.entity.User

object CommentHelper {

    /**
     * Builds Comment Dto from entity and other required objects
     *
     * @param comment
     * @return
     */
    fun buildDto(comment: Comment, creator: User): CommentDto {
        return CommentDto(
            comment.id,
            null, //   comment.submission!!.id.toString(),
            comment.text,
            comment.createTs.formatTo(),
            creator.run { UserDto(this.id, name = this.name) },
            comment.updateTs.formatTo()
        )
    }
}
