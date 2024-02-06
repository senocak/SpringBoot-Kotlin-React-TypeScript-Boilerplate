package com.github.senocak.auth.controller

import com.github.senocak.auth.TestConstants.USER_EMAIL
import com.github.senocak.auth.TestConstants.USER_NAME
import com.github.senocak.auth.TestConstants.USER_PASSWORD
import com.github.senocak.auth.createTestUser
import com.github.senocak.auth.domain.Role
import com.github.senocak.auth.domain.User
import com.github.senocak.auth.domain.dto.LoginRequest
import com.github.senocak.auth.domain.dto.RegisterRequest
import com.github.senocak.auth.domain.dto.UserWrapperResponse
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.security.JwtTokenProvider
import com.github.senocak.auth.service.RoleService
import com.github.senocak.auth.service.UserService
import com.github.senocak.auth.util.RoleName
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.ArgumentMatchers.anyList
import org.mockito.InjectMocks
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.mockito.kotlin.eq
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doReturn

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for AuthController")
class AuthControllerTest {
    @InjectMocks lateinit var authController: AuthController

    private val userService: UserService = mock()
    private val roleService: RoleService = mock()
    private val tokenProvider: JwtTokenProvider = mock()
    private val authenticationManager: AuthenticationManager = mock(AuthenticationManager::class.java)
    private val authentication: Authentication = mock(Authentication::class.java)
    private val passwordEncoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val bindingResult: BindingResult = mock(BindingResult::class.java)

    var user: User = createTestUser()

    @Nested
    internal inner class LoginTest {
        private val loginRequest: LoginRequest = LoginRequest(email = USER_EMAIL, password = USER_PASSWORD)
        @BeforeEach
        fun setup() {
            loginRequest.email = USER_NAME
            loginRequest.password = USER_PASSWORD
        }

        @Test
        @Throws(ServerException::class)
        fun givenSuccessfulPath_whenLogin_thenReturn200() {
            // Given
            whenever(methodCall = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(loginRequest.email, loginRequest.password)))
                .thenReturn(authentication)
            whenever(methodCall = userService.findByEmail(email = loginRequest.email)).thenReturn(user)
            val generatedToken = "generatedToken"
            whenever(methodCall = tokenProvider.generateJwtToken(userId = eq(user.id!!), roles = anyList())).thenReturn(generatedToken)
            // When
            val response: UserWrapperResponse = authController.login(loginRequest = loginRequest, resultOfValidation = bindingResult)
            // Then
            assertNotNull(response)
            assertEquals(generatedToken, response.token)
            assertEquals(user.name, response.userResponse.name)
            assertEquals(user.email, response.userResponse.email)
            assertEquals(user.roles.size, response.userResponse.roles.size)
        }
    }

    @Nested
    internal inner class RegisterTest {
        private val registerRequest: RegisterRequest = RegisterRequest(
            name = USER_NAME,
            email = USER_EMAIL,
            password = USER_PASSWORD
        )

        @Test
        fun givenExistMail_whenRegister_thenThrowServerException() {
            // Given
            whenever(userService.existsByEmail(email = registerRequest.email)).thenReturn(true)
            // When
            val closureToTest = Executable { authController.register(signUpRequest = registerRequest, resultOfValidation = bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        fun givenNotValidRole_whenRegister_thenThrowServerException() {
            // Given
            whenever(roleService.findByName(roleName = RoleName.ROLE_USER)).thenReturn(null)
            // When
            val closureToTest = Executable { authController.register(signUpRequest = registerRequest, resultOfValidation = bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        fun givenNotLogin_whenRegister_thenThrowServerException() {
            // Given
            doReturn(value = Role()).`when`(roleService).findByName(roleName = RoleName.ROLE_USER)
            // When
            val closureToTest = Executable { authController.register(signUpRequest = registerRequest, resultOfValidation = bindingResult) }
            // Then
            Assertions.assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenRegister_thenAssertResult() {
            // Given
            whenever(methodCall = roleService.findByName(roleName = RoleName.ROLE_USER)).thenReturn(Role())
            whenever(methodCall = userService.save(user = any())).thenReturn(user)
            whenever(methodCall = userService.findByEmail(email = registerRequest.email)).thenReturn(user)
            val generatedToken = "generatedToken"
            whenever(methodCall = tokenProvider.generateJwtToken(userId = eq(value = user.id!!), roles = anyList())).thenReturn(generatedToken)
            // When
            val response: UserWrapperResponse = authController.register(signUpRequest = registerRequest, resultOfValidation = bindingResult)
            // Then
            assertNotNull(response)
            assertNotNull(response.userResponse)
            assertNotNull(response.token)
            assertEquals(user.name, response.userResponse.name)
            assertEquals(user.email, response.userResponse.email)
            assertEquals(user.roles.size, response.userResponse.roles.size)
        }
    }
}