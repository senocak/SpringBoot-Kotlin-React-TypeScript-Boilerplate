package com.github.senocak.auth.controller;

import com.github.senocak.auth.util.logger
import org.slf4j.Logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.util.concurrent.CopyOnWriteArrayList

@RestController
@RequestMapping(EventController.URL)
class EventController: BaseController() {
    private val log: Logger by logger()
    private val emitters: CopyOnWriteArrayList<SseEmitter> = CopyOnWriteArrayList()
    val bean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connect(): SseEmitter =
            SseEmitter(Long.MAX_VALUE)
                .also {
                    it.onCompletion { emitters.remove(it).also { log.info("Completed") } }
                    //it.onTimeout { log.info("SseEmitter is timed out") }
                    //it.onError { ex: Throwable? -> log.info("SseEmitter got error:", ex) }
                    it.send(SseEmitter.event().name("spring").data("connected"))
                    emitters.add(it)
                    it.send(SseEmitter.event().name("server").data(bean.systemLoadAverage))
                    it.send(SseEmitter.event().name("server").data(bean.availableProcessors))
                }

    @PostMapping
    fun postQuestion(@RequestBody question: String): String {
        for(emitter: SseEmitter in emitters) {
            try {
                emitter.send(SseEmitter.event().name("spring").data(question))
            } catch (e: IOException) {
                log.warn("IOException occurred: ${e.message}")
            }
        }
        return "ok"
    }

    companion object {
        const val URL = "/api/v1/sse"
    }
}