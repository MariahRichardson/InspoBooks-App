package com.zybooks.inspobook.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        //if inspobook clicked in inspobooks fragment is not null, continue and set up viewmodel with the selected book
        if(tempInspoBookSelected != null){
            inspoBookSelected = tempInspoBookSelected
            inspoPagesViewModel.setupWithBook(inspoBookSelected)

        }
        else{
            Toast.makeText(requireContext(), "Select InspoBook is null", Toast.LENGTH_LONG).show()
        }

        //toolbar to navigate back to the list of inspo books
        toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.inspoPageToolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.backToBooks -> {
                    findNavController().navigate(R.id.action_InspoPageFragment_to_InspoBooksFragment)
                    true
                }
                else -> false
            }
        }

        toolbar.setTitle("${inspoBookSelected.name}")
    }

}