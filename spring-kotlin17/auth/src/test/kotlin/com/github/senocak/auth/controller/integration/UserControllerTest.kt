package com.github.senocak.auth.controller.integration

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.senocak.auth.TestConstants
import com.github.senocak.auth.TestConstants.USER_EMAIL
import com.github.senocak.auth.config.SpringBootTestConfig
import com.github.senocak.auth.controller.BaseController
import com.github.senocak.auth.controller.UserController
import com.github.senocak.auth.domain.User
import com.github.senocak.auth.domain.UserRepository
import com.github.senocak.auth.domain.dto.UpdateUserDto
import com.github.senocak.auth.exception.RestExceptionHandler
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.service.MessageSourceService
import com.github.senocak.auth.service.UserService
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.RoleName
import com.github.senocak.auth.util.randomStringGenerator
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
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
 * @see UserController
 */
@SpringBootTestConfig
@DisplayName("Integration Tests for UserController")
class UserControllerTest {
    @Autowired private lateinit var userController: UserController
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var userRepository: UserRepository
    @SpyBean  private lateinit var userService: UserService
    @Autowired private lateinit var messageSourceService: MessageSourceService

    private lateinit var mockMvc: MockMvc
    private lateinit var user: User

    @BeforeEach
    @Throws(ServerException::class)
    fun beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(RestExceptionHandler(messageSourceService = messageSourceService))
            .build()
        user = userRepository.findByEmail(email = USER_EMAIL) ?: throw RuntimeException("user not found: $USER_EMAIL")
        doReturn(value = user).`when`(userService).loggedInUser()
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
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", equalTo(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", equalTo(user.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles", hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name", equalTo(RoleName.ROLE_ADMIN.role)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.emailActivatedAt", IsNull.notNullValue()))
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
                    hasSize<Any>(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.containsInAnyOrder(
                        "password: Password must be 6 or more characters in length.\n" +
                                "Password must contain 1 or more uppercase characters.\n" +
                                "Password must contain 1 or more special characters.",
                        "passwordConfirmation: Password must be 6 or more characters in length.\n" +
                                "Password must contain 1 or more uppercase characters.\n" +
                                "Password must contain 1 or more special characters.",
                        "name: size must be between 4 and 40",
                        "Passwords don't match"
                    )))
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
                    hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.containsInAnyOrder(
                        "Passwords don't match"
                    )))
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
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", equalTo(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", equalTo(user.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles", hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name", equalTo(RoleName.ROLE_ADMIN.role)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.emailActivatedAt", IsNull.notNullValue()))
        }
    }

    @Nested
    @Order(3)
    @DisplayName("Get All Users")
    internal inner class GetAllUsersTest {
        @Test
        @DisplayName("ServerException is expected since sort column is invalid")
        fun givenSortColumnIsInvalid_whenPatchMe_thenThrowServerException() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .get("${BaseController.V1_USER_URL}?sortBy=invalid")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.statusCode",
                    IsEqual.equalTo(HttpStatus.BAD_REQUEST.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.id",
                    IsEqual.equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.messageId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.error.text",
                    IsEqual.equalTo(OmaErrorMessageType.BASIC_INVALID_INPUT.text)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exception.variables",
                    Matchers.containsInAnyOrder(
                        "Invalid sort column: invalid"
                    )))
        }

        @Test
        @DisplayName("Happy Path")
        @Throws(Exception::class)
        fun given_whenGetMe_thenReturn200() {
            // Given
            val requestBuilder: RequestBuilder = MockMvcRequestBuilders
                .get("${BaseController.V1_USER_URL}/me")
            // When
            val perform: ResultActions = mockMvc.perform(requestBuilder)
            // Then
            perform
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", equalTo(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", equalTo(user.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles", hasSize<Any>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].name", equalTo(RoleName.ROLE_ADMIN.role)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.emailActivatedAt", IsNull.notNullValue()))
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
