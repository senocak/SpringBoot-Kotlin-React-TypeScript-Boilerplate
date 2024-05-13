package com.github.senocak.auth.controller

import com.github.senocak.auth.TestConstants
import com.github.senocak.auth.createTestUser
import com.github.senocak.auth.domain.User
import com.github.senocak.auth.domain.dto.UpdateUserDto
import com.github.senocak.auth.domain.dto.UserPaginationDTO
import com.github.senocak.auth.domain.dto.UserResponse
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.service.MessageSourceService
import com.github.senocak.auth.service.UserService
import com.github.senocak.auth.util.OmaErrorMessageType
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.function.Executable
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for UserController")
class UserControllerTest {
    @InjectMocks lateinit var userController: UserController
    private val userService: UserService = mock<UserService>()
    private val passwordEncoder: PasswordEncoder = mock<PasswordEncoder>()
    private val messageSourceService: MessageSourceService = mock<MessageSourceService>()
    private val bindingResult: BindingResult = mock<BindingResult>()
    private val user: User = createTestUser()

    @Nested
    internal inner class GetMeTest {

        @Test
        @Throws(ServerException::class)
        fun givenServerException_whenGetMe_thenThrowServerException() {
            // Given
            doThrow(toBeThrown = ServerException::class).`when`(userService).loggedInUser()
            // When
            val closureToTest = Executable { userController.me(history = false, page = 0, size = 100) }
            // Then
            assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenGetMe_thenReturn200() {
            // Given
            doReturn(value = user).`when`(userService).loggedInUser()
            // When
            val getMe: UserResponse = userController.me(history = false, page = 0, size = 100)
            // Then
            assertNotNull(getMe)
            assertEquals(user.email, getMe.email)
            assertEquals(user.name, getMe.name)
        }
    }

    @Nested
    internal inner class PatchMeTest {
        private val updateUserDto: UpdateUserDto = UpdateUserDto()
        private val httpServletRequest: HttpServletRequest = Mockito.mock(HttpServletRequest::class.java)

        @Test
        @Throws(ServerException::class)
        fun givenNullPasswordConf_whenPatchMe_thenThrowServerException() {
            // Given
            doReturn(value = user).`when`(userService).loggedInUser()
            updateUserDto.password = "pass1"
            // When
            val closureToTest = Executable { userController.patchMe(httpServletRequest, updateUserDto, bindingResult) }
            // Then
            assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun givenInvalidPassword_whenPatchMe_thenThrowServerException() {
            // Given
            doReturn(value = user).`when`(userService).loggedInUser()
            updateUserDto.password = "pass1"
            updateUserDto.passwordConfirmation = "pass2"
            // When
            val closureToTest = Executable { userController.patchMe(httpServletRequest, updateUserDto, bindingResult) }
            // Then
            assertThrows(ServerException::class.java, closureToTest)
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenPatchMe_thenThrowServerException() {
            // Given
            doReturn(value = user).`when`(userService).loggedInUser()
            updateUserDto.name = TestConstants.USER_NAME
            updateUserDto.password = "pass1"
            updateUserDto.passwordConfirmation = "pass1"
            doReturn(value = "pass1").`when`(passwordEncoder).encode("pass1")
            doReturn(value = user).`when`(userService).save(user = user)
            // When
            val patchMe: UserResponse = userController.patchMe(httpServletRequest, updateUserDto, bindingResult)
            // Then
            assertNotNull(patchMe)
            assertEquals(user.email, patchMe.email)
            assertEquals(user.name, patchMe.name)
        }
    }

    @Nested
    internal inner class AllUsersTest {

        @Test
        @Throws(ServerException::class)
        fun givenInvalidSortBy_whenAllUsers_thenThrowServerException() {
            // Given
            doReturn(value = "invalid_sort_column").`when`(messageSourceService).get(code = "invalid_sort_column", params = arrayOf("sortBy"))
            // When
            val closureToTest = Executable {
                userController.allUsers(page = 1, size = 1, sortBy = "sortBy", sort = "sort", q = "q")
            }
            // Then
            val assertThrows: ServerException = assertThrows(ServerException::class.java, closureToTest)
            assertEquals(OmaErrorMessageType.BASIC_INVALID_INPUT, assertThrows.omaErrorMessageType)
            assertEquals(HttpStatus.BAD_REQUEST, assertThrows.statusCode)
            assertNotNull(assertThrows.variables)
            assertEquals(1, assertThrows.variables.size)
            assertEquals("invalid_sort_column", assertThrows.variables[0])
        }

        @Test
        @Throws(ServerException::class)
        fun given_whenAllUsers_thenAssertResult() {
            // Given
            doReturn(value = user).`when`(userService).loggedInUser()
            val specification: Specification<User> = mock<Specification<User>>()
            doReturn(value = specification).`when`(userService).createSpecificationForUser(q = "q")
            val listOfUser: List<User> = listOf(element = user)
            val pages: Page<User> = PageImpl(listOfUser)
            doReturn(value = pages).`when`(userService).findAllUsers(specification = eq(specification), pageRequest = any<Pageable>())
            // When
            val response: UserPaginationDTO = userController.allUsers(page = 1, size = 1, sortBy = "id", sort = "sort", q = "q")
            // Then
            assertNotNull(response)
            assertEquals("id", response.sortBy)
            assertEquals("sort", response.sort)
            assertEquals(1, response.page)
            assertEquals(1, response.pages)
            assertEquals(1, response.total)
            assertNotNull(response.items)
            assertEquals(1, response.items!!.size)
            assertNotNull(response.items!![0])
            assertEquals(user.name, response.items!![0].name)
            assertEquals(user.email, response.items!![0].email)
            assertNotNull(response.items!![0].roles)
            assertEquals(2, response.items!![0].roles.size)
            assertNotNull(response.items!![0].emailActivatedAt)
        }
    }
}
