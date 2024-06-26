package com.github.senocak.auth.domain

import com.github.senocak.auth.util.RoleName
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RoleRepository : PagingAndSortingRepository<Role, UUID> {
    fun findByName(roleName: RoleName): Role?
}

@Repository
interface UserRepository :
    PagingAndSortingRepository<User, UUID>,
    CrudRepository<User, UUID>,
    JpaSpecificationExecutor<User>,
    RevisionRepository<User, UUID, Long> {
    fun findByEmail(email: String?): User?
    fun existsByEmail(email: String?): Boolean
}

interface EmailActivationTokenRepository : CrudRepository<EmailActivationToken, UUID> {
    fun findByUser(user: User): EmailActivationToken?
    fun findAllByUser(user: User): List<EmailActivationToken?>

    fun findByUserId(userId: UUID): EmailActivationToken?
    fun findByToken(token: String): EmailActivationToken?

    @Modifying
    @Query("DELETE FROM EmailActivationToken rt WHERE rt.user.id = ?1")
    fun deleteByUserId(userId: UUID)
}

interface JwtTokenRepository : CrudRepository<JwtToken, UUID> {
    fun findByEmail(email: String): JwtToken?
    fun findAllByEmail(email: String): List<JwtToken?>
    fun findByToken(token: String): JwtToken?
    fun findByTokenAndTokenType(token: String, type: String): JwtToken?
}

interface PasswordResetTokenRepository : CrudRepository<PasswordResetToken, UUID> {
    fun findByToken(token: String): PasswordResetToken?
    fun findByUserId(userId: UUID): PasswordResetToken?
}
