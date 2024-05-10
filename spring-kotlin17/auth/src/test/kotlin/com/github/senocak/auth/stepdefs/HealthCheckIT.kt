package com.github.senocak.auth.stepdefs

import com.github.senocak.auth.util.CucumberStepsDefinition
import com.github.senocak.auth.util.SpringIntegrationUtil
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatusCode

@CucumberStepsDefinition
class HealthCheckIT(
    @LocalServerPort private var localServerPort: Int
){
    private val stepCommon = SpringIntegrationUtil(randomPort = localServerPort)

    @Given("^the client calls '([^\"]*)'")
    fun the_client_issues_GET_version(url: String) {
        stepCommon.executeGet(url = url)
    }

    @When("^the client receives status code of (\\d+)$")
    fun the_client_receives_status_code_of(statusCode: Int) {
        val currentStatusCode: HttpStatusCode? = SpringIntegrationUtil.latestResponse?.theResponse?.statusCode
        Assertions.assertEquals(statusCode.toLong(), currentStatusCode?.value()?.toLong())
    }

    @Then("^the client receives response containing '([^\"]*)'")
    fun the_client_receives_response_containing(response: String) {
        org.hamcrest.MatcherAssert.assertThat(SpringIntegrationUtil.latestResponse?.body, CoreMatchers.containsString(response))
    }
}