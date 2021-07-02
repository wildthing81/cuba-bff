/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.dto.BorrowerDto
import com.anzi.credible.entity.Borrower
import com.anzi.credible.entity.Institution
import com.anzi.credible.repository.impl.BorrowerRepositoryImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BorrowerRepositoryImplTest {

    @MockK
    lateinit var dataManager: DataManager

    @InjectMockKs
    var testRepository = BorrowerRepositoryImpl()

    private val testId = UUID.randomUUID()
    private val testSubmissionDefaults = """[
            {
                "slug": "financial-analysis",
                "text": "The quick brown fox jumps over the lazy dog"
            },
            {
                "slug": "pricing-summary",
                "text": "Come out to the coast, we'll get together, have a few laughs!!"
            }
            ]"""
    private val testBorrowerDto = BorrowerDto(
        testId,
        "bhp",
        ObjectMapper().readTree(testSubmissionDefaults),
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
    private val testBorrower = spyk<Borrower>()
    private val testInstitution = spyk<Institution>()
    private val testTransaction = mockk<Transaction>()
    private val testContext = mockk<LoadContext<Borrower>>()

    @BeforeAll
    fun setUp() {
        testTransaction.apply {
            every { commit() } just Runs
            every { close() } just Runs
        }
    }

    @BeforeEach
    fun common() {
        mockkStatic(LoadContext::class)
        dataManager.apply {
            every { load(testContext) } returns testBorrower
            every { commit(ofType(Borrower::class)) } returns testBorrower
        }
    }

    @Test
    fun testFetchBorrowerByIdSuccess() {
        every {
            dataManager.load(Borrower::class.java).id(ofType(UUID::class)).view(ofType(String::class))
        } returns mockk {
            every { one() } returns testBorrower
        }

        testRepository.fetchBorrowerById(testId.toString())

        verify(exactly = 1) {
            dataManager.load(Borrower::class.java).id(withArg { expectThat(it).isEqualTo(testId) })
        }
    }

    @Test
    fun testCreateBorrowerSuccess() {
        val mockQuery = mockk<LoadContext.Query>()

        every { LoadContext.create(Borrower::class.java) } returns testContext
        every { LoadContext.createQuery(ofType(String::class)) } returns mockQuery

        every { testContext.setQuery(any()).setView(ofType(String::class)) } returns testContext

        every { mockQuery.setParameter(ofType(String::class), any()) } returns mockQuery

        val result = testRepository.createBorrower(testInstitution, testBorrowerDto)

        verify(atMost = 1) { LoadContext.createQuery(QueryConstants.FETCH_BY_BORROWER_NAME) }
        verify(exactly = 1) {
            mockQuery.setParameter(
                "name",
                withArg { expectThat(it).isEqualTo(testBorrowerDto.name) }
            )
        }

        expect {
            that(result).isA<Borrower>() and {
                get { name }.isEqualTo("bhp")
                get { institution }.isEqualTo(testInstitution)
                get { customerType }.isEqualTo("Tier 1 - Financial")
                get { marketCap }.isEqualTo(100000)
                get { customerGroup }.isEqualTo("FINTECH")
            }
        }
    }

    @AfterEach
    fun reset() {
        clearAllMocks()
    }

    @AfterAll
    fun tearDown() {
        unmockkStatic(LoadContext::class)
        unmockkAll()
    }
}
