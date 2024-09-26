package org.tinkoff.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.tinkoff.dto.News
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class KudaGoClient {

    companion object {
        private const val BASE_URL_NEWS =
            "https://kudago.com/public-api/v1.4/news/?fields=id,title,slug,description,publication_date,favorites_count,comments_count&expand=&order_by=&text_format=text&ids=&location=&actual_only=true&page=&page_size="
        private const val PAGE_SIZE = 100
        private val logger = LoggerFactory.getLogger("KudaGoClient")
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class NewsResponse(val results: List<News> = emptyList())

    suspend fun getNews(count: Int = 100, page: Int = 1): List<News> {
        logger.info("Fetching news: count=$count, page=$page")

        val response: NewsResponse = client.get(BASE_URL_NEWS) {
            parameter("page_size", count)
            parameter("location", "spb")
            parameter("order_by", "publication_date")
            parameter("page", page)
        }.body() ?: throw Exception("Empty response")

        logger.debug("Fetched ${response.results.size} news items")
        return response.results
    }

    suspend fun getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
        val allNews = mutableSetOf<News>()
        val channel = Channel<List<News>>(Channel.UNLIMITED)

        try {
            for (page in 1..Int.MAX_VALUE / 2) {
                logger.info("Fetching page $page")
                val response: List<News> = getNews(PAGE_SIZE, page)

                if (response.isEmpty()) break

                channel.send(response)
                logger.debug("Fetched ${response.size} news items")
            }
        } finally {
            channel.close()
        }

        channel.consumeEach { newsList ->
            allNews.addAll(newsList)
        }

        val filteredNews = allNews.filter { news ->
            val publicationDate =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(news.publicationDate * 1000), ZoneId.systemDefault())
                    .toLocalDate()
            publicationDate in period
        }

        val sortedNews = filteredNews.sortedByDescending { it.rating }.take(count)
        logger.info("Returning ${sortedNews.size} most rated news items")
        return sortedNews
    }
}