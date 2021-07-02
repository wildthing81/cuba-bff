/*
 * The code is copyright ©2021
 */

package com.foo.credible.service

import java.util.Locale
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import com.anzi.credible.config.AppConfig
import com.anzi.credible.constants.ErrorConstants
import com.anzi.credible.dto.ErrorDto
import com.anzi.credible.dto.UserDto
import com.anzi.credible.entity.AppUser
import com.anzi.credible.entity.Institution
import com.anzi.credible.repository.UserRepository
import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.ObjectMapper
import com.haulmont.addon.restapi.rest.RestUserSessionInfo
import com.haulmont.addon.restapi.rest.ServerTokenStore
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.app.UserSessionsAPI
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import strikt.api.expect
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceBeanTest {

    @RelaxedMockK
    lateinit var userSessionSource: UserSessionSource

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var serverTokenStore: ServerTokenStore

    @MockK
    lateinit var userSessionsAPI: UserSessionsAPI

    @MockK
    lateinit var appConfig: AppConfig

    @InjectMockKs
    var testService = UserServiceBean()

    private lateinit var testDto: UserDto

    private val testUser = mockk<AppUser>()
    private val testId = UUID.randomUUID()
    private val testInstitution = mockk<Institution>()

    @BeforeAll
    fun setUp() {
        testDto = UserDto(
            null, "constantine",
            "John Constantine", "John",
            "T",
            "Constantine",
            "HellBlazer", "johnc@hellblazer.com",
            "https://randomuser.me/api/portraits/71.jpg",
            listOf("Magician"),
            "test-scope",
            15
        )
    }

    @BeforeEach
    fun common() {
        every { userRepository.getUsersFromLogins(any(), ofType(String::class)) } returns
            listOf(testUser)

        every { testUser.id } returns testId
        every { testUser.institution } returns testInstitution
        every { testUser.login } returns "constantine"
        every { testUser.name } returns "John J Constantine"
        every { testUser.firstName } returns "John"
        every { testUser.middleName } returns "J"
        every { testUser.lastName } returns "Constantine"
        every { testUser.position } returns "HellBlazer"
        every { testUser.email } returns "johnc@hellblazer.com"
        every { testUser.profileImage } returns ""
        every { testUser.cadLevel } returns 100
    }

    @Test
    fun testCreateUserSuccess() {
        every { userRepository.createUser(testDto, ofType(Institution::class)) } returns
            testUser

        val result = testService.createUser(testDto)

        verify(exactly = 1) {
            userRepository.createUser(testDto, ofType(Institution::class))
        }

        expect {
            that(result).isA<UserDto>() and {
                get { id }.isEqualTo(testId)
                get { password }.isNull()
            }
        }
    }

    @Test
    fun testCreateUserFailure() {
        every { userRepository.createUser(testDto, ofType(Institution::class)) } throws
            Exception("Error creating new user")

        val result = testService.createUser(testDto)

        verify {
            userRepository.createUser(testDto, ofType(Institution::class)) wasNot Called
        }
        expect {
            that(result).isA<ErrorDto>() and {
                get { errorMessage }.isEqualTo(ErrorConstants.CREATE_ENTITY)
                get { errorDetails }.isEqualTo("Error creating new user")
            }
        }
    }

    @Test
    fun testUserDetailsSuccess() {
        every { testUser.userRoles } returns emptyList()
        every { testUser.scope } returns "test-scope"
        every { testUser.preferences } returns "{}"
        every { testUser.name } returns "test-first-name test-last-name"
        every { testUser.firstName } returns "test-first-name"
        every { testUser.middleName } returns "test-middle-name"
        every { testUser.lastName } returns "test-last-name"

        val result = testService.userDetails()

        verify(exactly = 1) {
            userRepository.getUsersFromLogins(ofType(List::class) as List<String>)
        }

        expect {
            that(result).isA<UserDto>() and {
                get { login }.isEqualTo("constantine")
                get { name }.isEqualTo("test-first-name test-last-name")
                get { firstName }.isEqualTo("test-first-name")
                get { lastName }.isEqualTo("test-last-name")
                get { position }.isEqualTo("HellBlazer")
                get { cadLevel }.isEqualTo(100)
                get { scope }.isEqualTo("test-scope")
                get { preferences }.isEqualTo(ObjectMapper().createObjectNode())
            }
        }
    }

    @Test
    fun testUserDetailsFailure() {
        every { userRepository.getUsersFromLogins(any(), ofType(String::class)) } throws
            Exception("Error fetching user details")

        val result = testService.userDetails()

        verify(exactly = 1) {
            userRepository.getUsersFromLogins(ofType(List::class) as List<String>)
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("User")
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_ENTITY)
                get { errorDetails }.isEqualTo("Error fetching user details")
            }
        }
    }

    @Test
    fun testFetchInstitutionUsersSuccess() {
        every { userRepository.fetchInstitutionUsers(ofType(Institution::class)) } returns
            listOf(
                testUser,
                mockk() {
                    every { id } returns UUID.randomUUID()
                    every { institution } returns testInstitution
                    every { login } returns "test-login"
                    every { name } returns "test-first-name test-last-name"
                    every { position } returns "CEO"
                    every { cadLevel } returns 50
                }
            )

        val result = testService.fetchInstitutionUsers()

        verify(exactly = 1) {
            userRepository.fetchInstitutionUsers(testInstitution)
        }

        expect {
            that(result).isA<List<UserDto>>() and {
                get { this[0].name }.isEqualTo("John J Constantine")
                get { this[0].position }.isEqualTo("HellBlazer")
                get { this[0].cadLevel }.isEqualTo(100)

                get { this[1].name }.isEqualTo("test-first-name test-last-name")
                get { this[1].position }.isEqualTo("CEO")
                get { this[1].cadLevel }.isEqualTo(50)
            }
        }
    }

    @Test
    fun testFetchInstitutionUsersSuccessFailure() {
        every { userRepository.fetchInstitutionUsers(ofType(Institution::class)) } throws
            Exception("Database Error:SQLException")

        val result = testService.fetchInstitutionUsers()

        verify(exactly = 1) {
            userRepository.fetchInstitutionUsers(testInstitution)
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("User")
                get { errorMessage }.isEqualTo(ErrorConstants.FIND_USER)
                get { errorDetails }.isEqualTo("Database Error:SQLException")
            }
        }
    }

    @Test
    fun testGenerateJWTTokenSuccess() {
        every { appConfig.ckEditorEnvironmentId } returns "local"
        every { appConfig.ckEditorSecret } returns "secret"
        every { serverTokenStore.getAccessTokenByTokenValue(ofType(String::class)) } returns ByteArray(20)
        every {
            serverTokenStore.getSessionInfoByTokenValue(ofType(String::class))
        } returns RestUserSessionInfo(UUID.randomUUID(), Locale.ENGLISH)

        every {
            userSessionsAPI.get(ofType(UUID::class))?.user
        } returns testUser

        val result = testService.generateJWTToken("test-access-token")
        val decodedJWT = JWT.decode(result as String)

        verifyOrder {
            serverTokenStore.getAccessTokenByTokenValue("test-access-token")
            serverTokenStore.getSessionInfoByTokenValue("test-access-token")
        }

        expect {
            that(decodedJWT.subject).isEqualTo("constantine")
            that(decodedJWT.issuer).isEqualTo("auth0")
            that(decodedJWT.algorithm).isEqualTo("HS256")
            that(decodedJWT.claims["user"].toString())
                .isEqualTo("""{"email":"johnc@hellblazer.com","name":"John J Constantine"}""")
        }
    }

    @Test
    fun testGenerateJWTTokenFailure() {
        every { serverTokenStore.getAccessTokenByTokenValue(ofType(String::class)) } returns null

        val result = testService.generateJWTToken("test-access-token")

        verify(exactly = 1) {
            testService.generateJWTToken("test-access-token")
        }

        expect {
            that(result).isA<ErrorDto>() and {
                get { entity }.isEqualTo("User")
                get { errorMessage }.isEqualTo("Unable to generate token")
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
