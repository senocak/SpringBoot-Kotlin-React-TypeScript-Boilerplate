package com.github.senocak.auth.config

import javax.sql.DataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "spring.datasource")
class DataSourceConfig {
    var url: String? = null
    var username: String? = null
    var password: String? = null
    var driverClassName: String? = null

    @Bean
    @Primary
    fun dataSource(): DataSource =
        when {
            url!!.contains(other = "jdbc:postgresql") -> DriverManagerDataSource()
                .also { db: DriverManagerDataSource ->
                    db.url = url
                    db.username = username
                    db.password = password
                    //db.setDriverClassName(dataSourceProperties.dbtype)
                }
            else -> throw RuntimeException("Not configured")
        }

    override fun toString(): String =
        "DataSourceConfig(url=$url, username=$username, password=$password, driverClassName=$driverClassName)"
}
