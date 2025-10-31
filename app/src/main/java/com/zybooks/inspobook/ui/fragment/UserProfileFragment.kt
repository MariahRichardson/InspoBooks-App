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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zybooks.inspobook.R

class UserProfileFragment : Fragment() {

    private val TAG = "UserProfileFragment"

    // Firebase
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    //private val storage by lazy { FirebaseStorage.getInstance() }

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

        // Bind views to UI elements
        imageAvatar = view.findViewById(R.id.avatarImage)
        textUsername = view.findViewById(R.id.username)
        editAbout = view.findViewById(R.id.editAbout)
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto)
        btnSave = view.findViewById(R.id.btnSave)
        btnDelete = view.findViewById(R.id.btnDelete)
        // Load current profile
        loadUserProfile(view)
    }

    fun loadUserProfile(view: View) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val username = snapshot.getString("username") ?: "@username"
                    val about = snapshot.getString("about") ?: ""
                    //val photoUrl = snapshot.getString("photoUrl")

                    val usernameText = view.findViewById<TextView>(R.id.username)
                    val aboutEdit = view.findViewById<EditText>(R.id.editAbout)
                    //val avatarImg = view.findViewById<ImageView>(R.id.avatarImage)

                    usernameText.text = username
                    aboutEdit.setText(about)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT)
                    .show()
                Log.e(TAG, "Error loading profile: ${it.message}")
            }
//            // Clicks
//            btnChangePhoto?.setOnClickListener
//            {
//                Log.d(TAG, "Change Photo clicked")
//            }
//
            btnSave?.setOnClickListener {
                Log.d(TAG, "Save clicked")
                saveProfile()
            }
    }

    //Update (save)
    private fun saveProfile() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Not signed in.", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "saveProfile: no user logged in")
            return
        }

        // Get current text
        val username = view?.findViewById<TextView>(R.id.username)?.text?.toString()?.trim() ?: ""
        val about = view?.findViewById<EditText>(R.id.editAbout)?.text?.toString()?.trim() ?: ""

        val updates = hashMapOf<String, Any>(
            "username" to (username.ifEmpty { "@username" }),
            "about" to about
        )

        // write updates to db
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "saveProfile: profile updated successfully")
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "saveProfile: failed to update profile", e)
            }
    }

    //delete
    private fun deleteProfile() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()


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
