package com.zybooks.inspobook.util

/**
 * Builds query parameters for Unsplash search requests
 *
 * Allows testing without calling Unsplash API
 */
object UnsplashQueryBuilder {

    fun buildSearchQuery(
        keyword: String,
        orderBy: String? = null,
        color: String? = null,
        page: Int = 1,
        perPage: Int = 30
    ): Map<String, String> {

        val params = mutableMapOf(
            "query" to keyword,
            "page" to page.toString(),
            "per_page" to perPage.toString()
        )

        if (!orderBy.isNullOrBlank()) params["order_by"] = orderBy
        if (!color.isNullOrBlank()) params["color"] = color

        return params
    }
}
