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
import com.google.android.material.switchmaterial.SwitchMaterial
import com.zybooks.inspobook.R

class ColorWheelDialogFragment : DialogFragment() {

    lateinit var colorWheelImageView: ImageView
    lateinit var toggleShakeSensor: SwitchMaterial
    private var selectedColorAsInt: Int = Color.RED

    lateinit var selectedColorWheelPreview: TextView
    lateinit var acceptNewColorButton: Button
    lateinit var cancelButton: Button
    private var targetFragment: Fragment? = null

    private var oldColor: Int = Color.RED
    private var oldShakeToggle: Boolean = false

    lateinit var saturationSeekBar: SeekBar
    lateinit var brightnessSeekBar: SeekBar

    private var pastHue: Float = Color.GREEN.toFloat()
    private var pastSaturation: Float = 1f
    private var pastBrightness: Float = 0.5f

    private var allowShake: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //don't allow dismissing by clicking outside of dialog
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_color_wheel_dialog, container, false)

        toggleShakeSensor = view.findViewById<SwitchMaterial>(R.id.toggleShakeListenerSwitch)
        colorWheelImageView = view.findViewById<ImageView>(R.id.colorWheelImage)
        selectedColorWheelPreview = view.findViewById<TextView>(R.id.colorWheelSelectPreview)
        acceptNewColorButton = view.findViewById<Button>(R.id.setNewColorButton)
        cancelButton = view.findViewById<Button>(R.id.cancelButton)

        //get selected color passed in by another fragment, or set to default of color red, do same for toggle and default is false
        selectedColorAsInt = arguments?.getInt("currentColor", Color.RED) ?: Color.RED
        allowShake = arguments?.getBoolean("currentToggle", false) ?: false
        oldColor = selectedColorAsInt
        oldShakeToggle = allowShake
        toggleShakeSensor.isChecked = allowShake

        //get HSL array from the color int passed in from another fragment and assign them to variables
        val colorHSL = getHSLFromIntColor(selectedColorAsInt)
        pastHue = colorHSL[0]
        pastSaturation = colorHSL[1]
        pastBrightness = colorHSL[2]
        selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)

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
                        val newColor = bitMapOfColorWheel.getColor(x, y).toArgb()

                        //get hsl of the newly selected color int
                        val hsl = getHSLFromIntColor(newColor)
                        pastHue = hsl[0]

                        //get the color square to correct color
                        val adjustedColor = ColorUtils.HSLToColor(floatArrayOf(pastHue, pastSaturation, pastBrightness))
                        selectedColorAsInt = adjustedColor
                        selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)

                        imgView.performClick()
                        Log.d("ColorWheel", "Color selected: ${selectedColorAsInt} and pastHue ${pastHue}")
                    }
                    true
                }
                return true
            }
        })

        //if toggle button is checked, set allowShake to true, else set to false
        toggleShakeSensor.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                allowShake = true
                Log.d("Shake", "Toggle shake on")
            }
            else{
                allowShake = false
                Log.d("Shake", "Toggle shake off")
            }
        }

        //get saturation bar, allow user to adjust satuation of color
        saturationSeekBar = view.findViewById<SeekBar>(R.id.saturationBar)
        saturationSeekBar.progress = (pastSaturation*100).toInt()
        saturationSeekBar.max = 100
        saturationSeekBar.min = 1
        saturationSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //adjust saturation of color
                val newSaturation = progress.toFloat()/100f

                //only adjust past saturation when saturation bar is changed
                pastSaturation = newSaturation

                //convert hsl to color int and set preview tint
                Log.d("ColorWheel", "saturation is ${pastSaturation} and hue is ${pastHue}")
                val adjustedColor = ColorUtils.HSLToColor(floatArrayOf(pastHue, newSaturation, pastBrightness))
                selectedColorAsInt = adjustedColor
                selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        //get brightness bar, allow user to adjust brightness of color
        brightnessSeekBar = view.findViewById<SeekBar>(R.id.brightnessBar)
        brightnessSeekBar.progress = (pastBrightness*100).toInt()
        brightnessSeekBar.max = 100
        brightnessSeekBar.min = 1
        brightnessSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //adjust brightness of color
                val newBrightness = progress.toFloat()/100f

                //adjust past brightness
                pastBrightness = newBrightness

                //convert hsl to color int and set preview tint
                Log.d("ColorWheel", "brightness is ${pastBrightness} and hue is ${pastHue}")
                val adjustedColor = ColorUtils.HSLToColor(floatArrayOf(pastHue, pastSaturation, newBrightness))
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
            resultBundle.putBoolean("toggleShake", allowShake)
            parentFragmentManager.setFragmentResult("colorWheelResult", resultBundle)

            //dismiss this dialogfragment
            dismiss()
        }

        cancelButton.setOnClickListener {
            //send back bundle with the initial color passed into this dialogfragment
            val resultBundle = Bundle()
            resultBundle.putInt("newColor", oldColor)
            resultBundle.putBoolean("toggleShake", oldShakeToggle)
            parentFragmentManager.setFragmentResult("colorWheelResult", resultBundle)


            //dismiss this dialogfragment
            dismiss()
        }

        // Inflate the layout for this fragment
        return view
    }

    //return hsl, a float array of size 3 that continues hue, saturation, and brightness in that order
    fun getHSLFromIntColor(intColor: Int): FloatArray{
        //convert int color to hsl and return hue
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(intColor, hsl)
        return hsl
    }

    companion object{
        //pass in a color and shake toggle for dialogfragment to use when called from another fragment
        fun newInstance(currentColor: Int, currentShakeToggle: Boolean): ColorWheelDialogFragment{
            val colorWheelFragment = ColorWheelDialogFragment()
            val args = Bundle()
            args.putInt("currentColor", currentColor)
            args.putBoolean("currentToggle", currentShakeToggle)
            colorWheelFragment.arguments = args
            return colorWheelFragment
        }
    }
}