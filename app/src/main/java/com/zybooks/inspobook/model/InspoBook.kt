package com.zybooks.inspobook.model

import java.util.concurrent.atomic.AtomicInteger
class InspoBook(var bookName: String)
{
    var name: String?
    var coverPage: Int = 1

    var listOfPages: List<InspoPage> = listOf()

    //companion object makes so it belongs to the class and not an instance of it
    companion object{
        val nextID: AtomicInteger = AtomicInteger(0)
    }
    //increment nextID and assign as id
    val id = nextID.incrementAndGet()

    //init runs right after primary constructor
    init {
        name = bookName
        //if inspobook name given is null or blank, set to a default str
        if (name.isNullOrBlank()) {
            //name = "untitled book"
            name = "Default $id"
        }

        //TODO: set next id if there are inspobooks that already exist
    }
}