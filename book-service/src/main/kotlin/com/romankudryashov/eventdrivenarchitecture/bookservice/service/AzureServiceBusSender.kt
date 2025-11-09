package com.romankudryashov.eventdrivenarchitecture.bookservice.service

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class AzureServiceBusSender(
    @Value("\${azure.servicebus.connection-string}") private val connectionString: String,
    @Value("\${azure.servicebus.queue-name}") private val queueName: String,
    @Value("\${azure.servicebus.send.max-retries:3}") private val maxRetries: Int,
    @Value("\${azure.servicebus.send.initial-backoff-ms:200}") private val initialBackoffMs: Long,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val sender: ServiceBusSenderAsyncClient = ServiceBusClientBuilder()
        .connectionString(connectionString)
        .sender()
        .queueName(queueName)
        .buildAsyncClient()

    /**
     * Send a "book created" event to Service Bus asynchronously.
     * This is a suspend function using coroutines and will retry on transient failures.
     *
     * @param payloadObject the object to serialize as JSON
     * @param messageId optional message id; a UUID will be generated if null
     * @param correlationId optional correlation id
     */
    suspend fun sendBookCreatedEvent(payloadObject: Any, messageId: String? = null, correlationId: String? = null) {
        val payloadJson = try {
            log.info("Serializing payload for Service Bus: {}", payloadObject)
            objectMapper.writeValueAsString(payloadObject)
        } catch (ex: Exception) {
            log.error("Failed to serialize payload for Service Bus", ex)
            throw ex
        }

        val sbMessage = ServiceBusMessage(payloadJson).apply {
            contentType = "application/json"
            this.messageId = messageId ?: UUID.randomUUID().toString()
            this.correlationId = correlationId
            // Add any custom application properties if needed:
            // this.applicationProperties["eventType"] = "BookCreated"
        }

        var attempt = 0
        var backoff = initialBackoffMs
        while (true) {
            try {
                // sendMessage returns a Mono\<Void\>; await as coroutine
                sender.sendMessage(sbMessage).then().awaitSingleOrNull()
                log.debug("Sent book created event to Service Bus queue {}: messageId={}", queueName, sbMessage.messageId)
                return
            } catch (ex: Exception) {
                attempt++
                val isLast = attempt > maxRetries
                log.warn("Attempt {} failed to send message to Service Bus (queue={}): {}", attempt, queueName, ex.message)
                if (isLast) {
                    log.error("Exceeded max retries ({}) for sending messageId={} to Service Bus", maxRetries, sbMessage.messageId, ex)
                    // swallow or rethrow depending on your outbox/compensation strategy; rethrow here so caller can handle
                    throw ex
                } else {
                    // exponential backoff with jitter
                    val jitter = (backoff * 0.2).toLong()
                    val waitMs = backoff + (-(jitter)..jitter).random()
                    log.debug("Retrying send in {} ms (attempt {}/{})", waitMs, attempt + 1, maxRetries)
                    delay(waitMs)
                    backoff = (backoff * 2).coerceAtMost(Duration.ofSeconds(30).toMillis())
                }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        try {
            sender.close()
            log.debug("Service Bus sender closed")
        } catch (ex: Exception) {
            log.warn("Error closing Service Bus sender", ex)
        }
    }

    // Simple extension to produce a random value in range for jitter
    private fun ClosedRange<Long>.random(): Long {
        val start = this.start
        val end = this.endInclusive
        if (start == end) return start
        val diff = end - start + 1
        return start + (kotlin.random.Random.nextLong(diff))
    }
}
