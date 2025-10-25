package com.zybooks.inspobook.ui.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class PageCanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //default paint brush
    private val paint = Paint().apply{
        color = Color.BLACK
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val path = Path()
    private var paths = mutableListOf<Path>()
    private var isEraseMode = false

    init{
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
    }

    fun setEraseMode(isEraseMode: Boolean){
        //if isEraseMode is true, then change brush to be color transparent, allowing for erase
        if(isEraseMode){
            paint.color = Color.TRANSPARENT
            //TODO: add different stroke width for paint brush and erase brush
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        //call invalidate to trigger onDraw, draw line based on user touch
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                paths.add(Path(path))
                path.reset()
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for(p in paths){
            canvas.drawPath(p, paint)
        }
        canvas.drawPath(path, paint)
    }

    //save canvas as a bitmap
    fun getBitMap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

}