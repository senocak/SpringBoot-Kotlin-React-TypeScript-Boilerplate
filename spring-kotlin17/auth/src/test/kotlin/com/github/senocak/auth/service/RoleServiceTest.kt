package com.github.senocak.auth.service

import com.github.senocak.auth.domain.Role
import com.github.senocak.auth.domain.RoleRepository
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.RoleName
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.function.Executable
import org.mockito.InjectMocks
import org.mockito.kotlin.doReturn
import org.springframework.http.HttpStatus

@Tag("unit")
@ExtendWith(MockitoExtension::class)
@DisplayName("Unit Tests for RoleService")
class RoleServiceTest {
    @InjectMocks lateinit var roleService: RoleService
    private val roleRepository: RoleRepository = Mockito.mock(RoleRepository::class.java)
    private val messageSourceService: MessageSourceService = Mockito.mock(MessageSourceService::class.java)

    @Test
    fun givenRoleName_whenFindByName_thenAssertResult() {
        // Given
        val role = Role()
        val roleName: RoleName = RoleName.ROLE_USER
        doReturn(value = role).`when`(roleRepository).findByName(roleName = roleName)
        // When
        val findByName: Role = roleService.findByName(roleName = roleName)
        // Then
        assertEquals(role, findByName)
    }

    @Test
    fun givenNullRoleName_whenFindByName_thenAssertResult() {
        // Given
        val roleName: RoleName = RoleName.ROLE_USER
        // When
        val closureToTest = Executable { roleService.findByName(roleName = roleName) }
        // Then
        val assertThrows: ServerException = assertThrows(ServerException::class.java, closureToTest)
        assertEquals(OmaErrorMessageType.MANDATORY_INPUT_MISSING, assertThrows.omaErrorMessageType)
        assertEquals(HttpStatus.NOT_FOUND, assertThrows.statusCode)
    }
}
