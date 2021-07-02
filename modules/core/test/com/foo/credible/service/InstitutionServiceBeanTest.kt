/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.repository.InstitutionRepository
import com.github.slugify.Slugify
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstitutionServiceBeanTest {

    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var institutionRepository: InstitutionRepository

    @InjectMockKs
    var testService = InstitutionServiceBean()

    private val testUser = mockk<AppUser>()
    private val configuration = """{"configuration": {"jobs": []", "taskStatus": [
            {
                "slug": "complete",
                "title": "Complete"
            },
            {
                "slug": "incomplete",
                "title": "Incomplete"
            }
        ]}}"""
    private val borrowerDefaults = """{borrowerDefaults":{
            "cmf": [
                {
                    "slug": "sample-1",
                    "title": "Sample CMF 1"
                },
                {
                    "slug": "sample-2",
                    "title": "Sample CMF 2"
                }
            ]}}"""

    private val testInstitution = Institution()

    @BeforeAll
    fun setUp() {

        testInstitution.name = "anz"
        testInstitution.siteId = Slugify().slugify("anz")
        testInstitution.configuration = configuration
        testInstitution.borrowerDefaults = borrowerDefaults
    }

    @BeforeEach
    fun common() {
        every { userService.fetchLoggedUser() } returns testUser
        every { testUser.institution } returns testInstitution
    }

    @Test
    fun testFetchSiteConfigSuccess() {
        every { institutionRepository.fetchSiteConfigBySiteId(any()) } returns testInstitution

        val result = testService.fetchSiteConfig()
        verify(exactly = 1) { institutionRepository.fetchSiteConfigBySiteId("anz") }
        expectThat(result) {
            isEqualTo(
                arrayOf(
                    "anz",
                    configuration,
                    borrowerDefaults
                )
            )
        }
    }

    @Test
    fun testFetchSiteConfigNoConfig() {
        every { institutionRepository.fetchSiteConfigBySiteId(any()) } returns null

        val result = testService.fetchSiteConfig()
        verify(exactly = 1) { institutionRepository.fetchSiteConfigBySiteId("anz") }
        expectThat(result) {
            isEqualTo(emptyArray())
        }
    }

    @Test
    fun testCreateSiteConfigSuccess() {
        every {
            institutionRepository
                .createSiteConfig(
                    ofType(String::class),
                    ofType(String::class),
                    ofType(String::class)
                )
        } returns testInstitution

        val result = testService.createSiteConfig(
            "anz",
            configuration,
            borrowerDefaults
        )
        verify(exactly = 1) {
            institutionRepository.createSiteConfig(
                "anz",
                configuration,
                borrowerDefaults
            )
        }
        expectThat(result) {
            isEqualTo("anz")
        }
    }

    @Test
    fun testCreateSiteConfigFailure() {
        every {
            institutionRepository
                .createSiteConfig(
                    "anz",
                    configuration,
                    borrowerDefaults
                )
        } returns null

        val result = testService.createSiteConfig(
            "anz",
            configuration,
            borrowerDefaults
        )
        verify(exactly = 1) {
            institutionRepository.createSiteConfig(
                "anz",
                configuration,
                borrowerDefaults
            )
        }
        expectThat(result) {
            isEqualTo("Failure")
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
