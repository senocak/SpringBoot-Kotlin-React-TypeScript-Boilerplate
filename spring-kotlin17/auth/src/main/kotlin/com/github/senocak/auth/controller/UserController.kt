package com.github.senocak.auth.controller

import com.github.senocak.auth.domain.User
import com.github.senocak.auth.domain.dto.ExceptionDto
import com.github.senocak.auth.domain.dto.PageRequestBuilder
import com.github.senocak.auth.domain.dto.PaginationCriteria
import com.github.senocak.auth.domain.dto.UpdateUserDto
import com.github.senocak.auth.domain.dto.UserPaginationDTO
import com.github.senocak.auth.domain.dto.UserResponse
import com.github.senocak.auth.domain.dto.UserRevisionPaginationDTO
import com.github.senocak.auth.domain.dto.UserWrapperResponse
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.security.Authorize
import com.github.senocak.auth.service.MessageSourceService
import com.github.senocak.auth.service.UserService
import com.github.senocak.auth.util.AppConstants.ADMIN
import com.github.senocak.auth.util.AppConstants.DEFAULT_PAGE_NUMBER
import com.github.senocak.auth.util.AppConstants.DEFAULT_PAGE_SIZE
import com.github.senocak.auth.util.AppConstants.SECURITY_SCHEME_NAME
import com.github.senocak.auth.util.AppConstants.USER
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.convertEntityToDto
import com.github.senocak.auth.util.convertEntityToRevisionDto
import com.github.senocak.auth.util.logger
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.Pattern
import org.slf4j.Logger
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.history.Revision
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@Authorize(roles = [ADMIN, USER])
@RequestMapping(BaseController.V1_USER_URL)
@Tag(name = "User", description = "User Controller")
class UserController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val messageSourceService: MessageSourceService
) : BaseController() {
    private val log: Logger by logger()

    @Throws(ServerException::class)
    @Operation(
        summary = "Get me",
        tags = ["User"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "successful operation",
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserWrapperResponse::class)))
            ),
            ApiResponse(
                responseCode = "500",
                description = "internal server error occurred",
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class)))
            )
        ],
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME, scopes = [ADMIN, USER])]
    )
    @GetMapping("/me")
    fun me(
        @Parameter(description = "Show history", required = false)
        @RequestParam(required = false, defaultValue = "false")
        history: Boolean,
        @Parameter(name = "page", description = "Page number", example = DEFAULT_PAGE_NUMBER)
        @RequestParam(defaultValue = "1", required = false)
        page: Int,
        @Parameter(name = "size", description = "Page size", example = DEFAULT_PAGE_SIZE)
        @RequestParam(defaultValue = "\${spring.data.web.pageable.default-page-size:10}", required = false)
        size: Int
    ): UserResponse = run {
        val me = userService.loggedInUser()
        var userRevisionPaginationDTO: UserRevisionPaginationDTO? = null
        if (history) {
            val pr: PageRequest = PageRequestBuilder.build(paginationCriteria = PaginationCriteria(page = page, size = size))
            val revisionPage: Page<Revision<Long, User>> = userService.findRevisions(id = me.id!!, pr = pr)
            userRevisionPaginationDTO = UserRevisionPaginationDTO(
                pageModel = revisionPage,
                items = revisionPage.content.map { it: Revision<Long, User> -> it.convertEntityToRevisionDto() }.toList()
            )
        }
        me.convertEntityToDto()
            .apply {
                if (userRevisionPaginationDTO != null) {
                    this.history = userRevisionPaginationDTO
                }
            }
    }

    @PatchMapping("/me")
    @Operation(
        summary = "Update user by username",
        tags = ["User"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "successful operation",
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = HashMap::class)))
            ),
            ApiResponse(
                responseCode = "500",
                description = "internal server error occurred",
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class)))
            )
        ],
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME, scopes = [ADMIN, USER])]
    )
    @Throws(ServerException::class)
    fun patchMe(
        request: HttpServletRequest,
        @Parameter(description = "Request body to update", required = true) @Validated @RequestBody userDto: UpdateUserDto,
        resultOfValidation: BindingResult
    ): UserResponse {
        validate(resultOfValidation = resultOfValidation)
        val user: User = userService.loggedInUser()
        val name: String? = userDto.name
        if (!name.isNullOrEmpty()) {
            user.name = name
        }
        val password: String? = userDto.password
        val passwordConfirmation: String? = userDto.passwordConfirmation
        if (!password.isNullOrEmpty()) {
            if (passwordConfirmation.isNullOrEmpty()) {
                messageSourceService.get(code = "password_confirmation_not_provided")
                    .apply { log.error(this) }
                    .run {
                        throw ServerException(
                            omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                            variables = arrayOf(this),
                            statusCode = HttpStatus.BAD_REQUEST
                        )
                    }
            }
            if (passwordConfirmation != password) {
                messageSourceService.get(code = "password_and_confirmation_not_matched")
                    .apply { log.error(this) }
                    .run {
                        throw ServerException(
                            omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                            variables = arrayOf(this),
                            statusCode = HttpStatus.BAD_REQUEST
                        )
                    }
            }
            user.password = passwordEncoder.encode(password)
        }
        return userService.save(user = user)
            .run user@{
                this@user.convertEntityToDto()
            }
    }

    @Throws(ServerException::class)
    @Operation(
        summary = "All Users",
        tags = ["User"],
        responses = [
            ApiResponse(
                responseCode = "200",
                description = MediaType.APPLICATION_JSON_VALUE,
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = UserPaginationDTO::class)))
            ),
            ApiResponse(
                responseCode = "500",
                description = "internal server error occurred",
                content = arrayOf(Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ExceptionDto::class)))
            )
        ],
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME, scopes = [ADMIN])]
    )
    @Authorize(roles = [ADMIN])
    @GetMapping
    fun allUsers(
        @Parameter(name = "page", description = "Page number", example = DEFAULT_PAGE_NUMBER)
        @RequestParam(defaultValue = "1", required = false)
        page: Int,
        @Parameter(name = "size", description = "Page size", example = DEFAULT_PAGE_SIZE)
        @RequestParam(defaultValue = "\${spring.data.web.pageable.default-page-size:10}", required = false)
        size: Int,
        @Parameter(name = "sortBy", description = "Sort by column", example = "id")
        @RequestParam(defaultValue = "id", required = false)
        sortBy: String,
        @Parameter(name = "sort", description = "Sort direction", schema = Schema(type = "string", allowableValues = ["asc", "desc"]))
        @RequestParam(defaultValue = "asc", required = false)
        @Pattern(regexp = "asc|desc")
        sort: String,
        @Parameter(name = "q", description = "Search keyword", example = "lorem")
        @RequestParam(required = false)
        q: String?
    ): UserPaginationDTO =
        arrayListOf("id", "name", "email")
            .run {
                if (this.none { it == sortBy }) {
                    messageSourceService.get(code = "invalid_sort_column", params = arrayOf(sortBy))
                        .also { log.error(it) }
                        .run error@{
                            throw ServerException(
                                omaErrorMessageType = OmaErrorMessageType.BASIC_INVALID_INPUT,
                                variables = arrayOf(this@error),
                                statusCode = HttpStatus.BAD_REQUEST
                            )
                        }
                }
                PaginationCriteria(page = page, size = size)
                    .also { it: PaginationCriteria ->
                        it.sortBy = sortBy
                        it.sort = sort
                        it.columns = this
                    }
                    .run paginationCriteria@{
                        userService.findAllUsers(
                            specification = userService.createSpecificationForUser(q = q),
                            pageRequest = PageRequestBuilder.build(paginationCriteria = this@paginationCriteria)
                        )
                    }
                    .run messagePage@{
                        UserPaginationDTO(
                            pageModel = this@messagePage,
                            items = this@messagePage.content.map { it: User -> it.convertEntityToDto() }.toList(),
                            sortBy = sortBy,
                            sort = sort
                        )
                    }
            }
}
