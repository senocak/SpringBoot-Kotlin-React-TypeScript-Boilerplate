package com.github.senocak.auth.service

import com.github.senocak.auth.domain.Role
import com.github.senocak.auth.domain.RoleRepository
import com.github.senocak.auth.exception.ServerException
import com.github.senocak.auth.util.OmaErrorMessageType
import com.github.senocak.auth.util.RoleName
import com.github.senocak.auth.util.logger
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val messageSourceService: MessageSourceService
) {
    private val log: Logger by logger()

    /**
     * @param roleName -- enum variable to retrieve from db
     * @return -- Role object retrieved from db
     */
    fun findByName(roleName: RoleName): Role = roleRepository.findByName(roleName = roleName)
        ?: throw ServerException(
            omaErrorMessageType = OmaErrorMessageType.MANDATORY_INPUT_MISSING,
            variables = arrayOf(messageSourceService.get(code = "role_not_found")),
            statusCode = HttpStatus.NOT_FOUND
        )
            .also { log.error("User Role is not found") }
}