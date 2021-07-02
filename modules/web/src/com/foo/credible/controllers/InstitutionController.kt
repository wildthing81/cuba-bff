/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.service.InstitutionService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
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

@ApiIgnore
@Api(tags = ["Institution APIs"], description = "Operations on institution")
@RestController
class InstitutionController {

    @Inject
    private lateinit var institutionService: InstitutionService

    @ApiOperation(value = "Fetch institution from user login token", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "Institution fetched", response = JsonNode::class),
            ApiResponse(code = 400, message = ErrorConstants.FIND_ENTITY)
        ]
    )
    @GetMapping("/institution")
    fun fetchSiteConfiguration(): ResponseEntity<JsonNode> {
        val response = institutionService.fetchSiteConfig()

        return if (response.isNotEmpty()) {
            val json = ObjectMapper().createObjectNode()
                .put("name", response[0])
                .set<JsonNode>(
                    "configuration",
                    (ObjectMapper().readTree(response[1]) as ObjectNode)
                        .set("borrowerDefaults", ObjectMapper().readTree(response[2]))
                )
            ResponseEntity<JsonNode>(json, HttpStatus.OK)
        } else ResponseEntity<JsonNode>(HttpStatus.BAD_REQUEST)
    }

    @ApiOperation(value = "Create an institution", produces = "application/json")
    @ApiResponses(
        value = [
            ApiResponse(code = 201, message = "Institution created", response = JsonNode::class),
            ApiResponse(code = 500, message = "Error creating institution")
        ]
    )
    @PostMapping("/institution")
    fun createSiteConfiguration(@RequestBody requestBody: JsonNode): ResponseEntity<JsonNode> {
        val name = requestBody.get("name").textValue()
        val borrowerDefaults = (requestBody.get("configuration") as ObjectNode).remove("borrowerDefaults").toString()
        val configuration = requestBody.get("configuration").toString()

        val response = institutionService.createSiteConfig(name, configuration, borrowerDefaults)

        return if (response == "Failure") ResponseEntity<JsonNode>(HttpStatus.INTERNAL_SERVER_ERROR)
        else {
            val json = ObjectMapper().createObjectNode().put("slug", response)
            ResponseEntity<JsonNode>(json, HttpStatus.CREATED)
        }
    }
}
