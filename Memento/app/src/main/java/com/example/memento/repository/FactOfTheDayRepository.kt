package com.example.memento.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FactOfTheDayRepository @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchFactForToday(): String? = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        runCatching {
            val url = URL(
                "https://en.wikipedia.org/api/rest_v1/feed/onthisday/events" +
                "/${today.monthValue}/${today.dayOfMonth}"
            )
            val events = json.parseToJsonElement(url.readText())
                .jsonObject["events"]?.jsonArray ?: return@runCatching null
            if (events.isEmpty()) return@runCatching null
            val event = events.random().jsonObject
            val year = event["year"]?.jsonPrimitive?.content ?: ""
            val text = event["text"]?.jsonPrimitive?.content ?: return@runCatching null
            "In $year, $text"
        }.onFailure { Log.w("FactOfTheDay", "Failed to fetch fact", it) }
            .getOrNull()
    }
}
