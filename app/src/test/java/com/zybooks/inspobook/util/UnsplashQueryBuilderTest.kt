package com.zybooks.inspobook.util

import org.junit.Assert.*
import org.junit.Test

class UnsplashQueryBuilderTest {

    @Test
    fun `build basic query`() {
        val params = UnsplashQueryBuilder.buildSearchQuery("cats")

        assertEquals("cats", params["query"])
        assertEquals("1", params["page"])
        assertEquals("30", params["per_page"])
        assertFalse(params.containsKey("order_by"))
        assertFalse(params.containsKey("color"))
    }

    @Test
    fun `build query with sort and color`() {
        val params = UnsplashQueryBuilder.buildSearchQuery(
            keyword = "nature",
            orderBy = "latest",
            color = "green",
            page = 2,
            perPage = 50
        )

        assertEquals("nature", params["query"])
        assertEquals("latest", params["order_by"])
        assertEquals("green", params["color"])
        assertEquals("2", params["page"])
        assertEquals("50", params["per_page"])
    }

    @Test
    fun `build query ignores blank fields`() {
        val params = UnsplashQueryBuilder.buildSearchQuery(
            keyword = "sky",
            orderBy = "",
            color = " "
        )

        assertFalse(params.containsKey("order_by"))
        assertFalse(params.containsKey("color"))
    }
}
