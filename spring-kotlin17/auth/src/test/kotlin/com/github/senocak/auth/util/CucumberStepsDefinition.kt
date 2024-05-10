package com.github.senocak.auth.util

import com.github.senocak.auth.config.SpringBootTestConfig
import org.junit.jupiter.api.DisplayName

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTestConfig
@DisplayName("Integration Tests for Cucumber Scenarios")
annotation class CucumberStepsDefinition
