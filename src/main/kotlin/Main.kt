package org.tinkoff

import org.tinkoff.client.KudaGoClient
import org.tinkoff.client.getMostRatedNews
import org.tinkoff.client.saveNews
import org.tinkoff.dsl.readme
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

suspend fun main() {
    val client = KudaGoClient()
    val newsList = client.getNews()

    val period = LocalDate.of(2023, 1, 1)..LocalDate.of(2024, 12, 31)
    val mostRatedNews = getMostRatedNews(20, period)

    saveNews("src/main/resources/news.csv", newsList)

    val readmeContent = readme {
        header(level = 1) { +"Most rated news: " }

        for (news in mostRatedNews) {
            header(level = 2) { +news.title }
            header(level = 3) { +"Description: ".plus(news.description!!) }
            header(level = 4) {
                +"Publication date: ".plus(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(news.publicationDate * 1000), ZoneId.systemDefault())
                        .toLocalDate().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    )
                )
            }
            header(level = 4) { +"Favorites count: ".plus(news.favoritesCount!!) }
            header(level = 4) { +"Comments count: ".plus(news.commentsCount!!) }
            dividingLine()
        }
    }

    File("README.md").writeText(readmeContent.toString())
}