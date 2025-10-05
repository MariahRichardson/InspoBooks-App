package com.zybooks.inspobook.model

class InspoBook(var bookName: String)
{

//    var id: String = bookIdentifier
    var name: String? = bookName
    var coverPage: Int = 1

    //init runs right after primary constructor
    init {
        //if inspobook name given is null or blank, set to a default str
        if (name.isNullOrBlank()) {
            name = "untitled book"
        }
    }
}