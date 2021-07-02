/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.TaskDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.service.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import javax.inject.Inject
import org.springframework.web.bind.annotation.PathVariable

@ApiIgnore
@Api(tags = ["User APIs"], description = "Operations on user related entities")
@RestController
class UserController {

    @Inject
    private lateinit var userService: UserService

    @GetMapping("/user/me")
    fun userDetails(): ResponseEntity<out Any> {
        val response = userService.userDetails()
        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(response as UserDto, HttpStatus.OK)
    }

    @ApiOperation(value = "Create an user", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 201, message = "User created", response = UserDto::class),
            ApiResponse(code = 500, message = "Error creating user", response = ErrorDto::class)
        ]
    )
    @PostMapping("/user")
    fun createUser(@RequestBody userDto: UserDto): ResponseEntity<out Any> {
        val response = userService.createUser(userDto)
        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(response, HttpStatus.CREATED)
    }

    @ApiOperation(value = "Logout an user", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "User logged out"),
            ApiResponse(code = 500, message = "Error logging out user", response = ErrorDto::class)
        ]
    )
    @PostMapping("/user/logout")
    fun logout(): ResponseEntity<out Any> {
        val response = userService.logout()
        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(HttpStatus.OK)
    }

    @ApiOperation(
        value = "Fetch all of institution's users",
        produces = "application/json"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Tasks fetched",
                response = TaskDto::class,
                responseContainer = "List"
            ),
            ApiResponse(code = 404, message = "Incorrect path parameter"),
            ApiResponse(code = 500, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping("/users")
    fun fetchInstitutionUsers(): ResponseEntity<out Any> {
        val response = userService.fetchInstitutionUsers()
        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(response as List<*>, HttpStatus.OK)
    }

    @GetMapping("/ckeditor/{accessToken}")
    fun generateCKEditorToken(@PathVariable(value = "accessToken") accessToken: String):
        ResponseEntity<out Any> {
            val response = userService.generateJWTToken(accessToken)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.BAD_REQUEST)
            else ResponseEntity(response, HttpStatus.OK)
        }
}
