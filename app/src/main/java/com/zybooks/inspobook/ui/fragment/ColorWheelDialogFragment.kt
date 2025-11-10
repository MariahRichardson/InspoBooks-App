package com.zybooks.inspobook.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.zybooks.inspobook.R

class ColorWheelDialogFragment : DialogFragment() {

    lateinit var colorWheelImageView: ImageView
//    private lateinit var selectedColor: Color
    private var selectedColorAsInt: Int = Color.BLACK

    lateinit var selectedColorWheelPreview: TextView
    lateinit var acceptNewColorButton: Button
    lateinit var cancelButton: Button
    private var targetFragment: Fragment? = null

    private var oldColor: Int = Color.BLACK

    lateinit var saturationSeekBar: SeekBar
    lateinit var brightnessSeekBar: SeekBar

    private var pastHue: Float = Color.BLACK.toFloat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_color_wheel_dialog, container, false)

        colorWheelImageView = view.findViewById<ImageView>(R.id.colorWheelImage)
        selectedColorWheelPreview = view.findViewById<TextView>(R.id.colorWheelSelectPreview)
        acceptNewColorButton = view.findViewById<Button>(R.id.setNewColorButton)
        cancelButton = view.findViewById<Button>(R.id.cancelButton)

        //get selected color passed in by another fragment, or set to default of color black
        selectedColorAsInt = arguments?.getInt("currentColor", Color.BLACK) ?: Color.BLACK
        oldColor = selectedColorAsInt
        pastHue = getHueFromIntColor(selectedColorAsInt)
        Log.d("ColorWheel", "created diaglogFrag currentColor is: ${selectedColorAsInt}")
        //set color preview to color passed in by fragment or default
        selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)


        //execute ontouch checks after the imageView of the color wheel has actually loaded
        colorWheelImageView.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onPreDraw(): Boolean {
        //get the svg color wheel and convert to bitmap, use RGB_565 to lessen strain on memory(no alpha value)
                val bitMapOfColorWheel = colorWheelImageView.drawable.toBitmap(
                    colorWheelImageView.width,
                    colorWheelImageView.height,
                    Bitmap.Config.RGB_565
                )
                //detect touch within the color wheel img square to select new color
                colorWheelImageView.setOnTouchListener { imgView, event ->
                    val x = event.x.toInt()
                    val y = event.y.toInt()

                    //only set color if touch is within the bitmap of the image view
                    if (x in 0 until bitMapOfColorWheel.width && y in 0 until bitMapOfColorWheel.height) {

                        //get the Int of the color selected of the color wheel image view on touch, set color on the color preview
                        selectedColorAsInt = bitMapOfColorWheel.getColor(x, y).toArgb()
                        selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)
                        pastHue = getHueFromIntColor(selectedColorAsInt)
                        imgView.performClick()
                        Log.d("ColorWheel", "Color selected: ${selectedColorAsInt}")
                    }
                    true
                }
                return true
            }
        })

        saturationSeekBar = view.findViewById<SeekBar>(R.id.saturationBar)
        saturationSeekBar.max = 100
        saturationSeekBar.min = 1
        saturationSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //adjust saturation of color
                val newSaturation = progress.toFloat()

                val hsl = FloatArray(3)
                //convert the current color int to hsl, so hsl contains hue, saturation, and lightness
                ColorUtils.colorToHSL(selectedColorAsInt, hsl)
                //get the hue of the pixel color
                val hue = hsl[0]

                val adjustedColor = ColorUtils.HSLToColor(floatArrayOf(pastHue, newSaturation/100f, hsl[2]))
                selectedColorAsInt = adjustedColor
                selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        brightnessSeekBar = view.findViewById<SeekBar>(R.id.brightnessBar)
        brightnessSeekBar.max = 100
        brightnessSeekBar.min = 1
        brightnessSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //adjust brightness of color
                val newBrightness = progress.toFloat()/100f

                val hsl = FloatArray(3)
                //convert the current color int to hsl, so hsl contains hue, saturation, and lightness
                ColorUtils.colorToHSL(selectedColorAsInt, hsl)
                //get the hue of the pixel color
                val hue = hsl[0]

                //if brightness is 0 then saturation is 0, else keep saturation
                val saturationAdjustment = when{
                    newBrightness == 0f -> 0f
                    else -> hsl[1]
                }

                Log.d("ColorDialog", "hue is ${hue} and ${pastHue}")
                val adjustedColor = ColorUtils.HSLToColor(floatArrayOf(pastHue, saturationAdjustment, newBrightness))
                selectedColorAsInt = adjustedColor
                selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        acceptNewColorButton.setOnClickListener{
            //send back bundle with new color selected on touch
            val resultBundle = Bundle()
            resultBundle.putInt("newColor", selectedColorAsInt)
            parentFragmentManager.setFragmentResult("colorWheelResult", resultBundle)

            //dismiss this dialogfragment
            dismiss()
        }

        cancelButton.setOnClickListener {
            //send back bundle with the initial color passed into this dialogfragment
            val resultBundle = Bundle()
            resultBundle.putInt("newColor", oldColor)
            parentFragmentManager.setFragmentResult("colorWheelResult", resultBundle)


            //dismiss this dialogfragment
            dismiss()
        }

        // Inflate the layout for this fragment
        return view
    }

    fun getHueFromIntColor(intColor: Int): Float{
        //convert int color to hsl and return hue
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(intColor, hsl)
        return hsl[0]
    }

    companion object{
        //pass in a color for dialogfragment to use when called from another fragment
        fun newInstance(currentColor: Int): ColorWheelDialogFragment{
            val colorWheelFragment = ColorWheelDialogFragment()
            val args = Bundle()
            args.putInt("currentColor", currentColor)
            colorWheelFragment.arguments = args
            return colorWheelFragment
        }
    }
}