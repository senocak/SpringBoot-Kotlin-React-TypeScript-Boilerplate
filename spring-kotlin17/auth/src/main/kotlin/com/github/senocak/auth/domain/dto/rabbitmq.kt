package com.github.senocak.auth.domain.dto

import com.github.senocak.auth.util.Action

data class ServiceData(
    var action: Action? = null,
    var message: String? = null
)
