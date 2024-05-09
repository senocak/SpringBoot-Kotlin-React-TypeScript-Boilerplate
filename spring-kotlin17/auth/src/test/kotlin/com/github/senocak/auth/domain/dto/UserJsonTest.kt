package com.github.senocak.auth.domain.dto

import com.github.senocak.auth.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.boot.test.json.JsonContent

@JsonTest
class UserJsonTest {
    @Autowired
    private lateinit var jackson: JacksonTester<User>

    private val person: User = User(name = "anil", email = "anil@senocak.com", password = "password")
    private var asJson: JsonContent<User>? = null

    @BeforeEach
    fun setup() {
        asJson = jackson.write(person)
    }

    @Test
    fun serializeJson() {
        assertThat(asJson).hasEmptyJsonPathValue("id")
        assertThat(asJson).extractingJsonPathStringValue("name").isEqualTo("anil")
        assertThat(asJson).hasJsonPathStringValue("createdAt");
        assertThat(asJson).hasJsonPathStringValue("updatedAt")
        assertThat(asJson).hasJsonPathArrayValue("roles")
        assertThat(asJson).doesNotHaveJsonPath("not_exist_field")
    }

    @Test
    fun deserializeJson() {
        val dtoString = "{\"name\":\"anil\",\"email\":\"anil@senocak.com\",\"password\":\"password\",\"roles\":[],\"id\":null,\"createdAt\":\"2022-12-19T11:09:55.150+00:00\",\"updatedAt\":\"2022-12-19T11:09:55.150+00:00\"}";
        val dto: User = jackson.parseObject(dtoString)
        assertThat(dto.id).isNull()
        assertThat(dto.name).isEqualTo("anil")
        //assertEquals("anil", dto.name);
        assertThat(dto.email).isEqualTo("anil@senocak.com")
        assertThat(dto.password).isEqualTo("password")
        assertThat(dto.roles).isEmpty()
        assertThat(dto.createdAt).isNotNull
        assertThat(dto.updatedAt).isNotNull
    }
}