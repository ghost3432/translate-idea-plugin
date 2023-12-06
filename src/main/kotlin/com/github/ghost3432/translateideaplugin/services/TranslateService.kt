package com.github.ghost3432.translateideaplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.github.ghost3432.translateideaplugin.MyBundle
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class TranslateService(project: Project) {

    init {
        thisLogger().info(MyBundle.message("projectService", project.name))
    }

    private val httpClient = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }
    private val executor = Executors.newSingleThreadScheduledExecutor()

    private var translateTask: ScheduledFuture<*>? = null

    @Serializable
    data class SimplyTranslateResponse(@Required @SerialName("translated_text") val translatedText: String)

    fun translate(text: String, from: String, to: String, callback: (String) -> Unit) {
        val task = translateTask
        if (task != null) {
            if (!task.isCancelled && !task.isDone)
                task.cancel(true)

            translateTask = null
        }

        translateTask = executor.schedule({
            val uri = URI.create("https://simplytranslate.org/api/translate?engine=google&from=$from&to=$to&text=${URLEncoder.encode(text, Charset.defaultCharset())}")
            val request = HttpRequest.newBuilder()
                .uri(uri)
                .build()

            thisLogger().warn("Translating: $text")
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            thisLogger().warn("Translated: ${response.body()}")

            try {
                callback(json.decodeFromString<SimplyTranslateResponse>(response.body()).translatedText)
            } catch (e: Exception) {
                thisLogger().warn(e)
            }
        }, 500, TimeUnit.MILLISECONDS)
    }
}
