/*
 * The code is copyright ©2021
 */

package com.foo.credible.helpers

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import com.anzi.credible.dto.SubmissionDto
import com.anzi.credible.entity.SubmissionSection
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubmissionHelperTest {

    var helper = SubmissionHelper

    private var testSections = mutableListOf<SubmissionSection>()

    @BeforeAll
    fun setUp() {
        testSections.add(
            spyk {
                every { slug } returns "risk-grading"
            }
        )
        testSections.add(
            spyk {
                every { slug } returns "financial-analysis"
                every { content } returns "financial-analysis content"
            }
        )
    }

    @Test
    fun testUpdateSections() {
        val updateSubmissionDto = mockk<SubmissionDto>().apply {
            every { sections } returns mutableListOf(
                mockk {
                    every { slug } returns "risk-grading"
                    every { content } returns "risk-grading updated content"
                },
                mockk {
                    every { slug } returns "financial-analysis"
                    every { content } returns null
                }
            )
        }

        val deletedSections = helper.updateSections(testSections, updateSubmissionDto)
        expect {
            that(deletedSections).isA<MutableList<SubmissionSection>>() and {
                get { size }.isEqualTo(1)
                get { this[0].slug }.isEqualTo("financial-analysis")
                get { this[0].content }.isEqualTo("financial-analysis content")
            }
        }
        expectThat(testSections.find { it.slug == "risk-grading" }?.content).isEqualTo("risk-grading updated content")
    }

    @AfterAll
    fun tearDown() {
        unmockkAll()
    }
}
