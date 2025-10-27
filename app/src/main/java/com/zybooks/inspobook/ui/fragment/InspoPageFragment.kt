package com.zybooks.inspobook.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import android.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zybooks.inspobook.R
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.viewmodel.InspoPagesViewModel
import kotlin.getValue

class InspoPageFragment : Fragment() {

    private val inspoPagesViewModel: InspoPagesViewModel by activityViewModels()
    private lateinit var toolbar: MaterialToolbar
    private lateinit var inspoBookSelected: InspoBook
    private lateinit var drawView: PageCanvasView

    private lateinit var brushSizeBar: SeekBar
    private lateinit var bottomInspoPageNavView : BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        var tempInspoBookSelected = arguments?.let{
            InspoPageFragmentArgs.fromBundle(it).inspoBook
        }


        //get canvas, brush size seek bar, bottom nav view from view
        drawView = requireActivity().findViewById<PageCanvasView>(R.id.canvasView)
        bottomInspoPageNavView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomInspoPageNavigationView)

        //if inspobook clicked in inspobooks fragment is not null, continue and set up viewmodel with the selected book
        if(tempInspoBookSelected != null){
            //reset canvas view for each book loaded
            resetPageCanvasView(drawView)
            inspoBookSelected = tempInspoBookSelected
            inspoPagesViewModel.setupWithBook(inspoBookSelected, drawView.getBitMap())
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
        toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.inspoPageToolbar)
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
                else -> false
            }
        }
        //set toolbar title to be the selected inspobook's name
        toolbar.setTitle("${inspoBookSelected.name}: page ${inspoPagesViewModel.currentPageNum+1}")


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
                        toolbar.setTitle("${inspoBookSelected.name}: page ${inspoPagesViewModel.currentPageNum+1}")
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
                    toolbar.setTitle("${inspoBookSelected.name}: page ${inspoPagesViewModel.currentPageNum+1}")

                    true
                }
                R.id.nextPage -> {
                    if(inspoPagesViewModel.doesNextPageExist()){
                        //set page to next page
                        inspoPagesViewModel.toNextPage()

                        //clean the canvas and draw the next page's content
                        resetPageCanvasView(drawView)
                        drawView.initializeCanvasPage(inspoPagesViewModel.getCurrentPageContent())
                        toolbar.setTitle("${inspoBookSelected.name}: page ${inspoPagesViewModel.currentPageNum+1}")
                    }
                    else{
                        Toast.makeText(requireContext(), "There is no next page!", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun resetPageCanvasView(v: PageCanvasView){
        v.initializeCanvasPage(null)
        v.clearPaths()
    }

}