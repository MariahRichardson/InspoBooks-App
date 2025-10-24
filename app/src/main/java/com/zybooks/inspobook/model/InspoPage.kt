package com.zybooks.inspobook.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.icu.number.IntegerWidth
import androidx.core.graphics.createBitmap

class InspoPage(inspobookID: String, canvasWidth: Int?, canvasHeight: Int?)
{
    var pageID: String? = "dummy"

    //use bitmap to eventually compress and store as a file
    var bitmapOfPage: Bitmap

    //use canvas to actually do changes on
    var canvasPage: Canvas

    init {
        //if user provided page width and height is not null and greater than or equal to 10, then initialize using given width and height
        //TODO:OR REMOVE AND MAKE IT A SET SIZE
        if(canvasWidth != null && canvasWidth >= 10 && canvasHeight != null && canvasHeight >= 10) {
            bitmapOfPage = createBitmap(canvasWidth, canvasHeight)
        }
        else{
            //else create a canvas of size 10 x 10
            bitmapOfPage = createBitmap(10,10)
        }

        //default background of created page to white
        bitmapOfPage.eraseColor(Color.WHITE)

        //make a Canvas with the bitmap created
        canvasPage = Canvas(bitmapOfPage)
    }
}