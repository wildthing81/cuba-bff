/*
 * The code is copyright ©2021
 */

package com.foo.credible.controllers

import java.util.UUID
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
import com.anzi.credible.constants.AppConstants
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.service.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import io.mockk.verify

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @MockK
    lateinit var userService: UserService

    @Inject
    private lateinit var mockMvc: MockMvc

    @InjectMockKs
    var testController = UserController()

    private val testId = UUID.randomUUID()
    private lateinit var testJson: String
    private lateinit var testDto: UserDto

    @BeforeAll
    fun setUp() {
        mockMvc = standaloneSetup(testController)
            .setMessageConverters(MappingJackson2HttpMessageConverter()).build()
        testJson = """{
                "login": "johnc",
                "name": "John C",
                "firstName": "John",
                "lastName": "Constantine",
                "email": "johnc@hellblazer.com",
                "roles": ["system-full-access","credible-ui"],
                "position": "magician",
                "scope": "test-scope",
                "profileImage": "rtretrtty",
                "cadLevel": 15,
	            "preferences": {}
        }"""

        testDto = ObjectMapper().registerKotlinModule().readValue(testJson, UserDto::class.java)
    }

    @Test
    fun testCreateUserSuccess() {
        every {
            userService
                .createUser(ofType(UserDto::class))
        } returns UserDto(testId)

        mockMvc.perform(
            post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated)
            .andExpect(content().json("""{ "id": $testId }"""))
            .andReturn()

        verify(exactly = 1) { userService.createUser(testDto) }
    }

    @Test
    fun testCreateUserFailure() {
        every {
            userService
                .createUser(ofType(UserDto::class))
        } returns ErrorDto(
            "User",
            null,
            ErrorConstants.CREATE_ENTITY,
            "Error creating User"
        )

        mockMvc.perform(
            post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testJson)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "User",
                    "id": null,
                    "errorMessage": "Unable to create entity",
                    "errorDetails": "Error creating User"
                }"""
                )
            )

        verify(exactly = 1) { userService.createUser(testDto) }
    }

    @Test
    fun testUserDetailsSuccess() {
        every {
            userService
                .userDetails()
        } returns testDto.copy(id = UUID.fromString("5d5f1bed-95a1-8e88-10e1-2250052479d1"))

        mockMvc.perform(
            get("/user/me")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """{
                    "id": "5d5f1bed-95a1-8e88-10e1-2250052479d1",
                     "login": "johnc",
                     "name": "John C",
                    "roles": ["system-full-access","credible-ui"],
                    "position": "magician",
                    "scope": "test-scope",
                    "cadLevel": 15,
	                "preferences": {}
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { userService.userDetails() }
    }

    @Test
    fun testUserDetailsFailure() {
        every {
            userService
                .userDetails()
        } returns ErrorDto(
            "User",
            null,
            ErrorConstants.FIND_ENTITY,
            "Error fetching details of User"
        )

        mockMvc.perform(
            get("/user/me")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "User",
                    "id": null,
                    "errorMessage": "Unable to find existing entity",
                    "errorDetails": "Error fetching details of User"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { userService.userDetails() }
    }

    @Test
    fun testUserLogoutSuccess() {
        every {
            userService.logout()
        } returns AppConstants.SUCCESS

        mockMvc.perform(
            post("/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andReturn()

        verify(exactly = 1) { userService.logout() }
    }

    @Test
    fun testUserLogoutFailure() {
        every {
            userService.logout()
        } returns ErrorDto(
            "User",
            "JohnC",
            ErrorConstants.LOGOUT_ERROR,
            "Error logging out JohnC"
        )

        mockMvc.perform(
            post("/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "User",
                    "id": "JohnC",
                    "errorMessage": "Unable to logout user",
                    "errorDetails": "Error logging out JohnC"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { userService.logout() }
    }

    @Test
    fun testFetchInstitutionUsersSuccess() {
        every {
            userService
                .fetchInstitutionUsers()
        } returns listOf(testDto.copy(id = UUID.fromString("5d5f1bed-95a1-8e88-10e1-2250052479d1")))

        mockMvc.perform(
            get("/users")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """[{
                    "id": "5d5f1bed-95a1-8e88-10e1-2250052479d1",
                     "name": "John C",
                    "position": "magician",
                    "cadLevel": 15
                }]"""
                )
            )
            .andReturn()

        verify(exactly = 1) { userService.fetchInstitutionUsers() }
    }

    @Test
    fun testFetchInstitutionUsersFailure() {
        every {
            userService
                .fetchInstitutionUsers()
        } returns ErrorDto(
            "User",
            null,
            ErrorConstants.FIND_ENTITY,
            "Error finding Users"
        )

        mockMvc.perform(
            get("/users")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is5xxServerError)
            .andExpect(
                content().json(
                    """{
                    "entity": "User",
                    "id": null,
                    "errorMessage": "Unable to find existing entity",
                    "errorDetails": "Error finding Users"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) { userService.fetchInstitutionUsers() }
    }

    @Test
    fun testGenerateCKEditorTokenFailure() {
        val accessToken = "test-access-token-value"
        every {
            userService
                .generateJWTToken(ofType(String::class))
        } returns ErrorDto(
            "User",
            null,
            ErrorConstants.INVALID_ACCESS_TOKEN,
            "Invalid access token"
        )

        mockMvc.perform(
            get("/ckeditor/$accessToken")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is4xxClientError)
            .andExpect(
                content().json(
                    """{
                    "entity": "User",
                    "id": null,
                    "errorMessage": "Invalid access token",
                    "errorDetails": "Invalid access token"
                }"""
                )
            )
            .andReturn()

        verify(exactly = 1) {
            userService.generateJWTToken(accessToken)
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
