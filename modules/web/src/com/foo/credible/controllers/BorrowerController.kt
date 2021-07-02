/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.service.BorrowerService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import javax.inject.Inject

@ApiIgnore
@Api(tags = ["Borrower APIs"], description = "Operations on borrower")
@RestController
class BorrowerController {

    @Inject
    private lateinit var borrowerService: BorrowerService

    @ApiOperation(value = "Create a borrower", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 201, message = "Borrower created", response = BorrowerDto::class),
            ApiResponse(code = 500, message = "Error creating borrower", response = ErrorDto::class)
        ]
    )
    @PostMapping("/borrower")
    fun createBorrower(@RequestBody borrower: BorrowerDto):
        ResponseEntity<out Any> {
            val response = borrowerService.createBorrower(borrower)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(response as BorrowerDto, HttpStatus.CREATED)
        }

    @ApiOperation(value = "Fetch a borrower by id", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Borrower fetched", response = BorrowerDto::class),
            ApiResponse(code = 400, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping("/borrower/{id}")
    fun fetchBorrower(@PathVariable(value = "id") borrowerId: String):
        ResponseEntity<out Any> {
            val response = borrowerService.fetchBorrower(borrowerId)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.BAD_REQUEST)
            else ResponseEntity(response as BorrowerDto, HttpStatus.OK)
        }

    @ApiOperation(value = "Fetch all of an institution's borrowers", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(
                code = 200,
                message = "Borrowers fetched",
                response = BorrowerDto::class,
                responseContainer =
                "List"
            ),
            ApiResponse(code = 500, message = ErrorConstants.FIND_ENTITY, response = ErrorDto::class)
        ]
    )
    @GetMapping("/borrowers")
    fun fetchBorrowers(): ResponseEntity<out Any> {
        val response = borrowerService.fetchBorrowers()

        return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
        else ResponseEntity(response as List<*>, HttpStatus.OK)
    }

    @PatchMapping("/borrower/{borrowerId}")
    fun updateBorrower(@PathVariable borrowerId: String, @RequestBody borrowerDto: BorrowerDto):
        ResponseEntity<out Any> {
            val response = borrowerService.updateBorrower(borrowerId, borrowerDto)

            return if (response is ErrorDto) ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
            else ResponseEntity(HttpStatus.NO_CONTENT)
        }
}
