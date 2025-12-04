package com.zybooks.inspobook.ui.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zybooks.inspobook.R
import com.zybooks.inspobook.adapter.InspoBookAdapter
import com.zybooks.inspobook.model.InspoBook
import com.zybooks.inspobook.viewmodel.InspoBooksViewModel

class InspoBooksFragment : Fragment(), InspoBookAdapter.OnItemClickListener {

    private val TAG : String = "InspoBooksFragment"
    private lateinit var recyclerView: RecyclerView
    lateinit var inspoBookAdapter: InspoBookAdapter
    //use activityViewModels instead of viewModels so that the view model is in the scope of the activity
    private val inspobooksViewModel: InspoBooksViewModel by activityViewModels()
    private lateinit var toolbar: Toolbar
    private var isSelectClicked = false

    private lateinit var bottomNavView : BottomNavigationView
    private var menuReference: Menu? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView() called")

        inspobooksViewModel.setUpInspoBooks()
        inspoBookAdapter = InspoBookAdapter(emptyList(), this)
        val bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        //set inspobooks page as starting page after login
        bottomNavView.selectedItemId = R.id.mybooks

        //set up recyclerView to display inspo books, can scroll down and recyclerview will update
        val v: View = inflater.inflate(R.layout.fragment_inspo_books, container, false)
        recyclerView = v.findViewById(R.id.inspoBookRecyclerView)
        //use a gridlayout with 2 columns
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = inspoBookAdapter
        setRetainInstance(true)
        return v
    }

    fun isInternetAvailable(context: Context): Boolean{
        //get network info
        val connectManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectManager.activeNetwork
        val capabilities = connectManager.getNetworkCapabilities(network)

        //check if network's capabilities are available and if device has internet access
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    override fun onItemClick(item: InspoBook) {

        //only navigate to inspopage on click if internet is available
        if(isInternetAvailable(requireContext())) {
            //navigate from inspobooks fragment to inspopage fragment and pass "item" that was clicked to inspopage fragment
            Toast.makeText(requireContext(), "Opening Book! Please wait...", Toast.LENGTH_LONG).show()
            val action = InspoBooksFragmentDirections.actionInspoBooksFragmentToInspoPageFragment(item)
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")

        //set up top toolbar after fragment is created
        //use requireActivity as the toolbar is defined outside of fragment
        toolbar = requireActivity().findViewById<Toolbar>(R.id.inspoBooksToolbar)
        menuReference = toolbar.menu
        updatedToolbarVisibility()



        toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.addInspoBook -> {
                    //TODO: add actual changes when add or select is clicked in the toolbar
                    Log.d(TAG,"add inspobook clicked")
                    //Toast.makeText(requireContext(), "add clicked", Toast.LENGTH_SHORT)
                    inspobooksViewModel.addBook()
                    true
                }
                R.id.selectInspoBooks -> {
                    //Toast.makeText(requireContext(), "select clicked", Toast.LENGTH_SHORT)
                    Log.d(TAG,"select inspobook clicked")
                    inspoBookAdapter.isSelectMode = true
                    isSelectClicked = !isSelectClicked
                    //if select is clicked after selecting again clear selection
                    if(!isSelectClicked){
                        inspoBookAdapter.clearAllSelections()
                    }
                    updatedToolbarVisibility()
                    true
                }
                R.id.deleteSelectedBooks -> {
                    val selectedItems = inspoBookAdapter.getSelectedItems()
                    inspobooksViewModel.removeBooks(selectedItems)
                    isSelectClicked = !isSelectClicked
                    updatedToolbarVisibility()
                    true
                }
                R.id.editInspoBookName -> {
                    val selectedItems = inspoBookAdapter.getSelectedItems()
                    //if there is not only one item selected
                    if(selectedItems.size != 1){
                        Toast.makeText(requireContext(), "Select only ONE book to edit name of", Toast.LENGTH_LONG).show()
                        inspoBookAdapter.clearAllSelections()
                    }
                    else{
                        //update the selected book's name(should be only item in selectedItems so index 0
                        showBookNameEditDialog(selectedItems[0])
                        //inspoBookAdapter.clearAllSelections()

                    }

                    isSelectClicked = !isSelectClicked
                    updatedToolbarVisibility()
                    true
                }
                else -> false
            }
        }

        //observe the livedata(the list of inspobooks), triggers whenever the list is changed
        inspobooksViewModel.bookList.observe(viewLifecycleOwner){inspobooks ->
            inspoBookAdapter.updateInspoBooks(inspobooks)
        }
    }

    //adjust visibility of options depending on is the select option is clicked in the toolbar
    fun updatedToolbarVisibility(){
        if(isSelectClicked){
            menuReference?.findItem(R.id.addInspoBook)?.isVisible = true
            menuReference?.findItem(R.id.selectInspoBooks)?.isVisible = true
            menuReference?.findItem(R.id.deleteSelectedBooks)?.isVisible = true
            menuReference?.findItem(R.id.editInspoBookName)?.isVisible = true
        }
        else{
            menuReference?.findItem(R.id.addInspoBook)?.isVisible = true
            menuReference?.findItem(R.id.selectInspoBooks)?.isVisible = true
            menuReference?.findItem(R.id.deleteSelectedBooks)?.isVisible = false
            menuReference?.findItem(R.id.editInspoBookName)?.isVisible = false
        }
    }

    fun showBookNameEditDialog(selectedBook: InspoBook){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter new name for your selected InspoBook")
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setCancelable(false)
        builder.setView(input)

        builder.setPositiveButton("Change Name"){dialog, which ->
            //update selected book's name
            val newName = input.text.toString()
            inspobooksViewModel.updateBookName(selectedBook, newName)
        }
        builder.setNegativeButton("Cancel"){dialog, which ->
            //remove dialog is cancel button is clicked
            dialog.cancel()
            inspoBookAdapter.clearAllSelections()
        }

        builder.show()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView() called")
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")

        bottomNavView = requireActivity().findViewById(R.id.bottomNavigationView)
        //set inspobooks page as starting page after login
        bottomNavView.selectedItemId = R.id.mybooks
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        super.onDestroy()
    }
}