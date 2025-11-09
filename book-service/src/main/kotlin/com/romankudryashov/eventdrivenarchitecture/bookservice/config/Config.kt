package com.romankudryashov.eventdrivenarchitecture.bookservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.core.task.support.TaskExecutorAdapter
import java.util.concurrent.Executors

@Configuration
class Config {

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    fun asyncTaskExecutor(): AsyncTaskExecutor = TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor())

    @Bean
    fun objectMapper(): ObjectMapper =
        jacksonObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
}
