package com.github.senocak.auth.controller

import com.github.senocak.auth.util.changeLevel
import com.sun.management.OperatingSystemMXBean
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

@RestController
class GraphQLController {
    private val log = LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
    private val operatingSystemMXBean: OperatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
    private val dateFormat = SimpleDateFormat("YYYY-MM-DD HH:mm:ss")
    private val byte = 1L
    private val kb: Long = byte * 1000
    private val mb: Long = kb * 1000
    private val gb: Long = mb * 1000
    private val tb: Long = gb * 1000

    @GetMapping("/api/v1/ping")
    fun ping() = getPerformance()

    @QueryMapping
    fun getLogLevel(): String = log.level.levelStr

    @MutationMapping
    fun changeLogLevel(@Argument loglevel: String): String = run {
        log.changeLevel(loglevel = loglevel)
        getLogLevel()
    }

    /**
     * this is scheduled to run every minute
     */
    @Scheduled(cron = "0 * * ? * *")
    @Async
    fun printRuntimeDataEvery10Seconds() =
        MDC.put("userId", "scheduler")
            .run { getPerformance() }
            .run { MDC.remove("userId") }

    fun getPerformance(): Performance =
        Runtime.getRuntime()
            .run {
                Performance(
                    timestamp = dateFormat.format(Date()),
                    committedVirtualMemorySize = operatingSystemMXBean.committedVirtualMemorySize,
                    totalSwapSpaceSize = operatingSystemMXBean.totalSwapSpaceSize,
                    freeSwapSpaceSize = operatingSystemMXBean.freeSwapSpaceSize,
                    totalMemorySize = operatingSystemMXBean.totalMemorySize,
                    freeMemorySize = operatingSystemMXBean.freeMemorySize,
                    cpuLoad = operatingSystemMXBean.cpuLoad,
                    processCpuLoad = operatingSystemMXBean.processCpuLoad,
                    availableProcessors = this.availableProcessors(),
                    totalMemory = toHumanReadableSIPrefixes(size = this.totalMemory()),
                    maxMemory = toHumanReadableSIPrefixes(size = this.maxMemory()),
                    freeMemory = toHumanReadableSIPrefixes(size = this.freeMemory())
                )
            }
            .apply { log.info("$this") }

    private fun toHumanReadableSIPrefixes(size: Long): String =
        when {
            size >= tb -> formatSize(size = size, divider = tb, unitName = "TB")
            size >= gb -> formatSize(size = size, divider = gb, unitName = "GB")
            size >= mb -> formatSize(size = size, divider = mb, unitName = "MB")
            size >= kb -> formatSize(size = size, divider = kb, unitName = "KB")
            else -> formatSize(size = size, divider = byte, unitName = "Bytes")
        }

    private fun formatSize(size: Long, divider: Long, unitName: String): String =
        DecimalFormat("#.##").format(size.toDouble() / divider) + " " + unitName

    data class Performance(
        val timestamp: String,
        var committedVirtualMemorySize: Long = 0,
        var totalSwapSpaceSize: Long = 0,
        var freeSwapSpaceSize: Long = 0,
        var totalMemorySize: Long = 0,
        var freeMemorySize: Long = 0,
        var cpuLoad: Double = 0.0,
        var processCpuLoad: Double = 0.0,
        var availableProcessors: Int = 0,
        var totalMemory: String = "",
        var maxMemory: String = "",
        var freeMemory: String = ""
    )
}
