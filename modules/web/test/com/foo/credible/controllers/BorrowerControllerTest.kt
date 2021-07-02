/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.service.BorrowerService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID
import javax.inject.Inject

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BorrowerControllerTest {

    @MockK
    lateinit var borrowerService: BorrowerService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = BorrowerController()

    private val testId = UUID.randomUUID()
    private lateinit var testSubmissionDefault: String
    private lateinit var testJson: String
    private lateinit var testDto: BorrowerDto

    @BeforeAll
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
            .setMessageConverters(MappingJackson2HttpMessageConverter()).build()
        testSubmissionDefault = """
                    [
                        {
                            "slug": "financial-analysis",
                            "text": "The quick brown fox jumps over the lazy dog"
                        },
                        {
                            "slug": "pricing-summary",
                            "text": "Come out to the coast, we'll get together, have a few laughs!!"
                        }
                    ] 
        """

        testJson = """{
            	"name": "test-borrower",
                "submissionDefaults": [
                    {
                        "slug": "financial-analysis",
                        "text": "The quick brown fox jumps over the lazy dog"
                    },
                    {
                        "slug": "pricing-summary",
                        "text": "Come out to the coast, we'll get together, have a few laughs!!"
                    }
                ],
                "customerType" : "Tier 1 - Financial",
                "marketCap": 100000,
                "cadLevel": 100,
                "customerGroup": "FINTECH",
                "businessUnit": "BU",
                "anzsic": "B-0600",
                "ccrRiskScore": 68,
                "securityIndex": "ASD23",
                "externalRatingAndOutLook": 8,
                "lastFullReviewAt": "2020-08-01T00:40:09.327Z",
                "lastScheduleReviewAt": "2020-08-01T00:40:09.876Z",
                "nextScheduleReviewAt": "2021-08-01T00:40:09.456Z",
                "riskSignOff": "Y",
                "regulatoryRequirements" : "Y"
            }"""

        testDto = ObjectMapper().registerKotlinModule().readValue(testJson, BorrowerDto::class.java)
    }

    @Test
    fun testCreateBorrowerSuccess() {

        every {
            borrowerService.createBorrower(ofType(BorrowerDto::class))
        } returns testDto.copy(id = testId)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/borrower")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.content().json("""{"id": "$testId"}"""))
            .andReturn()
    }

    @Test
    fun testCreateBorrowerFailure() {
        every {
            borrowerService.createBorrower(ofType(BorrowerDto::class))
        } returns ErrorDto(
            "Borrower",
            null,
            ErrorConstants.CREATE_ENTITY,
            "java.text.ParseException: Unparseable date: \"2020-08-01T00:40:09\""
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/borrower")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
            .andReturn()

        verify(exactly = 1) { borrowerService.createBorrower(ofType(BorrowerDto::class)) }
    }

    @Test
    fun testFetchBorrowerSuccess() {
        every {
            borrowerService.fetchBorrower(testId.toString())
        } returns (
            BorrowerDto(
                testId, "test-borrower",
                ObjectMapper().readTree(testSubmissionDefault),
                null,
                "Tier 1 - Financial",
                100000,
                100,
                "FINTECH",
                "BU",
                "B-0600",
                68,
                "ASD23",
                8,
                "2020-08-01T00:40:09.327Z",
                "2020-08-01T00:40:09.876Z",
                "2021-08-01T00:40:09.456Z",
                "Y",
                "Y"
            )
            )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/borrower/$testId")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().json(testJson))
            .andReturn()

        verify(exactly = 1) { borrowerService.fetchBorrower(testId.toString()) }
    }

    @Test
    fun testFetchBorrowerFailure() {
        every {
            borrowerService.fetchBorrower("test-borrower-id-that-does-not-exists")
        } returns ErrorDto(
            "Institution",
            "test-borrower-id-that-does-not-exists",
            ErrorConstants.FIND_ENTITY,
            "No results"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/borrower/test-borrower-id-that-does-not-exists")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andReturn()

        verify(exactly = 1) { borrowerService.fetchBorrower("test-borrower-id-that-does-not-exists") }
    }

    @Test
    fun testFetchBorrowersSuccess() {
        val testBorrowersResponseStr = """[
                {
                    "id": "ea06fd44-d3df-5049-7a02-2de560c35e71",
                    "name": "test-borrower-1",
                    "submissionCount": 22
                },
                {
                    "id": "6a81ae53-7f12-09b8-7568-07d3e20312da",
                    "name": "test-borrower-2",
                    "submissionCount": 9
                }
            ]"""

        every {
            borrowerService.fetchBorrowers()
        } returns listOf(
            BorrowerDto(
                UUID.fromString("ea06fd44-d3df-5049-7a02-2de560c35e71"),
                "test-borrower-1",
                submissionCount = 22
            ),
            BorrowerDto(
                UUID.fromString("6a81ae53-7f12-09b8-7568-07d3e20312da"),
                "test-borrower-2",
                submissionCount = 9
            )
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/borrowers")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().json(testBorrowersResponseStr))
            .andReturn()

        verify(exactly = 1) { borrowerService.fetchBorrowers() }
    }

    @Test
    fun testUpdateBorrowerSuccess() {
        val updateTeamJson = """ {
                "team": [
                    "5dda63c5-0e1a-ad9e-564d-bc0ee48fb4bf"
                ]
            } """

        every {
            borrowerService.fetchBorrower(testId.toString())
        } returns (
            BorrowerDto(
                testId, "test-borrower",
                ObjectMapper().readTree(testSubmissionDefault),
                null,
                "Tier 1 - Financial",
                100000,
                100,
                "FINTECH",
                "BU",
                "B-0600",
                68,
                "ASD23",
                8,
                "2020-08-01T00:40:09.327Z",
                "2020-08-01T00:40:09.876Z",
                "2021-08-01T00:40:09.456Z",
                "Y",
                "Y",
                null
            )
            )

        every {
            borrowerService.updateBorrower(ofType(String::class), ofType(BorrowerDto::class))
        } returns AppConstants.SUCCESS

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/borrower/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateTeamJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isNoContent)
            .andReturn()

        verify(exactly = 1) { borrowerService.updateBorrower(ofType(String::class), ofType(BorrowerDto::class)) }
    }

    @Test
    fun testUpdateBorrowerFailure() {
        val updateTeamJson = """ {
                "team": [
                    "5dda63c5-0e1a-ad9e-564d-bc0ee48fb4bf"
                ]
            } """

        every {
            borrowerService.fetchBorrower(testId.toString())
        } returns (
            BorrowerDto(
                testId, "test-borrower",
                ObjectMapper().readTree(testSubmissionDefault),
                null,
                "Tier 1 - Financial",
                100000,
                100,
                "FINTECH",
                "BU",
                "B-0600",
                68,
                "ASD23",
                8,
                "2020-08-01T00:40:09.327Z",
                "2020-08-01T00:40:09.876Z",
                "2021-08-01T00:40:09.456Z",
                "Y",
                "Y",
                null
            )
            )

        every {
            borrowerService.updateBorrower(ofType(String::class), ofType(BorrowerDto::class))
        } returns ErrorDto(
            "Borrower",
            testId.toString(),
            ErrorConstants.UPDATE_ENTITY,
            "Database Error"
        )

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/borrower/$testId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateTeamJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
            .andExpect(
                MockMvcResultMatchers.content().json(
                    """{
                    "entity": "Borrower",
                    "id": $testId,
                    "errorMessage": "Unable to update entity",
                    "errorDetails": "Database Error"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { borrowerService.updateBorrower(ofType(String::class), ofType(BorrowerDto::class)) }
    }

    @AfterEach
    fun reset() {
        clearAllMocks()
    }

    @AfterAll
    fun tearDown() {
        unmockkAll()
    }
}
