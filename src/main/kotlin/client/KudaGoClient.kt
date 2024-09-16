package org.tinkoff.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.tinkoff.dto.News
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class KudaGoClient {
    val logger = LoggerFactory.getLogger("KudaGoClient")

    private val baseUrlNews =
        "https://kudago.com/public-api/v1.4/news/?fields=id,title,slug,description,publication_date,favorites_count,comments_count&expand=&order_by=&text_format=text&ids=&location=&actual_only=true&page=&page_size="

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class NewsResponse(val results: List<News>)

    suspend fun getNews(count: Int = 100, page: Int = 1): List<News> {
        logger.info("Fetching news: count=$count, page=$page")

        val response: NewsResponse = client.get(baseUrlNews) {
            parameter("page_size", count)
            parameter("location", "spb")
            parameter("page", page)
        }.body() ?: throw Exception("Empty response")

        logger.debug("Fetched ${response.results.size} news items")
        return response.results
    }
}

suspend fun getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    val logger = LoggerFactory.getLogger("getMostRatedNews")
    val allNews = mutableSetOf<News>()
    var page = 1
    val pageSize = 100

    while (true) {
        logger.info("Fetching page $page")
        val response: List<News> = KudaGoClient().getNews(pageSize, page)
        val filteredNews = response.filter { news ->
            val publicationDate =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(news.publicationDate * 1000), ZoneId.systemDefault())
                    .toLocalDate()
            publicationDate in period
        }

        allNews.addAll(filteredNews)
        logger.debug("Filtered ${filteredNews.size} news items")

        if (filteredNews.size < pageSize) break
        page++
    }

    val sortedNews = allNews.sortedByDescending { it.rating }.take(count)
    logger.info("Returning ${sortedNews.size} most rated news items")
    return sortedNews
}

fun saveNews(path: String, news: Collection<News>) {
    val logger = LoggerFactory.getLogger("saveNews")
    val filePath = Paths.get(path)

    if (!Files.exists(filePath)) {
        Files.createFile(filePath)
        logger.info("Created new file at $path")
    } else {
        logger.warn("File already exists at $path")
    }

    val csv = news.joinToString("\n") { news ->
        "\"${news.id}\",\"${news.title}\",\"${news.publicationDate}\",\"${news.slug}\",\"${news.place}\",\"${news.description}\",\"${news.siteUrl}\",\"${news.favoritesCount}\",\"${news.commentsCount}\",\"${news.rating}\""
    }

    File(path).writeText(csv)
    logger.info("Saved ${news.size} news items to $path")
}