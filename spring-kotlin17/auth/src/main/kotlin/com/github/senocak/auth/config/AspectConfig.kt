package com.github.senocak.auth.config

import com.github.senocak.auth.util.logger
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.Logger
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Aspect
@Configuration
@Profile("!integration-test")
class AspectConfig {
    private val log: Logger by logger()

    @Before(value = "execution(* com.github.senocak.auth.controller.*.*(..))")
    fun logStatementBefore(joinPoint: JoinPoint?) = log.info("Starting executing $joinPoint")

    @After(value = "execution(* com.github.senocak.auth.controller.*.*(..))")
    fun logStatementAfter(joinPoint: JoinPoint?) = log.info("Complete executing of $joinPoint")

    @Around(value = "execution(* com.github.senocak.auth.controller.*.*(..))")
    @Throws(Throwable::class)
    fun timeTracker(joinPoint: ProceedingJoinPoint): Any = logStatement(joinPoint = joinPoint)

    /**
     * @param joinPoint the join point
     * @return the result of the join point
     */
    @Throws(Throwable::class)
    private fun logStatement(joinPoint: ProceedingJoinPoint): Any {
        val start = System.currentTimeMillis()
        val methodArguments = joinPoint.args
        val builder = StringBuilder()
        if (methodArguments != null) {
            for (arg in methodArguments) {
                if (arg != null) {
                    builder.append(arg).append(",")
                }
            }
        }
        val obj = joinPoint.proceed()
        val timeTaken = System.currentTimeMillis() - start
        log.trace("Method: ${joinPoint.signature.name} invoke with arguments ${builder}. Took $timeTaken ms")
        return obj
    }
}
