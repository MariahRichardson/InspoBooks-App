package com.zybooks.inspobook.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zybooks.inspobook.R
import com.zybooks.inspobook.viewmodel.UserViewModel
import kotlin.getValue

class UserProfileFragment : Fragment() {

    private val TAG = "UserProfileFragment"

    private val userViewModel: UserViewModel by viewModels()

    // Firebase
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    // Views
    private var imageAvatar: ImageView? = null
    private var textUsername: TextView? = null
    private var editAbout: EditText? = null
    private var btnChangePhoto: Button? = null
    private var btnSave: Button? = null
    private var btnDelete: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() called")
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated() called")
        val bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        //set inspobooks page as starting page after login
        bottomNavView.selectedItemId = R.id.profile

        // Bind views to UI elements
        imageAvatar = view.findViewById(R.id.avatarImage)
        textUsername = view.findViewById(R.id.username)
        editAbout = view.findViewById(R.id.editAbout)
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto)
        btnSave = view.findViewById(R.id.btnSave)
        // Load current profile
        btnSave?.setOnClickListener {
            Log.d(TAG, "Save clicked")
            saveProfile()
        }

        btnDelete?.setOnClickListener {
            Log.d(TAG, "Save clicked")
            deleteUserAccount()
        }

        loadUserProfile(view)

        //any change to user, update username and aboutme text fields
        userViewModel.user.observe(viewLifecycleOwner){user ->
            textUsername?.text = user?.username
            editAbout?.setText(user?.about)
            Log.d("UserProfFrag", "OBSERVED! ${user?.username} and ${user?.about}")
        }
    }

    fun loadUserProfile(view: View) {

        val firebaseUser = userViewModel.currentUser.value
        val uid = firebaseUser?.uid
        userViewModel.getUserProfile(uid!!)
    }

    //Update (save)
    private fun saveProfile() {
        // Get current text
        val username = view?.findViewById<TextView>(R.id.username)?.text?.toString()?.trim() ?: ""
        val about = view?.findViewById<EditText>(R.id.editAbout)?.text?.toString()?.trim() ?: ""

        val updates = hashMapOf<String, Any>(
            "username" to (username.ifEmpty { "@username" }),
            "about" to about
        )
        Log.d("UserProfFrag", "${username} and ${about} and ${updates.size}")

        //update about and username of the user
        userViewModel.updateUserAboutAndUsername(updates)
    }

    //delete
    private fun deleteUserAccount() {

        val isSuccessfulDeletion: Boolean = userViewModel.deleteUserAccount()

        if(isSuccessfulDeletion){
            //is user deletion is successful
            Toast.makeText(requireContext(), getString(R.string.account_deleted), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "User account deleted from Firebase Authentication.")
            // Guide user to the login screen
            findNavController().navigate(R.id.action_UserProfileFragment_to_LoginFragment)
        }
        else{
            //unsuccessful deletion
            Toast.makeText(requireContext(), getString(R.string.account_deleted_fail), Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Failed to delete user account.")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop(){
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView() called")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        super.onDestroy()
    }

}
