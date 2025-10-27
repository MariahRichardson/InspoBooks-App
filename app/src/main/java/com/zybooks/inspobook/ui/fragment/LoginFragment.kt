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
import com.zybooks.inspobook.viewmodel.UserViewModel

class LoginFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() called")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated() called")

        // find UI elements (ids must match fragment_login.xml)
        val emailField = view.findViewById<EditText>(R.id.editTextId)
        val passwordField = view.findViewById<EditText>(R.id.editTextPassword)
        val loginButton = view.findViewById<Button>(R.id.buttonLogin)
        val signupButton = view.findViewById<Button>(R.id.buttonSignup)

        // Login button click
        loginButton.setOnClickListener {
            Log.d(TAG, "Login button clicked")

            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            // simple validation
            if (email.isEmpty() || password.isEmpty()) {
                Log.d(TAG, "Validation failed: empty email or password")
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d(TAG, "Attempting sign-in via ViewModel for: $email")

            userViewModel.login(email, password).observe(viewLifecycleOwner) { result ->
                result.onSuccess {
                    Log.d(TAG, "Sign-in successful.")

                    val firebaseUser = userViewModel.currentUser.value
                    val uid = firebaseUser?.uid

                    if (uid != null) {
                        userViewModel.getUserProfile(uid).observe(viewLifecycleOwner) { profile ->
                            if (profile != null) {
                                Log.d(TAG, "Fetched Firestore profile: ${profile.username}")
                            } else {
                                Log.w(TAG, "No Firestore profile found for UID=$uid")
                            }

                            Toast.makeText(requireContext(), "Welcome!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_LoginFragment_to_InspoBooksFragment)
                        }
                    } else {
                        Log.w(TAG, "No Firebase user found after login")
                    }
                }

                result.onFailure { e ->
                    Log.e(TAG, "Login failed: ${e.message}")
                    Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Sign up button click -> go to SignupFragment
        signupButton.setOnClickListener {
            Log.d(TAG, "Sign Up button clicked - navigating to SignupFragment")
            findNavController().navigate(R.id.action_LoginFragment_to_SignupFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
        // Auto-redirect if user already signed in
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            Log.d(TAG, "User already signed in (uid=${currentUser.uid}). Auto-navigating to home.")
//            // extra safety: only navigate if this fragment is current destination
//            val nav = findNavController()
//            if (nav.currentDestination?.id == R.id.loginFragment) {
//                nav.navigate(R.id.action_LoginFragment_to_InspoBooksFragment)
//            }
//        } else {
//            Log.d(TAG, "No signed-in user at onStart.")
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
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