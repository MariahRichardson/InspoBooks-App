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
import android.graphics.RectF

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
    private var bitmap: Bitmap? = null
    private var isBitmapDrawn: Boolean = false
    private var isEraseMode = false
    private var backgroundColor = Color.WHITE
    var paintBrushSize: Float
    var eraseBrushSize: Float
    var canvasWidth: Int = 1
    var canvasHeight: Int = 1

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

    //when layout pass it done, get the width and height of the canvasview created
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        canvasWidth = right - left
        canvasHeight = bottom - top
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //always draw bitmap that was saved(initial canvas)
        if(bitmap != null) {
            canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        }
        if(isBitmapDrawn) {
            //paths contains past pairs of the path and paint on canvas
            for (paintPair in paths) {
                canvas.drawPath(paintPair.first, paintPair.second)
            }
            //update to draw the new path
            canvas.drawPath(path, paint)
        }
        else{
            //if it is the first time the Canvas is being loaded(isBitmapDrawn = false)
            if(bitmap != null) {
                canvas.drawBitmap(bitmap!!, 0f, 0f, null)
                isBitmapDrawn = true
            }
        }
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

    fun clearPaths(){
        paths.clear()
        path.reset()
        invalidate()
    }

    //save canvas as a bitmap
    fun getBitMap(): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmap)

        //update tempCanvas with all paths, which will update the bitmap
        draw(tempCanvas)
        return bitmap
    }

    fun initializeCanvasPage(bmap: Bitmap?){
//        if(bitmap == null && bmap != null) {
//            bitmap = bmap
//        }
        bitmap = bmap
        invalidate()
    }

    fun addImageBitmap(image: Bitmap) {
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            return
        }

        // Ensure we have a mutable base bitmap the same size as the canvas
        if (bitmap == null || bitmap?.width != canvasWidth || bitmap?.height != canvasHeight || bitmap?.isMutable == false) {
            val newBase = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val baseCanvas = Canvas(newBase)

            baseCanvas.drawColor(backgroundColor)

            bitmap?.let { existing ->
                baseCanvas.drawBitmap(existing, 0f, 0f, null)
            }

            bitmap = newBase
        }

        val baseBitmap = bitmap!!
        val canvas = Canvas(baseBitmap)

        // Compute scale: fit inside canvas (don’t upscale beyond 1x)
        val scale = minOf(
            canvasWidth.toFloat() / image.width.toFloat(),
            canvasHeight.toFloat() / image.height.toFloat(),
            1f
        )

        val destWidth = (image.width * scale).toInt().coerceAtLeast(1)
        val destHeight = (image.height * scale).toInt().coerceAtLeast(1)

        // Center the image
        val left = ((canvasWidth - destWidth) / 2f).coerceAtLeast(0f)
        val top = ((canvasHeight - destHeight) / 2f).coerceAtLeast(0f)
        val destRect = RectF(left, top, left + destWidth, top + destHeight)

        // Draw bitmap into destination rectangle
        canvas.drawBitmap(image, null, destRect, null)

        isBitmapDrawn = true
        invalidate()
    }

}