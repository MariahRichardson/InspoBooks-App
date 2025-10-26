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
    private var paths = mutableListOf<Pair<Path,Paint>>()
    private var isEraseMode = false
    private var backgroundColor = Color.WHITE
    var paintBrushSize: Float
    var eraseBrushSize: Float

    init{
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        setBackgroundColor(Color.WHITE)

        //set default brush sizes
        paintBrushSize = 5f
        eraseBrushSize = 10f
        paint.strokeWidth = paintBrushSize
    }

    fun setEraseMode(eraseMode: Boolean){
        //if eraseMode is true, then change brush to be background color, allowing for erase
        if(eraseMode){
            paint.color = backgroundColor
            paint.strokeWidth = eraseBrushSize
            //paint.color = Color.TRANSPARENT
            //TODO: add different stroke width for paint brush and erase brush
        }
        else{
            paint.color = Color.BLACK
            paint.strokeWidth = paintBrushSize
        }
        isEraseMode = eraseMode
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
                paths.add(Pair(Path(path), Paint(paint)))
                path.reset()
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //paths contains past pairs of the path and paint on canvas
        for(paintPair in paths){
            canvas.drawPath(paintPair.first, paintPair.second)
        }
        //update to draw the new path
        canvas.drawPath(path, paint)
    }

    fun setStrokeWidth(width: Float){
        //update brush sizes depending on mode selected
        if(isEraseMode){
            eraseBrushSize = width
        }
        else{
            paintBrushSize = width
        }

        paint.strokeWidth = width
    }

    //save canvas as a bitmap
    fun getBitMap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

}