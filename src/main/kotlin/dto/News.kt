package org.tinkoff.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.exp

@Serializable
data class News(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("publication_date") val publicationDate: Long,
    @SerialName("slug") val slug: String? = "No slug",
    @SerialName("place") val place: String? = "No place",
    @SerialName("description") val description: String? = "No description",
    @SerialName("siteUrl") val siteUrl: String? = "No siteUrl",
    @SerialName("favorites_count") val favoritesCount: Int? = null,
    @SerialName("comments_count") val commentsCount: Int? = null,
) {
    val rating: Double by lazy {
        calculateRating(favoritesCount ?: 0, commentsCount ?: 0)
    }

    private fun calculateRating(favoritesCount: Int, commentsCount: Int): Double {
        return 1 / (1 + exp(-(favoritesCount.toDouble() / (commentsCount + 1))))
    }
}