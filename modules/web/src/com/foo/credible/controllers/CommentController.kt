/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import com.anzi.credible.dto.CommentDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.service.CommentService
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import javax.inject.Inject

@ApiIgnore
@Api(tags = ["Comment APIs"], description = "Operations on comment")
@RestController
class CommentController {

    @Inject
    private lateinit var commentService: CommentService

    @PostMapping("/comment")
    fun createComment(
        @RequestBody comment: CommentDto
    ):
        ResponseEntity<out Any> {
            val response = commentService.createComment(comment)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(response as CommentDto, HttpStatus.CREATED)
        }

    @GetMapping("/comment/{commentId}")
    fun fetchComment(@PathVariable(required = true) commentId: String): ResponseEntity<out Any> {
        val response = commentService.fetchComment(commentId)

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.BAD_REQUEST)
        else ResponseEntity(response as CommentDto, HttpStatus.OK)
    }
}
