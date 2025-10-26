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
import com.zybooks.inspobook.R
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.viewmodel.InspoPagesViewModel
import kotlin.getValue

class InspoPageFragment : Fragment() {

    private val inspoPagesViewModel: InspoPagesViewModel by activityViewModels()
    private lateinit var toolbar: MaterialToolbar
    private lateinit var inspoBookSelected: InspoBook
    private lateinit var drawView: PageCanvasView
    private lateinit var saveButton: Button

    private lateinit var brushSizeBar: SeekBar


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


        //get canvas, save button, brush size seek bar from view
        saveButton = requireActivity().findViewById<Button>(R.id.saveDrawingButton)
        drawView = requireActivity().findViewById<PageCanvasView>(R.id.canvasView)


        //if inspobook clicked in inspobooks fragment is not null, continue and set up viewmodel with the selected book
        if(tempInspoBookSelected != null){
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

        //save canvas of page
        saveButton.setOnClickListener {
            inspoPagesViewModel.updatePage(inspoPagesViewModel.getCurrentPageContent())
            Toast.makeText(requireContext(), "Page Saved!", Toast.LENGTH_LONG).show()
        }

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
        toolbar.setTitle("${inspoBookSelected.name}")

    }

}