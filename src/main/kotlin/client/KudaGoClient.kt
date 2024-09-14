package org.tinkoff.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.tinkoff.dto.News
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.exp

class KudaGoClient {
    private val baseUrlNews =
        "https://kudago.com/public-api/v1.4/news/?fields=id,title,slug,description,publication_date,favorites_count,comments_count&expand=&order_by=&text_format=text&ids=&location=&actual_only=true"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class NewsResponse(val results: List<News>)

    suspend fun getNews(count: Int = 100): List<News> {
        val response: NewsResponse = client.get(baseUrlNews) {
            parameter("page_size", count)
//            parameter("order_by", "publication_date")
            parameter("location", "spb")
        }.body() ?: throw Exception("Empty response")

        return response.results.map { news ->
            news.copy(rating = calculateRating(news.favoritesCount ?: 0, news.commentsCount ?: 0))
        }
    }
}

fun calculateRating(favoritesCount: Int, commentsCount: Int): Double {
    return 1 / (1 + exp(-(favoritesCount.toDouble() / (commentsCount + 1))))
}

fun List<News>.getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    return this.filter { news ->
        val publicationDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(news.publicationDate*1000), ZoneId.systemDefault()).toLocalDate()
        println(news.publicationDate.toString() + " " + publicationDate.toString())
        publicationDate in period
    }.sortedByDescending { it.rating }
        .take(count)
}

fun saveNews(path: String, news: Collection<News>) {
    val filePath = Paths.get(path)

    if (!Files.exists(filePath)) {
        Files.createFile(filePath)
    }

    val csv = news.joinToString("\n") { news ->
        "\"${news.id}\",\"${news.title}\",\"${news.publicationDate}\",\"${news.slug}\",\"${news.place}\",\"${news.description}\",\"${news.siteUrl}\",\"${news.favoritesCount}\",\"${news.commentsCount}\",\"${news.rating}\""
    }

    File(path).writeText(csv)
}