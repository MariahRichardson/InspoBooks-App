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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zybooks.inspobook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zybooks.inspobook.viewmodel.UserViewModel
import com.zybooks.inspobook.model.User

class SignupFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val TAG = "SignupFragment"

    private var selectedPFPPath: String? = null // temporary

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() called")
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

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

            Log.d(TAG, "Attempting to create user via ViewModel with email: $email")

            userViewModel.register(email, password, email.substringBefore("@"))
                .observe(viewLifecycleOwner) { result ->
                    result.onSuccess {
                        Log.d(TAG, "Firebase Auth registration successful")

                        val firebaseUser = userViewModel.currentUser.value
                        val uid = firebaseUser?.uid ?: return@observe

                        val newUser = User(
                            uid = userViewModel.currentUser.value?.uid ?: "",
                            email = email,
                            password = password,
                            username = email.substringBefore("@"),
                            pfp = selectedPFPPath ?: ""
                        )

                        userViewModel.saveUserProfile(newUser)
                            .observe(viewLifecycleOwner) { saveResult ->
                                saveResult.onSuccess {
                                    Toast.makeText(requireContext(), "Account created!", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_signupFragment_to_inspoBooksFragment)
                                }
                                saveResult.onFailure { e ->
                                    Log.e(TAG, "Failed to save profile: ${e.message}")
                                    Toast.makeText(requireContext(), "Error saving profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }

                    result.onFailure { e ->
                        Log.e(TAG, "Registration failed: ${e.message}")
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
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