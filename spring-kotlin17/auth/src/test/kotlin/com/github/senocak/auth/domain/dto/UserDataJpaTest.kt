package com.github.senocak.auth.domain.dto

import com.github.senocak.auth.domain.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

/*
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:tc:mysql:8.0.1://localhost/spring?TC_INITSCRIPT=file:src/test/resources/db.sql",
    "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
    "spring.liquibase.enabled=false",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect",
    "spring.jpa.hibernate.ddl-auto=validate"
])
*/
@DataJpaTest
@ActiveProfiles(value = ["datajpa-test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserDataJpaTest {
    @Autowired private lateinit var userRepository: UserRepository

    @Autowired private lateinit var jdbcTemplate: JdbcTemplate

    // @Sql("/db.sql")
    @Test
    fun whenInjectInMemoryDataSource_thenReturnCorrectEmployeeCount() {
        // userRepository.save(User("name1","username1","email@email1.com","password1"))
        assertEquals(3, userRepository.findAll().count())

        val queryForList = jdbcTemplate.queryForList("select * from users")
        assertEquals(3, queryForList.size)
    }
}
