package com.zybooks.inspobook.ui.fragment

import android.app.AlertDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zybooks.inspobook.R
import com.zybooks.inspobook.viewmodel.UserViewModel
import kotlin.getValue

class SettingsFragment : Fragment() {

    private val TAG = "SettingsFragment"
    private val userViewModel: UserViewModel by viewModels()
    private var btnLogout: Button? = null
    private var btnDeleteProfile: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() called")
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated() called")

        // Bind views to UI elements
        btnLogout = view.findViewById(R.id.btnLogout)
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile)

        // Logout button clicked
        btnLogout?.setOnClickListener {
            Log.d(TAG, "Save clicked")
            logout()
        }

        //Delete profile button clicked
        btnDeleteProfile?.setOnClickListener {
            Log.d(TAG, "Save clicked")
            deleteUserAccount()
        }

    }
    //Logout
    private fun logout() {
        userViewModel.logout()
        Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.loginFragment)
    }

    //delete
    private fun deleteUserAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you would like to delete your account?")
            .setPositiveButton("Yes") { _, _ ->
                // when user confirms deletion, perform deletion
                val isSuccessfulDeletion: Boolean = userViewModel.deleteUserAccount()

                if (isSuccessfulDeletion) {
                    Toast.makeText(requireContext(), "Account Deleted", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "User account deleted from Firebase Authentication.")
                    findNavController().navigate(R.id.loginFragment)
                } else {
                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "Failed to delete user account.")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
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
