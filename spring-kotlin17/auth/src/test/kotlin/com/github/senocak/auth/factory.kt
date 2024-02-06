package com.github.senocak.auth

import com.github.senocak.auth.TestConstants.USER_EMAIL
import com.github.senocak.auth.TestConstants.USER_NAME
import com.github.senocak.auth.TestConstants.USER_PASSWORD
import com.github.senocak.auth.domain.Role
import com.github.senocak.auth.domain.User
import com.github.senocak.auth.util.RoleName
import java.util.ArrayList
import java.util.Date

fun createTestUser(): User =
    User(name = USER_NAME, email = USER_EMAIL, password = USER_PASSWORD)
        .also {
            it.roles = arrayListOf<Role>()
                .also { list: ArrayList<Role> -> list.add(RoleName.ROLE_USER.createRole()) }
                .also { list: ArrayList<Role> -> list.add(RoleName.ROLE_ADMIN.createRole()) }
            it.emailActivatedAt = Date()
        }

fun RoleName.createRole(): Role = Role().also { it.name = this}
