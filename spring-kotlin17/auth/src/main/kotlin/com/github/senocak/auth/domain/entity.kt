package com.github.senocak.auth.domain

import com.github.senocak.auth.util.RoleName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.UniqueConstraint
import org.hibernate.Hibernate
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import java.io.Serializable
import java.util.Date
import java.util.Objects
import java.util.UUID
import java.util.concurrent.TimeUnit

@MappedSuperclass
open class BaseDomain(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    var id: UUID? = null,
    @Column var createdAt: Date = Date(),
    @Column var updatedAt: Date = Date()
) : Serializable {
    @PrePersist
    protected open fun prePersist() {
        // id = UUID.randomUUID()
    }
}

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["email"])
    ]
)
data class User(
    @Column var name: String? = null,
    @Column var email: String? = null,
    @Column var password: String? = null
) : BaseDomain() {
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    @ManyToMany(fetch = FetchType.EAGER)
    var roles: List<Role> = arrayListOf()

    @OneToOne(mappedBy = "user")
    var emailActivationToken: EmailActivationToken? = null

    @Column(name = "email_activated_at", nullable = true)
    var emailActivatedAt: Date? = null
}

@Entity
@Table(name = "roles")
data class Role(
    @Column
    @Enumerated(EnumType.STRING)
    var name: RoleName? = null
) : BaseDomain()

@Entity
@Table(
    name = "email_activation_tokens",
    uniqueConstraints = [UniqueConstraint(columnNames = ["token"], name = "uk_email_activation_tokens_token")]
)
data class EmailActivationToken(
    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_email_activation_tokens_user_user_id")
    )
    var user: User? = null,

    @Column(nullable = false, length = 64)
    var token: String? = null
) : BaseDomain() {
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var expirationDate: Date = Date()

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other == null || Hibernate.getClass(this) != Hibernate.getClass(other) -> false
            else -> {
                val that: EmailActivationToken = other as EmailActivationToken
                id != null && id == that.id
            }
        }

    override fun hashCode(): Int = Objects.hash(user, token, expirationDate)
}

@RedisHash(value = "jwtTokens")
data class JwtToken(
    @org.springframework.data.annotation.Id
    @Indexed
    val token: String,

    val tokenType: String,

    @Indexed
    val email: String,

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    val timeToLive: Long = TimeUnit.MINUTES.toMillis(30)
)

@RedisHash("password_reset_tokens")
data class PasswordResetToken(
    @org.springframework.data.annotation.Id
    @Indexed
    val token: String,

    @Indexed
    val userId: UUID,

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    val timeToLive: Long = TimeUnit.MINUTES.toMillis(30)
)
