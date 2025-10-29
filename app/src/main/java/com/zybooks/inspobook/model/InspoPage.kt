package com.zybooks.inspobook.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.icu.number.IntegerWidth
import android.os.Parcel
import android.os.Parcelable
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import java.io.File

//FOR REPOSITORY/CRUD: pageID(str) and content(bitmap)
//Parcelable allows object to be passed between components(ex.fragments), faster than Serializable
class InspoPage(var pageID: String, var content: Bitmap?): Parcelable //inspobookID: String, canvasWidth: Int?, canvasHeight: Int?)
{
    constructor(parcel: Parcel): this(
        //get id and bitmap from parcel to create an InspoPage
        parcel.readString() ?: "no id",
        parcel.createByteArray()?.let{byteArray ->
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        //convert string id and write to parcel
        parcel.writeString(pageID)

        //convert bitmap to bytearray and write to parcel
        content?.let{
            val byteArrayOutput = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutput)
            val byteArray = byteArrayOutput.toByteArray()
            parcel.writeByteArray(byteArray)
        }
    }

    companion object{
        @JvmField
        val CREATOR: Parcelable.Creator<InspoPage> = object: Parcelable.Creator<InspoPage>{
            override fun createFromParcel(parcel: Parcel): InspoPage? {
                return InspoPage(parcel)
            }

            override fun newArray(size: Int): Array<InspoPage?> {
                return arrayOfNulls(size)
            }
        }
    }
}