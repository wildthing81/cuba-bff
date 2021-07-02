/*
 * The code is copyright ©2021
 */

package com.foo.credible.repository

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.entity.Institution
import com.anzi.credible.repository.impl.InstitutionRepositoryImpl
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
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
class InstitutionRepositoryImplTest {

    @MockK
    lateinit var entityManager: EntityManager

    @MockK
    lateinit var dataManager: DataManager

    @MockK
    lateinit var persistence: Persistence

    @InjectMockKs
    var testRepository = InstitutionRepositoryImpl()

    private val testSiteId = "anz"
    private val testConfig = """{configuration":{"msg":"success"}}"""
    private val testBorrowerDefaults = """{borrowerDefaults":{"borrower-defaults-msg":"borrower-defaults-success"}}"""
    private val testInstitution = spyk<Institution>()
    private val testTransaction = mockk<Transaction>()
    private val testContext = mockk<LoadContext<Institution>>()

    @BeforeAll
    fun setUp() {
        every { persistence.entityManager } returns entityManager
        every { persistence.createTransaction() } returns testTransaction

        testTransaction.apply {
            every { commit() } just Runs
            every { close() } just Runs
        }
    }

    @BeforeEach
    fun common() {
        mockkStatic(LoadContext::class)
        dataManager.apply {
            every { load(testContext) } returns testInstitution
            every { commit(ofType(Institution::class)) } returns testInstitution
        }
    }

    @Test
    fun testFetchSiteConfigSuccess() {
        val mockQuery = mockk<Query>()
        every { entityManager.createQuery(ofType(String::class)) } returns mockQuery
        mockQuery.apply {
            every { setParameter(ofType(String::class), any()) } returns mockQuery
            every { firstResult } returns mockk<Institution>()
        }

        testRepository.fetchSiteConfigBySiteId(testSiteId)

        verify(atMost = 1) { entityManager.createQuery(QueryConstants.FETCH_BY_SITEID) }
        verify(exactly = 1) {
            mockQuery.setParameter(
                "siteId",
                withArg { expectThat(it).isEqualTo(testSiteId) }
            )
        }
    }

    // @Test
    fun testFetchSiteConfigNoConfig() {
        TODO("CUBA method calls")
    }

    @Test
    fun testCreateSiteConfigSuccess() {
        val mockQuery = mockk<LoadContext.Query>()

        every { LoadContext.create(Institution::class.java) } returns testContext
        every { LoadContext.createQuery(ofType(String::class)) } returns mockQuery
        every { testContext.setQuery(any()) } returns testContext
        every { mockQuery.setParameter(ofType(String::class), any()) } returns mockQuery

        val result = testRepository.createSiteConfig("anz", testConfig, testBorrowerDefaults)

        verify(atMost = 1) { LoadContext.createQuery(QueryConstants.FETCH_BY_SITEID) }
        verify(exactly = 1) {
            mockQuery.setParameter(
                "siteId",
                withArg { expectThat(it).isEqualTo(testSiteId) }
            )
        }

        expect {
            that(result).isA<Institution>() and {
                get { this.siteId }.isEqualTo(testSiteId)
                get { this.configuration }.isEqualTo(testConfig)
                get { this.borrowerDefaults }.isEqualTo(testBorrowerDefaults)
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
