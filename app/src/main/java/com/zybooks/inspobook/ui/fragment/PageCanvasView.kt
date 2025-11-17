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
import android.graphics.Matrix
import android.graphics.PointF


class PageCanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    //default paint brush
    private val paint = Paint().apply{
        color = Color.RED
        strokeWidth = 5f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val path = Path()
    private var paths = mutableListOf<Pair<Path,Paint>>()
    private var bitmap: Bitmap? = null

    private var editingImage: Bitmap? = null
    private val editingMatrix = Matrix()
    private var isPlacingImage: Boolean = false

    // touch handling for image modification
    private var touchMode = MODE_NONE
    private var lastX = 0f
    private var lastY = 0f
    private var prevDistance = 0f
    private var prevAngle = 0f
    private val midPoint = PointF()

    companion object {
        private const val MODE_NONE = 0
        private const val MODE_DRAG = 1
        private const val MODE_ZOOM_ROTATE = 2
    }


    private var isBitmapDrawn: Boolean = false
    private var isEraseMode = false
    private var backgroundColor = Color.WHITE
    private var paintBrushColor = Color.RED
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
        paintBrushSize = 1f
        eraseBrushSize = 1f
        paint.strokeWidth = paintBrushSize
    }

    fun setPaintBrushColor(color: Int){
        paint.color = color
        paintBrushColor = color
    }

    fun getPaintBrushColor(): Int{
        return paint.color
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
            paint.color = paintBrushColor
            paint.strokeWidth = paintBrushSize
        }
        isEraseMode = eraseMode
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isPlacingImage && editingImage != null) {
            handleImageTouch(event)
            return true
        }

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

        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        }

        if (isBitmapDrawn) {
            for (paintPair in paths) {
                canvas.drawPath(paintPair.first, paintPair.second)
            }
            canvas.drawPath(path, paint)
        } else {
            if (bitmap != null) {
                canvas.drawBitmap(bitmap!!, 0f, 0f, null)
                isBitmapDrawn = true
            }
        }

        if (isPlacingImage && editingImage != null) {
            canvas.drawBitmap(editingImage!!, editingMatrix, null)
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

    fun clearPaths(shouldInvalidate: Boolean = true) {
        paths.clear()
        path.reset()
        if (shouldInvalidate) {
            invalidate()
        }
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

        // ensure same size bitmap as canvas
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

        // scale to fit inside of canvas
        val scale = minOf(
            canvasWidth.toFloat() / image.width.toFloat(),
            canvasHeight.toFloat() / image.height.toFloat(),
            1f
        )

        val destWidth = (image.width * scale).toInt().coerceAtLeast(1)
        val destHeight = (image.height * scale).toInt().coerceAtLeast(1)

        // center image
        val left = ((canvasWidth - destWidth) / 2f).coerceAtLeast(0f)
        val top = ((canvasHeight - destHeight) / 2f).coerceAtLeast(0f)
        val destRect = RectF(left, top, left + destWidth, top + destHeight)

        // draw bitmap
        canvas.drawBitmap(image, null, destRect, null)

        isBitmapDrawn = true
        invalidate()
    }

    // called after user picks an image
    fun startImagePlacement(image: Bitmap) {
        if (canvasWidth <= 0 || canvasHeight <= 0) return

        editingImage = image
        editingMatrix.reset()

        // scale image
        val scale = minOf(
            canvasWidth.toFloat() / image.width.toFloat(),
            canvasHeight.toFloat() / image.height.toFloat()
        ) * 0.6f

        editingMatrix.postScale(scale, scale)

        val scaledW = image.width * scale
        val scaledH = image.height * scale

        // Center it on the canvas
        val dx = (canvasWidth - scaledW) / 2f
        val dy = (canvasHeight - scaledH) / 2f
        editingMatrix.postTranslate(dx, dy)

        isPlacingImage = true
        touchMode = MODE_NONE
        invalidate()
    }

    // commit the image into the bitmap
    fun commitImagePlacement() {
        if (!isPlacingImage || editingImage == null) return

        // flatten current view
        val flattened = getBitMap()

        bitmap = flattened
        isBitmapDrawn = true

        clearPaths(shouldInvalidate = false)
        editingImage = null
        isPlacingImage = false
        touchMode = MODE_NONE

        invalidate()
    }

    // helper--if image placement mode
    fun hasPendingImageEdit(): Boolean {
        return isPlacingImage && editingImage != null
    }

    private fun handleImageTouch(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchMode = MODE_DRAG
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) {
                    touchMode = MODE_ZOOM_ROTATE
                    prevDistance = spacing(event)
                    if (prevDistance > 10f) {
                        midPoint(event, midPoint)
                        prevAngle = rotation(event)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (touchMode) {
                    MODE_DRAG -> {
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        editingMatrix.postTranslate(dx, dy)
                        lastX = event.x
                        lastY = event.y
                        invalidate()
                    }
                    MODE_ZOOM_ROTATE -> {
                        if (event.pointerCount >= 2) {
                            val newDist = spacing(event)
                            if (newDist > 10f) {
                                val scale = newDist / prevDistance
                                midPoint(event, midPoint)
                                editingMatrix.postScale(scale, scale, midPoint.x, midPoint.y)

                                val newAngle = rotation(event)
                                val deltaAngle = newAngle - prevAngle
                                editingMatrix.postRotate(deltaAngle, midPoint.x, midPoint.y)

                                prevDistance = newDist
                                prevAngle = newAngle
                                invalidate()
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {
                touchMode = MODE_NONE
            }
        }
    }

    private fun spacing(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt(x * x + y * y)
    }

    private fun midPoint(event: MotionEvent, point: PointF) {
        if (event.pointerCount < 2) return
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2f, y / 2f)
    }

    private fun rotation(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val dx = event.getX(1) - event.getX(0)
        val dy = event.getY(1) - event.getY(0)
        return (Math.toDegrees(kotlin.math.atan2(dy, dx).toDouble())).toFloat()
    }

}