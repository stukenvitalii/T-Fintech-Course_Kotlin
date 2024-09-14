package org.tinkoff

import org.tinkoff.client.KudaGoClient
import org.tinkoff.client.getMostRatedNews
import org.tinkoff.client.saveNews
import java.time.LocalDate

suspend fun main() {
    val client = KudaGoClient()
    val newsList = client.getNews()

    newsList.forEach(::println)

    val period = LocalDate.of(2024, 1, 1)..LocalDate.of(2024, 12, 31)
    val mostRatedNews = newsList.getMostRatedNews(20, period)

    println("Total news: ${newsList.size}")
    println("Most rated news: ${mostRatedNews.size}")

    mostRatedNews.forEach {
        println(it)
    }

    saveNews("src/main/resources/news.csv", newsList)
}