package com.github.senocak.auth.controller.integration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.auth.TestConstants
import com.github.senocak.auth.TestConstants.USER_EMAIL
import com.github.senocak.auth.TestConstants.USER_NAME
import com.github.senocak.auth.config.SpringBootTestConfig
import com.github.senocak.auth.controller.BaseController
import com.github.senocak.auth.controller.UserController
import com.github.senocak.auth.domain.dto.LoginRequest
import com.github.senocak.auth.domain.dto.UpdateUserDto
import com.github.senocak.auth.domain.dto.UserWrapperResponse
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.RoleName
import com.github.senocak.auth.util.randomStringGenerator
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasSize
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

/**
 * This integration test class is written for
 * @see UserController
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for UserController")
class UserControllerIT {
    @Autowired private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup(wac: WebApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    @Nested
    @Order(1)
    @DisplayName("Get me")
    internal inner class GetMeTest {

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenGetMe_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .get("${BaseController.V1_USER_URL}/me")
                .header("Authorization", "Bearer $token")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name", equalTo(USER_NAME)))
                .andExpect(jsonPath("$.email", equalTo(USER_EMAIL)))
                .andExpect(jsonPath("$.roles", hasSize<Any>(1)))
                .andExpect(jsonPath("$.roles[0].name", equalTo(RoleName.ROLE_ADMIN.role)))
                .andExpect(jsonPath("$.emailActivatedAt", IsNull.notNullValue()))
        }
    }

    @Nested
    @Order(2)
    @DisplayName("Patch me")
    internal inner class PatchMeTest {
        private val updateUserDto: UpdateUserDto = UpdateUserDto()

        @Test
        @DisplayName("ServerException is expected since schema is invalid")
        @Throws(Exception::class)
        fun givenInvalidSchema_whenPatchMe_thenThrowServerException() {
            // Given
            updateUserDto.name = "n"
            updateUserDto.password = "p1"
            updateUserDto.passwordConfirmation = "p2"
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch("${BaseController.V1_USER_URL}/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(updateUserDto))
                .header("Authorization", "Bearer $token")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(jsonPath("$.exception.variables", hasSize<Any>(4)))
                .andExpect(
                    jsonPath(
                        "$.exception.variables",
                        Matchers.containsInAnyOrder(
                            "password: Password must be 6 or more characters in length.\n" +
                                "Password must contain 1 or more uppercase characters.\n" +
                                "Password must contain 1 or more special characters.",
                            "passwordConfirmation: Password must be 6 or more characters in length.\n" +
                                "Password must contain 1 or more uppercase characters.\n" +
                                "Password must contain 1 or more special characters.",
                            "name: size must be between 4 and 40",
                            "Passwords don't match"
                        )
                    )
                )
        }

        @Test
        @DisplayName("ServerException is expected since password confirmation not provided")
        @Throws(Exception::class)
        fun givenPassConfNotProvided_whenPatchMe_thenThrowServerException() {
            // Given
            updateUserDto.name = 10.randomStringGenerator()
            updateUserDto.password = TestConstants.USER_PASSWORD
            updateUserDto.passwordConfirmation = null
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch("${BaseController.V1_USER_URL}/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(updateUserDto))
                .header("Authorization", "Bearer $token")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.messageId)))
                .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.JSON_SCHEMA_VALIDATOR.text)))
                .andExpect(jsonPath("$.exception.variables", hasSize<Any>(1)))
                .andExpect(jsonPath("$.exception.variables", Matchers.containsInAnyOrder("Passwords don't match")))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenPatchMe_thenReturn200() {
            // Given
            updateUserDto.name = TestConstants.USER_NAME
            updateUserDto.password = TestConstants.USER_PASSWORD
            updateUserDto.passwordConfirmation = TestConstants.USER_PASSWORD
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .patch("${BaseController.V1_USER_URL}/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValueAsString(updateUserDto))
                .header("Authorization", "Bearer $token")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name", equalTo(USER_NAME)))
                .andExpect(jsonPath("$.email", equalTo(USER_EMAIL)))
                .andExpect(jsonPath("$.roles", hasSize<Any>(1)))
                .andExpect(jsonPath("$.roles[0].name", equalTo(RoleName.ROLE_ADMIN.role)))
                .andExpect(jsonPath("$.emailActivatedAt", IsNull.notNullValue()))
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Get All Users")
    internal inner class GetAllUsersTest {

        @Test
        @DisplayName("ServerException is expected since sort column is invalid")
        fun givenSortColumnIsInvalid_whenAllUsers_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .get("${BaseController.V1_USER_URL}?sortBy=invalid")
                .header("Authorization", "Bearer $token")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.exception.statusCode", equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.exception.error.id", equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.messageId)))
                .andExpect(jsonPath("$.exception.error.text", equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.text)))
                .andExpect(jsonPath("$.exception.variables", hasSize<Any>(1)))
                .andExpect(
                    jsonPath(
                        "$.exception.variables",
                        Matchers.containsInAnyOrder("Invalid sort column: invalid")
                    )
                )
        }

        @Test
        @DisplayName("Happy Path")
        fun given_whenAllUsers_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .get(BaseController.V1_USER_URL)
                .header("Authorization", "Bearer $token")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.page", equalTo(1)))
                .andExpect(jsonPath("$.pages", equalTo(1)))
                .andExpect(jsonPath("$.total", greaterThan(1)))
                .andExpect(jsonPath("$.sort", equalTo("asc")))
                .andExpect(jsonPath("$.sortBy", equalTo("id")))
                .andExpect(jsonPath("$.items.length()", greaterThan(1)))
        }
    }

    /**
     * @param value -- an object that want to be serialized
     * @return -- string
     * @throws JsonProcessingException -- throws JsonProcessingException
     */
    @Throws(JsonProcessingException::class)
    private fun writeValueAsString(value: Any): String = objectMapper.writeValueAsString(value)

    private fun login(loginRequest: LoginRequest): ResultActions =
        MockMvcRequestBuilders
            .post("${BaseController.V1_AUTH_URL}/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(writeValueAsString(loginRequest))
            .run { mockMvc.perform(this) }

    private val token: String
        get() =
            LoginRequest(email = USER_EMAIL, password = TestConstants.USER_PASSWORD)
                .run { login(loginRequest = this) }
                .run { this.andReturn().response.contentAsString }
                .run { objectMapper.readValue(this, UserWrapperResponse::class.java) }
                .run { this.token }
}
