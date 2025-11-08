package com.zybooks.inspobook
import com.google.firebase.firestore.FirebaseFirestore

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.zybooks.inspobook.ui.fragment.InspoBooksFragment
import com.zybooks.inspobook.ui.fragment.UserProfileFragment
import com.zybooks.inspobook.ui.fragment.SettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavView : BottomNavigationView

    // private lateinit var db: FirebaseFirestore
    private val TAG : String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance()

        // database testing

        // Create a new user
        val user = hashMapOf(
            "id" to "test0@test.com",
            "name" to "user0",
            "password" to "test0"
        )

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
         */

        bottomNavView = findViewById(R.id.bottomNavigationView)
        //set inspobooks page as starting page after login
        bottomNavView.selectedItemId = R.id.mybooks

        //set up nav graph with the bottom navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavView.setupWithNavController(navController)

        //nav only appears for certain fragments, otherwise don't
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when(destination.id){
                R.id.InspoBooksFragment,
                R.id.UserProfileFragment, R.id.SettingsFragment -> {
                    bottomNavView.visibility = View.VISIBLE
                }
                else ->  bottomNavView.visibility = View.GONE
            }
        }

        bottomNavView.setOnItemSelectedListener{item ->
            //when is the switch statement in Kotlin

            //only navigate is current fragment is not the same as the one you are changing to
            val currentDestinationID = navController.currentDestination?.id
            when(item.itemId){
                R.id.profile -> {
                    //Log.d(TAG, "profile in NavBar clicked")
                    if(R.id.UserProfileFragment != currentDestinationID){
                        navController.navigate(R.id.UserProfileFragment)}
                    true
                }
                R.id.mybooks -> {
                    //Log.d(TAG, "my books in NavBar clicked")
                    if(R.id.InspoBooksFragment != currentDestinationID){
                        navController.navigate(R.id.InspoBooksFragment)}
                    true
                }
              R.id.settings -> {
                    //Log.d(TAG, "Settings in NavBar clicked")
                    if(R.id.SettingsFragment != currentDestinationID){
                        navController.navigate(R.id.SettingsFragment)}
                    true
              }
                else -> false

            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        super.onDestroy()
    }

    //replace main activity layout with fragment, not used as of now
    private fun replaceFragment(fragment: Fragment){
        val fm = supportFragmentManager
        fm.beginTransaction()
            .replace(R.id.fragment_container,fragment)
            .commit()
    }

}


/*
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
 */