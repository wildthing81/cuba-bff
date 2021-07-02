/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import com.anzi.credible.dto.SectionDto
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.haulmont.cuba.security.entity.User
import io.mockk.mockk
import io.mockk.unmockkAll
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubmissionSectionsHelperTest {

    var helper = SubmissionSectionsHelper

    private val loggedUser = mockk<User>(relaxed = true)
    private lateinit var mapper: ObjectMapper
    private lateinit var testDefaults: JsonNode
    private lateinit var configuration: JsonNode

    @BeforeAll
    fun setUp() {
        mapper = ObjectMapper()
        testDefaults = mapper.readTree(
            """[
                {
                "slug": "decision-details-and-rationale", 
                "text": "The quick brown fox jumps over the lazy dog"
                },
                {
                 "slug": "submission-approval", 
                 "text": "Come out to the coast, we'll get together, have a few laughs!!"
                }          
            ]"""
        )
        configuration = mapper.readTree(
            """{
                "exceptions": [
                    {
                    "slug": "group-entites-rating",
                    "title": "Rating of group entites without financial statements",
                    "description": "sample description"
                    },
                    {
                    "slug": "submission-approval",
                    "title": "Rating of group entites without financial statements",
                    "description": "sample description for st-kilda"
                    }
                ],
                "submissionTemplate":[
                    {
                    "slug": "decision-details-and-rationale", 
                    "title": "Decision Details and Rationale"
                    },
                    {
                     "slug": "submission-approval", 
                     "title": "Submission Approval"
                    }          
                ]
        }"""
        )
    }

    @Test
    fun testCreateDefaultSections() {
        val sections = helper.createDefaultSections(
            configuration.get("submissionTemplate"),
            configuration.get("exceptions"),
            testDefaults,
            loggedUser
        )
        expect {
            that(sections).isA<List<SectionDto>>()
            that(sections.size).isEqualTo(2)
            that(sections[0].content).isEqualTo("The quick brown fox jumps over the lazy dog")
            that(sections[0].slug).isEqualTo("decision-details-and-rationale")
            that(sections[0].exceptions).isA<ArrayNode>()
            that(sections[0].exceptions?.size()).isEqualTo(2)
            that(sections[0].exceptions?.get(0)?.get("slug")?.asText()).isEqualTo("group-entites-rating")
            that(sections[0].exceptions?.get(0)?.get("title")?.asText()).isEqualTo(
                "Rating of group entites without " +
                    "financial statements"
            )
            that(sections[0].exceptions?.get(0)?.get("description")?.asText()).isEqualTo("sample description")
            that(sections[0].exceptions?.get(1)?.get("slug")?.asText()).isEqualTo("submission-approval")
            that(sections[0].exceptions?.get(1)?.get("title")?.asText())
                .isEqualTo("Rating of group entites without financial statements")
            that(sections[0].exceptions?.get(1)?.get("description")?.asText())
                .isEqualTo("sample description for st-kilda")

            that(sections).isA<List<SectionDto>>()
            that(sections.size).isEqualTo(2)
            that(sections[1].content).isEqualTo("Come out to the coast, we'll get together, have a few laughs!!")
            that(sections[1].slug).isEqualTo("submission-approval")
            that(sections[1].exceptions).isA<ArrayNode>()
            that(sections[1].exceptions?.size()).isEqualTo(2)
            that(sections[1].exceptions?.get(0)?.get("slug")?.asText()).isEqualTo("group-entites-rating")
            that(sections[1].exceptions?.get(0)?.get("title")?.asText()).isEqualTo(
                "Rating of group entites without " +
                    "financial statements"
            )
            that(sections[1].exceptions?.get(0)?.get("description")?.asText()).isEqualTo("sample description")
            that(sections[1].exceptions?.get(1)?.get("slug")?.asText()).isEqualTo("submission-approval")
            that(sections[1].exceptions?.get(1)?.get("title")?.asText()).isEqualTo(
                "Rating of group entites without " +
                    "financial " +
                    "statements"
            )
            that(sections[1].exceptions?.get(1)?.get("description")?.asText()).isEqualTo(
                "sample description for " +
                    "st-kilda"
            )
        }
    }

    @AfterAll
    fun tearDown() {
        unmockkAll()
    }
}
