package com.zybooks.inspobook.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zybooks.inspobook.R
import com.zybooks.inspobook.adapter.InspoBookAdapter
import com.zybooks.inspobook.viewmodel.InspoBooksViewModel

class InspoBooksFragment : Fragment() {

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }*/
    private val TAG : String = "InspoBooksFragment"
    private lateinit var recyclerView: RecyclerView
    var inspoBookAdapter: InspoBookAdapter = InspoBookAdapter()
    private val inspobooksViewModel: InspoBooksViewModel by viewModels()
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView() called")

        //set up recyclerView to display inspo books
        val v: View = inflater.inflate(R.layout.fragment_inspo_books, container, false)
        recyclerView = v.findViewById(R.id.inspoBookRecyclerView)
        //use a gridlayout with 2 columns
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = inspoBookAdapter

        inspobooksViewModel.bookList.observe(viewLifecycleOwner){inspobooks ->
            inspoBookAdapter.setInspoBooks(inspobooks)
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //use requireActivity as the toolbar is defined outside of fragment
        toolbar = requireActivity().findViewById<Toolbar>(R.id.inspoBooksToolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.addInspoBook -> {
                    //TODO: add actual changes when add or select is clicked in the toolbar
                    Log.d(TAG,"add inspobook clicked")
                    //Toast.makeText(requireContext(), "add clicked", Toast.LENGTH_SHORT)
                    inspobooksViewModel.addNewBook()
                    true
                }
                R.id.selectInspoBooks -> {
                    //Toast.makeText(requireContext(), "select clicked", Toast.LENGTH_SHORT)
                    Log.d(TAG,"select inspobook clicked")
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
    }
}