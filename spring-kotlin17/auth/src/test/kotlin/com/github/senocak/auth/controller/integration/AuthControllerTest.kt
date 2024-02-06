package com.github.senocak.auth.controller.integration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.auth.TestConstants.USER_EMAIL
import com.github.senocak.auth.TestConstants.USER_NAME
import com.github.senocak.auth.TestConstants.USER_PASSWORD
import com.github.senocak.auth.config.SpringBootTestConfig
import com.github.senocak.auth.controller.AuthController
import com.github.senocak.auth.domain.Role
import com.github.senocak.auth.domain.RoleRepository
import com.github.senocak.auth.domain.dto.LoginRequest
import com.github.senocak.auth.domain.dto.RegisterRequest
import com.github.senocak.auth.exception.RestExceptionHandler
import com.github.senocak.auth.service.RoleService
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.RoleName
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders

/**
 * This integration test class is written for
 * @see AuthController
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for AuthController")
class AuthControllerTest {
    @Autowired private lateinit var authController: AuthController
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var roleRepository: RoleRepository
    @MockBean  private lateinit var roleService: RoleService

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(RestExceptionHandler::class.java)
            .build()
    }

    @Nested
    @Order(1)
    @DisplayName("Test class for login scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    internal inner class LoginTest {
        private var loginRequest: LoginRequest = LoginRequest(email = "", password = "")

        @Test
        @Order(1)
        @DisplayName("ServerException is expected since request body is not valid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenLogin_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(loginRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize<Any>(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    containsInAnyOrder("email: must not be blank", "password: must not be blank")))
        }

        @Test
        @Order(2)
        @DisplayName("ServerException is expected since credentials are not valid")
        @Throws(Exception::class)
        fun givenInvalidCredentials_whenLogin_thenThrowServerException() {
            // Given
            loginRequest.email = "USERNAME"
            loginRequest.password = "PASSWORD"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(loginRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.NOT_FOUND.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.NOT_FOUND.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("User not found with email")))
        }

        @Test
        @Order(3)
        @DisplayName("Happy path")
        @Throws(Exception::class)
        fun given_whenLogin_thenReturn200() {
            // Given
            loginRequest.email = "asenocakUser"
            loginRequest.password = "asenocak"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(loginRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", IsEqual.equalTo(loginRequest.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles[0].name", IsEqual.equalTo(RoleName.ROLE_USER.role)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token", IsNull.notNullValue()))
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Test class for register scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    internal inner class RegisterTest {
        private var registerRequest: RegisterRequest = RegisterRequest(name = "", email = "", password = "")

        @Test
        @Order(1)
        @DisplayName("ServerException is expected since request body is not valid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenRegister_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(registerRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize<Any>(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    containsInAnyOrder("password: must not be blank", "email: must not be blank",
                        "name: must not be blank","email: Invalid email")))
        }

        @Test
        @Order(2)
        @DisplayName("ServerException is expected since there is already user with username")
        @Throws(Exception::class)
        fun givenUserNameExist_whenRegister_thenThrowServerException() {
            // Given
            registerRequest.name = USER_NAME
            registerRequest.email = USER_EMAIL
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(registerRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Username is already taken!")))
        }

        @Test
        @Order(3)
        @DisplayName("ServerException is expected since there is already user with email")
        @Throws(Exception::class)
        fun givenEmailExist_whenRegister_thenThrowServerException() {
            // Given
            registerRequest.name = USER_NAME
            registerRequest.email = USER_EMAIL
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(registerRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]",
                    IsEqual.equalTo("Email Address already in use!")))
        }

        @Test
        @Order(4)
        @DisplayName("ServerException is expected since invalid role")
        @Throws(Exception::class)
        fun givenNullRole_whenRegister_thenThrowServerException() {
            // Given
            doReturn(value = null).`when`(roleService)!!.findByName(roleName = RoleName.ROLE_USER)
            registerRequest.name = USER_NAME
            registerRequest.email = "userNew@email.com"
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(registerRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.MANDATORY_INPUT_MISSING.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.MANDATORY_INPUT_MISSING.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables[0]", IsEqual.equalTo("User Role is not found")))
        }

        @Test
        @Order(5)
        @DisplayName("Happy path")
        @Throws(Exception::class)
        fun given_whenRegister_thenReturn201() {
            // Given
            val role: Role? = roleRepository.findByName(roleName = RoleName.ROLE_USER)
            doReturn(value = role).`when`(roleService)!!.findByName(roleName = RoleName.ROLE_USER)
            registerRequest.name = USER_NAME
            registerRequest.email = "userNew@email.com"
            registerRequest.password = USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .post("${AuthController.URL}/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(registerRequest))
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.username", IsEqual.equalTo("USER_USERNAME")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles", Matchers.hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.roles[0].name", IsEqual.equalTo(RoleName.ROLE_USER.role)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token", IsNull.notNullValue()))
        }
    }

    /**
     * @param value -- an object that want to be serialized
     * @return -- string
     * @throws JsonProcessingException -- throws JsonProcessingException
     */
    @Throws(JsonProcessingException::class)
    private fun writeValueAsString(value: Any): String = objectMapper.writeValueAsString(value)
}
