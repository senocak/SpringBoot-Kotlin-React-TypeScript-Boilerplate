import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
}

group = "com.github.senocak"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

ktlint {
    filter {
        // exclude("auth.kt") // Replace your file name here
        // include("**/kotlin/**")
    }
}

repositories {
    mavenCentral()
}
val jjwt = "0.11.5"
val debezium = "2.7.0.Alpha1"
val cucumber = "4.2.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.data:spring-data-envers")
    implementation("org.hibernate:hibernate-envers:6.4.0.Final")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("redis.clients:jedis:3.3.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("io.jsonwebtoken:jjwt-api:$jjwt")
    implementation("io.jsonwebtoken:jjwt-impl:$jjwt")
    implementation("io.jsonwebtoken:jjwt-jackson:$jjwt")
    implementation("org.flywaydb:flyway-core")
    implementation("com.github.gavlyukovskiy:datasource-proxy-spring-boot-starter:1.9.0")
    implementation("org.passay:passay:1.6.2")
    // implementation("net.jodah:expiringmap:0.5.10")
    implementation("commons-io:commons-io:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    // testImplementation("org.springframework.amqp:spring-rabbit-test")
    //testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.instancio:instancio-junit:3.7.1")
    testImplementation("io.cucumber:cucumber-java:$cucumber")
    testImplementation("io.cucumber:cucumber-junit:$cucumber")
    testImplementation("io.cucumber:cucumber-spring:$cucumber")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "1G"
    val testType: String = "unit"
        .takeUnless { project.hasProperty("profile") }
        ?: "${project.property("profile")}"
    println(message = "Profile test type: $testType")
    when (testType) {
        "integration" -> include("**/*IT.*")
        else -> include("**/*Test.*", "**/*IT.*")
    }
}

// Integration test task (assuming it's named integrationTest)
tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests"
    group = "Verification"
    include("**/*IT.*")
}
