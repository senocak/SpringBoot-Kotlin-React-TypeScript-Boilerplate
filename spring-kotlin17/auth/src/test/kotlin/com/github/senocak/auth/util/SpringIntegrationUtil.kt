package com.github.senocak.auth.util

import org.apache.commons.io.IOUtils
import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequest
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.nio.charset.Charset

class SpringIntegrationUtil(
    private var randomPort: Int
) {
    private val restTemplate = RestTemplate()

    @Throws(IOException::class)
    fun executeGet(url: String) {
        val headers: MutableMap<String, String> = HashMap()
        headers["Accept"] = "application/json"
        val requestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
        val errorHandler = ResponseResultErrorHandler()

        restTemplate.errorHandler = errorHandler
        latestResponse = restTemplate.execute(
            "$BASE$randomPort$SUFFIX$url",
            HttpMethod.GET,
            requestCallback,
            { response -> if (errorHandler.hadError) errorHandler.getResults() else ResponseResults(response) }
        )
    }

    @Throws(IOException::class)
    fun executeDelete(url: String) {
        val headers: MutableMap<String, String> = HashMap()
        headers["Accept"] = "application/json"
        val requestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
        val errorHandler = ResponseResultErrorHandler()

        restTemplate.errorHandler = errorHandler
        latestResponse = restTemplate.execute(
            "$BASE$randomPort$SUFFIX$url",
            HttpMethod.DELETE,
            requestCallback,
            { response -> if (errorHandler.hadError) errorHandler.getResults() else ResponseResults(theResponse = response) }
        )
    }

    @Throws(IOException::class)
    fun executePut(url: String, entries: Map<String?, Any?>?) {
        val headers: MutableMap<String, String> = HashMap()
        headers["Accept"] = "application/json"
        headers["Content-Type"] = "application/json"
        val requestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
        if (entries != null) {
            requestCallback.body = JSONObject(entries).toString()
        }
        val errorHandler = ResponseResultErrorHandler()
        restTemplate.errorHandler = errorHandler
        latestResponse = restTemplate.execute(
            "$BASE$randomPort$SUFFIX$url",
            HttpMethod.PUT,
            requestCallback,
            { response -> if (errorHandler.hadError) errorHandler.getResults() else ResponseResults(theResponse = response) }
        )
    }

    fun executePost(url: String, entries: Map<String?, Any?>?) {
        val headers: MutableMap<String, String> = HashMap()
        headers["Accept"] = "application/json"
        headers["Content-Type"] = "application/json"
        val requestCallback = HeaderSettingRequestCallback(requestHeaders = headers)
        requestCallback.body = JSONObject(entries).toString()
        val errorHandler = ResponseResultErrorHandler()
        restTemplate.errorHandler = errorHandler
        latestResponse = restTemplate.execute(
            "$BASE$randomPort$SUFFIX$url",
            HttpMethod.POST,
            requestCallback,
            { response -> if (errorHandler.hadError) errorHandler.getResults() else ResponseResults(theResponse = response) }
        )
    }

    fun executePostWithMultipartFormData(
        url: String,
        bodyMap: LinkedMultiValueMap<String?, Any?>?
    ): ResponseEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers.accept = listOf(element = MediaType.APPLICATION_JSON)

        val requestEntity: HttpEntity<*> = HttpEntity<Any?>(bodyMap, headers)
        return restTemplate.exchange(
            "$BASE$randomPort$SUFFIX$url",
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )
    }

    private inner class ResponseResultErrorHandler : ResponseErrorHandler {
        private var results: ResponseResults? = null
        var hadError = false

        fun getResults(): ResponseResults? = results

        @Throws(IOException::class)
        override fun hasError(response: ClientHttpResponse): Boolean =
            response.statusCode.is5xxServerError
                .also { hadError = it }

        @Throws(IOException::class)
        override fun handleError(response: ClientHttpResponse) {
            results = ResponseResults(theResponse = response)
        }
    }

    companion object {
        var latestResponse: ResponseResults? = null
        private const val BASE = "http://localhost:"
        private const val SUFFIX = "/api/v1"
    }
}

class HeaderSettingRequestCallback(
    private val requestHeaders: Map<String, String>
) : RequestCallback {
    var body: String? = null

    @Throws(IOException::class)
    override fun doWithRequest(request: ClientHttpRequest) {
        val clientHeaders = request.headers
        for ((key, value) in requestHeaders) {
            clientHeaders.add(key, value)
        }
        if (null != body) {
            request.body.write(body!!.toByteArray())
        }
    }
}

class ResponseResults(val theResponse: ClientHttpResponse) {
    val body: String = IOUtils.toString(theResponse.body, Charset.defaultCharset())
}
