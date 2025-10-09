package com.zybooks.inspobook.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zybooks.inspobook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "SignupFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() called")
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        auth = FirebaseAuth.getInstance()

        val emailField = view.findViewById<EditText>(R.id.editTextEmail)
        val passwordField = view.findViewById<EditText>(R.id.editTextPassword)
        val createButton = view.findViewById<Button>(R.id.buttonCreateAccount)
        val clearButton = view.findViewById<Button>(R.id.buttonClear)
        val backButton = view.findViewById<Button>(R.id.buttonGoBack)

        // Create Account
        createButton.setOnClickListener {
            Log.d(TAG, "Create Account button clicked")

            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Log.d(TAG, "Validation failed: empty fields")
                Toast.makeText(requireContext(), "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting to create user with email: $email")

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        Log.d(TAG, "Account creation successful, UID: $userId")

                        // Save extra user data in Cloud Firestore
                        val db = FirebaseFirestore.getInstance()
                        val user = hashMapOf(
                            "email" to email,
                            "username" to email.substringBefore("@")
                        )
                        // Add a new user with a generated ID
                        db.collection("users")
                            .add(user)
                            .addOnSuccessListener { documentReference ->
                                Log.d(TAG, "User data saved successfully in Cloud Firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Failed to save user data.", e)
                            }

                        Toast.makeText(requireContext(), "Account created!", Toast.LENGTH_SHORT).show()

                        // Navigate to InspoBooksFragment
                        Log.d(TAG, "Navigating to InspoBooksFragment")
                        findNavController().navigate(R.id.action_signupFragment_to_inspoBooksFragment)

                    } else {
                        Log.e(TAG, "Account creation failed: ${task.exception?.message}")
                        Toast.makeText(requireContext(), "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Clear input fields
        clearButton.setOnClickListener {
            Log.d(TAG, "Clear button clicked")
            emailField.text.clear()
            passwordField.text.clear()
        }

        // Go back to LoginFragment
        backButton.setOnClickListener {
            Log.d(TAG, "Go Back button clicked - popping back stack")
            findNavController().popBackStack()
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
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