package com.github.senocak.auth

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/test/resources"],
    plugin = ["pretty", "html:target/cucumber-reports", "json:target/cucumber.json", "junit:target/cucumber-reports/Cucumber.xml"],
    glue = ["stepdefs"]
)
class CucumberTest