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

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

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

            Log.d(TAG, "Attempting sign-in for: $email")

            // Firebase sign-in
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        Log.d(TAG, "Sign-in successful. uid=$uid")

                        Toast.makeText(requireContext(), "Welcome!", Toast.LENGTH_SHORT).show()

                        // navigate to home (ensure action id exists in nav_graph)
                        Log.d(TAG, "Navigating to InspoBooksFragment")
                        findNavController().navigate(R.id.action_LoginFragment_to_InspoBooksFragment)
                    } else {
                        Log.e(TAG, "Sign-in failed: ${task.exception?.message}")
                        Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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
}