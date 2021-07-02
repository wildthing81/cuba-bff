/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import javax.inject.Inject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import com.anzi.credible.service.InstitutionService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import io.mockk.verify

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstitutionControllerTest {

    @MockK
    lateinit var institutionService: InstitutionService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = InstitutionController()

    private lateinit var testJson: String

    @BeforeAll
    fun setUp() {
        mockMvc = standaloneSetup(testController)
            .setMessageConverters(MappingJackson2HttpMessageConverter()).build()
        testJson = """{"name":"anz",
            "configuration":{"msg":"success","borrowerDefaults":{"borrower-defaults-msg":"borrower-defaults-success"}}
            }"""
    }

    @Test
    fun testFetchSiteConfigSuccess() {
        every { institutionService.fetchSiteConfig() } returns arrayOf(
            "anz",
            """{"msg":"success"}""",
            """{"borrower-defaults-msg":"borrower-defaults-success"}"""
        )

        mockMvc.perform(get("/institution").accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(content().json(testJson))
            .andReturn()

        verify(exactly = 1) { institutionService.fetchSiteConfig() }
    }

    @Test
    fun testFetchSiteConfigNoConfig() {
        every { institutionService.fetchSiteConfig() } returns emptyArray()

        mockMvc.perform(get("/institution"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError)
            .andReturn()

        verify(exactly = 1) { institutionService.fetchSiteConfig() }
    }

    @Test
    fun testCreateSiteConfigSuccess() {
        every {
            institutionService
                .createSiteConfig(
                    "anz",
                    """{"msg":"success"}""",
                    """{"borrower-defaults-msg":"borrower-defaults-success"}"""
                )
        } returns "anz"

        mockMvc.perform(
            post("/institution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(content().json("""{"slug":"anz"}"""))
            .andReturn()

        verify(exactly = 1) {
            institutionService.createSiteConfig(
                "anz",
                """{"msg":"success"}""",
                """{"borrower-defaults-msg":"borrower-defaults-success"}"""
            )
        }
    }

    @Test
    fun testCreateSiteConfigFailure() {
        every {
            institutionService
                .createSiteConfig(
                    "anz",
                    """{"msg":"success"}""",
                    """{"borrower-defaults-msg":"borrower-defaults-success"}"""
                )
        } returns "Failure"

        mockMvc.perform(
            post("/institution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)

        verify(exactly = 1) {
            institutionService.createSiteConfig(
                "anz",
                """{"msg":"success"}""",
                """{"borrower-defaults-msg":"borrower-defaults-success"}"""
            )
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
