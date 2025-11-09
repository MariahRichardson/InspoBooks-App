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
import android.widget.TextView
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

                colorWheelImageView.setOnTouchListener { imgView, event ->
                    val x = event.x.toInt()
                    val y = event.y.toInt()

                    //only set color if touch is within the bitmap of the image view
                    if (x in 0 until bitMapOfColorWheel.width && y in 0 until bitMapOfColorWheel.height) {

                        //get the Int of the color selected of the color wheel image view on touch, set color on the color preview
                        selectedColorAsInt = bitMapOfColorWheel.getColor(x, y).toArgb()
                        selectedColorWheelPreview.setBackgroundColor(selectedColorAsInt)
                        imgView.performClick()
                        Log.d("ColorWheel", "Color selected: ${selectedColorAsInt}")
                    }
                    true
                }
                return true
            }
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