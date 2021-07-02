/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution
import com.anzi.credible.entity.Submission
import com.anzi.credible.helpers.BorrowerHelper
import com.anzi.credible.repository.BorrowerRepository
import com.anzi.credible.repository.InstitutionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.slugify.Slugify
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BorrowerServiceBeanTest {
    @MockK
    lateinit var userService: UserService

    @RelaxedMockK
    lateinit var borrowerRepository: BorrowerRepository

    @RelaxedMockK
    lateinit var institutionRepository: InstitutionRepository

    @InjectMockKs
    var testService = BorrowerServiceBean()

    private val testUser = mockk<AppUser>()
    private lateinit var responseJson: String

    private val testBorrower1 = mockk<Borrower>()
    private val testBorrower2 = mockk<Borrower>()
    private val testId1 = UUID.randomUUID()
    private val testId2 = UUID.randomUUID()
    private val testInstitution = spyk<Institution>()
    private val testSubmissionDefaultsStr = """[
            {
                "slug": "financial-analysis",
                "text": "The quick brown fox jumps over the lazy dog"
            },
            {
                "slug": "pricing-summary",
                "text": "Come out to the coast, we'll get together, have a few laughs!!"
            }
            ]"""
    private val testDto = BorrowerDto(
        testId1,
        "test-borrower-1",
        ObjectMapper().readTree(testSubmissionDefaultsStr),
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
        "2020-08-01T00:40:09.876Z",
        "2020-08-01T00:40:09.876Z",
        "2020-12-01T00:40:09.876Z",
        "Y",
        "Y"
    )

    @BeforeAll
    fun setUp() {
        mockkObject(BorrowerHelper)

        responseJson = """{ "id": "6a81ae53-7f12-09b8-7568-07d3e20312da" }"""

        testInstitution.name = "anz"
        testInstitution.siteId = Slugify().slugify("anz")
    }

    @BeforeEach
    fun common() {
        every { userService.fetchLoggedUser() } returns testUser
        every { testUser.name } returns "JohnC"
        every { testUser.institution } returns testInstitution

        every {
            borrowerRepository.createBorrower(ofType(Institution::class), ofType(BorrowerDto::class))
        } returns testBorrower1

        every { testBorrower1.id } returns testId1
        every { testBorrower1.name } returns "test-borrower-1"
        every { testBorrower1.submissionDefaults } returns testSubmissionDefaultsStr
        every { testBorrower1.institution } returns testInstitution
        every { testBorrower1.submissions } returns mutableListOf(Submission(), Submission(), Submission())

        every { testBorrower2.id } returns testId2
        every { testBorrower2.name } returns "test-borrower-2"
        every { testBorrower2.submissionDefaults } returns testSubmissionDefaultsStr
        every { testBorrower2.institution } returns testInstitution
        every { testBorrower2.submissions } returns mutableListOf(Submission())
    }

    @Test
    fun testCreateBorrowerSuccess() {
        val result = testService.createBorrower(testDto)

        verify {
            userService.fetchLoggedUser()
            borrowerRepository.createBorrower(testInstitution, testDto)
        }

        expect {
            that(result).isA<BorrowerDto>() and {
                get { id }.isEqualTo(testId1)
            }
        }
    }

    @Test
    fun testCreateBorrowerFailure() {
        // @TODO - Next Borrower stories are going to change the syntax of the methods hence better to implement
        // this test after that.
    }

    @Test
    fun testFetchBorrowerSuccess() {
        every { borrowerRepository.fetchBorrowerById(testId1.toString()) } returns testBorrower1
        every { BorrowerHelper.buildDto(testBorrower1) } returns testDto

        val result = testService.fetchBorrower(testId1.toString())

        verify {
            userService.fetchLoggedUser()
            BorrowerHelper.buildDto(testBorrower1)
        }

        expect {
            that(result).isA<BorrowerDto>() and {
                get { id }.isEqualTo(testId1)
                get { name }.isEqualTo("test-borrower-1")
            }
        }
    }

    @Test
    fun testFetchBorrowerFailure() {
        every { borrowerRepository.fetchBorrowerById(testId1.toString()) } throws Exception("Database Error")
        every { BorrowerHelper.buildDto(testBorrower1) } returns testDto

        val result = testService.fetchBorrower(testId1.toString())

        verify {
            userService.fetchLoggedUser()
            borrowerRepository.fetchBorrowerById(testId1.toString())
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { id }.isEqualTo(testId1.toString())
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }

    @Test
    fun testFetchBorrowersSuccess() {
        every { institutionRepository.fetchBorrowers(testInstitution) } returns
            mutableSetOf(testBorrower1, testBorrower2)

        val result = testService.fetchBorrowers()

        verify {
            userService.fetchLoggedUser()
            institutionRepository.fetchBorrowers(testInstitution)
        }

        expect {
            that(result).isA<List<BorrowerDto>>() and {
                get { this[0].id }.isEqualTo(testId1)
                get { this[0].name }.isEqualTo("test-borrower-1")
                get { this[0].submissionCount }.isEqualTo(3)

                get { this[1].id }.isEqualTo(testId2)
                get { this[1].name }.isEqualTo("test-borrower-2")
                get { this[1].submissionCount }.isEqualTo(1)
            }
        }
    }

    @Test
    fun testFetchBorrowersFailure() {
        every { institutionRepository.fetchBorrowers(testInstitution) } throws Exception("Database Error")

        val result = testService.fetchBorrowers()

        verify {
            userService.fetchLoggedUser()
            institutionRepository.fetchBorrowers(testInstitution)
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("Institution")
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { id }.isEqualTo(testInstitution.siteId)
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
    }

    @Test
    fun testUpdateBorrowersSuccess() {
        every {
            borrowerRepository.fetchBorrowerById(ofType(String::class))
        } returns testBorrower1
        every {
            borrowerRepository.updateBorrower(ofType(Borrower::class))
        } returns testBorrower1

        testService.updateBorrower(testId1.toString(), testDto)

        verify {
            borrowerRepository.fetchBorrowerById(testId1.toString())
            borrowerRepository.updateBorrower(testBorrower1)
        }
    }

    @Test
    fun testUpdateBorrowersFailure() {
        every {
            borrowerRepository.fetchBorrowerById(ofType(String::class))
        } returns testBorrower1
        every {
            borrowerRepository.updateBorrower(ofType(Borrower::class))
        } throws Exception("Database Error")

        val result = testService.updateBorrower(testId1.toString(), testDto)

        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.UPDATE_ENTITY)
                get { id }.isEqualTo(testId1.toString())
                get { errorDetails }.isEqualTo("Database Error")
            }
        }
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
