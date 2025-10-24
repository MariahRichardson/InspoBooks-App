package com.zybooks.inspobook.model

import kotlin.collections.mutableListOf

class InspoPagesManager(bookPages: List<InspoPage>) {

    var mutablePages: MutableList<InspoPage>

    init {
        mutablePages = bookPages.toMutableList()
    }

    fun addPage(){}

    fun removePage(){}
}