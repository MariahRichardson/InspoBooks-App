package com.zybooks.inspobook.model

import android.os.Parcel
import android.os.Parcelable
import java.util.concurrent.atomic.AtomicInteger

//FOR REPOSITORY/CRUD: name(str) and id(str) and can most likely do coverPage and listOfPages in viewmodel
//Parcelable allows object to be passed between components(ex.fragments), faster than Serializable
class InspoBook(bookName: String): Parcelable
{
    constructor(): this("")
    constructor(parcel: Parcel): this(
        parcel.readString() ?: "no id"
    )

    var name: String?
    var coverPage: Int = 1
    //increment nextID and assign as id
    val id = nextID.incrementAndGet()
    var listOfPages: List<InspoPage> = listOf()


    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(coverPage)
        parcel.writeList(listOfPages)
    }

    //companion object makes so it belongs to the class and not an instance of it
    companion object{
        //increment id for each new inspobook
        val nextID: AtomicInteger = AtomicInteger(0)


        //create instances of type InspoBook from parcels
        @JvmField
        val CREATOR: Parcelable.Creator<InspoBook> = object: Parcelable.Creator<InspoBook>{

            //deserializes the InspoBook
            override fun createFromParcel(parcel: Parcel): InspoBook? {
                return InspoBook(parcel)
            }

            override fun newArray(size: Int): Array<InspoBook?> {
                return arrayOfNulls(size)
            }
        }
    }


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