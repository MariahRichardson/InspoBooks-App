package com.zybooks.inspobook.ui.fragment

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zybooks.inspobook.R
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.model.InspoPage
import com.zybooks.inspobook.viewmodel.InspoPagesViewModel
import kotlin.getValue
import kotlin.math.sqrt
import kotlin.random.Random

class InspoPageFragment : Fragment() {

    private val inspoPagesViewModel: InspoPagesViewModel by activityViewModels()
    private lateinit var toolbar: MaterialToolbar
    private lateinit var inspoBookSelected: InspoBook
    private lateinit var drawView: PageCanvasView

    private lateinit var brushSizeBar: SeekBar
    private lateinit var bottomInspoPageNavView : BottomNavigationView

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var sensorEventListener: SensorEventListener

    //save previous x, y, z accelerometer values
    private var prev_x: Float = 0f
    private var prev_y: Float = 0f
    private var prev_z: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get the sensor event listener, sensor manager, and the accelerometer sensor
        sensorEventListener = getSensorEventListener()
        sensorManager = getContext()?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inspo_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get the inspobook selected from the InspoBooksFragment
        var tempInspoBookSelected = arguments?.let{
            InspoPageFragmentArgs.fromBundle(it).inspoBook
        }

        //get canvas, brush size seek bar, bottom nav view from view, and top toolbar
        drawView = requireActivity().findViewById<PageCanvasView>(R.id.canvasView)
        bottomInspoPageNavView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomInspoPageNavigationView)
        toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.inspoPageToolbar)

        //set default tint to red as that is the default brush color
        toolbar.menu.findItem(R.id.paintBrushColor).icon?.setTint(Color.RED)

        //get int from dialogfragment, or set to default color red
        parentFragmentManager.setFragmentResultListener("colorWheelResult", viewLifecycleOwner){p0, result ->
            val newColor = result.getInt("newColor", Color.RED)
            drawView.setPaintBrushColor(newColor)

            //set new color item
            toolbar.menu.findItem(R.id.paintBrushColor).icon?.setTint(newColor)
        }
        Log.d("ColorWheel", "OnCreate In InspoPage, get color ${drawView.getPaintBrushColor()}")

        //only init all other items once onLayout of the canvas has been called
        drawView.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                //if inspobook clicked in inspobooks fragment is not null, continue and set up viewmodel with the selected book
                if(tempInspoBookSelected != null){
                    //reset canvas view for each book loaded
                    resetPageCanvasView(drawView)
                    inspoBookSelected = tempInspoBookSelected
                    inspoPagesViewModel.setupWithBook(inspoBookSelected, drawView.getBitMap())
                    Log.d("CHECCC","${drawView.canvasWidth} and ${drawView.canvasHeight}")
                }
                else{
                    Toast.makeText(requireContext(), "Select InspoBook is null", Toast.LENGTH_LONG).show()
                }

                //get first page content and display on Canvas
                var tempContent = inspoPagesViewModel.getCurrentPageContent()
                drawView.initializeCanvasPage(tempContent)

                //brush size can be between 1 to 100
                brushSizeBar = requireActivity().findViewById<SeekBar>(R.id.brushSizeBar)
                brushSizeBar.max = 100
                brushSizeBar.min = 1
                brushSizeBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        //adjust stroke width to a new value when bar is changed
                        drawView.setStrokeWidth(progress.toFloat())
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {}
                    override fun onStopTrackingTouch(p0: SeekBar?) {}
                })

                //toolbar to navigate back to the list of inspobooks
                //toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.inspoPageToolbar)
                toolbar.setOnMenuItemClickListener { menuItem ->
                    when(menuItem.itemId){
                        R.id.backToBooks -> {
                            findNavController().navigate(R.id.action_InspoPageFragment_to_InspoBooksFragment)
                            true
                        }
                        R.id.paintBrush -> {
                            drawView.setEraseMode(false)
                            brushSizeBar.setProgress(drawView.paintBrushSize.toInt())
                            true
                        }
                        R.id.eraseBrush -> {
                            drawView.setEraseMode(true)
                            brushSizeBar.setProgress(drawView.eraseBrushSize.toInt())
                            true
                        }
                        R.id.paintBrushColor -> {
                            val colorWheelDialogFragment = ColorWheelDialogFragment.newInstance(drawView.getPaintBrushColor())
                            colorWheelDialogFragment.show(parentFragmentManager, "colorWheelDialog")
                            true
                        }
                        else -> false
                    }
                }
                //set toolbar title to be the selected inspobook's name
                toolbar.setTitle("page ${inspoPagesViewModel.currentPageNum+1}: ${inspoBookSelected.name}")

                //set actions based on click of the inspopage's bottom navigation view selection
                bottomInspoPageNavView.setOnItemSelectedListener { item ->
                    when(item.itemId){
                        R.id.previousPage -> {
                            if(inspoPagesViewModel.doesPreviousPageExist()){
                                //set page to previous page
                                inspoPagesViewModel.toPrevPage()

                                //clean the canvas and draw the previous page's content
                                resetPageCanvasView(drawView)
                                drawView.initializeCanvasPage(inspoPagesViewModel.getCurrentPageContent())
                                toolbar.setTitle("page ${inspoPagesViewModel.currentPageNum+1}: ${inspoBookSelected.name}")
                            }
                            else{
                                Toast.makeText(requireContext(), "There is no previous page!", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }
                        R.id.savePageContent -> {
                            //save content of Canvas to the content variable of the InspoPage
                            inspoPagesViewModel.updatePage(drawView.getBitMap())
                            Toast.makeText(requireContext(), "Page saved!", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.addPage -> {
                            //save the current page content when add page is clicked
                            inspoPagesViewModel.updatePage(drawView.getBitMap())
                            Toast.makeText(requireContext(), "Page saved!", Toast.LENGTH_SHORT).show()

                            resetPageCanvasView(drawView)
                            //get blank canvas and make new page
                            inspoPagesViewModel.addPage(drawView.getBitMap())
                            drawView.initializeCanvasPage(inspoPagesViewModel.getCurrentPageContent())
                            Toast.makeText(requireContext(), "Page added!", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.deleteCurrentPage -> {
                            var isRemoveSuccess = inspoPagesViewModel.removePage()
                            drawView.initializeCanvasPage(inspoPagesViewModel.getCurrentPageContent())
                            if(!isRemoveSuccess){
                                //if unable to remove current page, clear it
                                resetPageCanvasView(drawView)
                                Toast.makeText(requireContext(), "Page cleared!", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                Toast.makeText(requireContext(), "Page removed!", Toast.LENGTH_SHORT).show()
                            }
                            toolbar.setTitle("page ${inspoPagesViewModel.currentPageNum+1}: ${inspoBookSelected.name}")

                            true
                        }
                        R.id.nextPage -> {
                            if(inspoPagesViewModel.doesNextPageExist()){
                                //set page to next page
                                inspoPagesViewModel.toNextPage()

                                //clean the canvas and draw the next page's content
                                resetPageCanvasView(drawView)
                                drawView.initializeCanvasPage(inspoPagesViewModel.getCurrentPageContent())
                                toolbar.setTitle("page ${inspoPagesViewModel.currentPageNum+1}: ${inspoBookSelected.name}")
                            }
                            else{
                                Toast.makeText(requireContext(), "There is no next page!", Toast.LENGTH_SHORT).show()
                            }
                            true
                        }
                        else -> false
                    }
                }

                inspoPagesViewModel.pageList.observe(viewLifecycleOwner){inspopages ->
                    //if the currentPage is less than the size observed, get the item
                    //get distinct items from inspopages based on id
                    //val distinctPages: List<InspoPage> = inspopages.toMutableList().distinctBy{it.pageID}
                    inspopages.sortBy{it.pageID.removePrefix("_").toLong()}

                    if(inspoPagesViewModel.currentPageNum < inspopages.size && inspopages.isNotEmpty()){
                        //inspoPagesViewModel.setLoadedPage(distinctPages[inspoPagesViewModel.currentPageNum])
                        inspoPagesViewModel.setLoadedPage(inspopages[inspoPagesViewModel.currentPageNum])
                        resetPageCanvasView(drawView)
                        drawView.initializeCanvasPage(inspoPagesViewModel.getCurrentPageContent())
                        //Log.d("InspoPageFragment", "observer of pages triggered and successfully loaded size ${distinctPages.size} and currentPageNum ${inspoPagesViewModel.currentPageNum}")
                        Log.d("InspoPageFrag_RepoTest", "observer pageNum: ${inspoPagesViewModel.currentPageNum} and page size ${inspoPagesViewModel.pageList.value.size}:")

                        var temp: Int = 0
                        for(i in inspoPagesViewModel.pageList.value){
                            Log.d("InspoPageFrag_RepoTest", "page #${temp} id: ${inspoPagesViewModel.pageList.value[temp].pageID} and content ${inspoPagesViewModel.pageList.value[temp].content}")
                            temp++
                        }
                    }
                    else{
                        //Log.d("InspoPageFragment", "observer of pages triggered but invalid load: size ${distinctPages.size} and currentPageNum ${inspoPagesViewModel.currentPageNum}")
                        Log.d("InspoPageFragment", "observer of pages triggered but invalid load: size ${inspopages.size} and currentPageNum ${inspoPagesViewModel.currentPageNum}")
                    }
                }
                //remove predraw listener after it runs
                drawView.viewTreeObserver.removeOnPreDrawListener(this)

                return true
            }
        })
    }

    fun resetPageCanvasView(v: PageCanvasView){
        v.initializeCanvasPage(null)
        v.clearPaths()
    }

    private fun setNewColor(newColor: Int, newSaturation: Int, newBrightness: Int){
        val hsl = FloatArray(3)
        //convert the pixel color int to hsl, so hsl contains hue, saturation, and lightness
        ColorUtils.colorToHSL(newColor, hsl)
        //get the hue of the pixel color(newColor passed in)
        val hue = hsl[0]

        val adjustedColor = ColorUtils.HSLToColor(floatArrayOf(hue, newSaturation/100f, newBrightness/100f))

        Log.d("InspoPageFrag", "Shake setNewColor: ${newColor} -> ${hue} -> ${adjustedColor}")
        //only run if pagecanvasview and toolbar are not null
        if(drawView != null && toolbar != null) {
            Log.d("InspoPageFrag", "Not null Shake setNewColor: ${newColor} -> ${adjustedColor}")
            //set new color paint brush
            drawView.setPaintBrushColor(adjustedColor)

            //set new color item
            toolbar.menu.findItem(R.id.paintBrushColor).icon?.setTint(adjustedColor)
        }
    }

    //count number of times the shake goes over the threshold before triggering action
    var count: Int = 0
    var shakeTimestamp: Long = 0
    val timeBetweenShakes: Long = 500

    private fun getSensorEventListener(): SensorEventListener{
        return object: SensorEventListener{
            override fun onSensorChanged(sensorEvent: SensorEvent?) {
                //if sensor event is not null and type is of accelerometer(linear acceleration ignores gravity)
                if(sensorEvent != null && sensorEvent.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
                    //get acceleration force of x, y, and z-axis detected
                    val x = sensorEvent.values[0]
                    val y = sensorEvent.values[1]
                    val z = sensorEvent.values[2]

                    //get change from previous acceleration to current
//                    val _x = Math.abs(x - prev_x)
//                    val _y = Math.abs(y - prev_y)
//                    val _z = Math.abs(z - prev_z)

                    var gForce: Float = sqrt(x*x + y*y + z*z)

                    //set how hard the user should shake their phone
                    val threshold = 3
                    if(gForce > threshold){
                        val currentTime = System.currentTimeMillis()

                        //detect shakes that are .5 seconds part
                        if(shakeTimestamp + timeBetweenShakes <= currentTime){
                            count++
                            if(count > 1){
                                count = 0
                                Log.d("InspoPageFrag", "Strong shake detected x,y,z: ${x}, ${y}, ${z}, and gforce: ${gForce}")

                                val vectorDrawable = context?.getDrawable(R.drawable.color_wheel_gradient_square) as VectorDrawable
                                vectorDrawable?.let{
                                    //get width and height of the colorwheel vector image from the drawable folder
                                    val width = it.intrinsicWidth
                                    val height = it.intrinsicHeight
                                    val scaleBy = 0.5f

                                    //calculate scaled down width and height, and create bitmap with 565 to reduce memory usage
                                    val scaledWidth = (width*scaleBy).toInt()
                                    val scaledHeight = (height*scaleBy).toInt()
                                    val colorBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)
                                    val canvas = Canvas(colorBitmap)

                                    //set new scaled width and height
                                    it.setBounds(0,0,scaledWidth,scaledHeight)
                                    it.draw(canvas)

                                    //choose a random x and y coordinate in the bitmap of colorwheel img, and extract the color pixel
                                    val randomX = Random.nextInt(colorBitmap.width)
                                    val randomY = Random.nextInt(colorBitmap.height)
                                    val pixelColor = colorBitmap.getColor(randomX, randomY).toArgb()
                                    Log.d("InspoPageFrag", "Shake selected color: ${Integer.toHexString(pixelColor)} and ${pixelColor}")

                                    //get random saturation and brightness of color 0-100
                                    val randomSaturation = Random.nextInt(101)
                                    val randomBrightness = Random.nextInt(101)
                                    setNewColor(pixelColor, randomSaturation, randomBrightness)
                                }
                            }
                        }
                        shakeTimestamp = currentTime
                    }

                    //assign current to previous x,y,z detected acceleration of device
                    prev_x = x
                    prev_y = y
                    prev_z = z
                }
            }


            override fun onAccuracyChanged(sensor: Sensor?, acc: Int) {
                //to handle accuracy changes
            }
        }
    }

    override fun onResume() {
        super.onResume()

        //apply sensorEventListener to the accelerometer
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorEventListener)
    }
}