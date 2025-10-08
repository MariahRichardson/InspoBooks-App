package com.zybooks.inspobook

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

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavView : BottomNavigationView
    private val TAG : String = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                R.id.UserProfileFragment -> {
                    bottomNavView.visibility = View.VISIBLE
                }
                else ->  bottomNavView.visibility = View.GONE
            }
        }

        bottomNavView.setOnItemSelectedListener{item ->
            //when is the switch statement in Kotlin
            when(item.itemId){
                R.id.profile -> {
                    //Log.d(TAG, "profile in NavBar clicked")
                    navController.navigate(R.id.UserProfileFragment)
                    true
                }
                R.id.mybooks -> {
                    //Log.d(TAG, "my books in NavBar clicked")
                    navController.navigate(R.id.InspoBooksFragment)
                    true
                }
//                R.id.settings -> {
//
//                    true
//                }
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