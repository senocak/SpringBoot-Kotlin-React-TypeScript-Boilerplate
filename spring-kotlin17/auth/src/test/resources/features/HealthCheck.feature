Feature: The health of the application can be checked
  Scenario: client makes call to GET /rest/version/1/ping
    Given the client calls '/ping'
    Then the client receives status code of 200
