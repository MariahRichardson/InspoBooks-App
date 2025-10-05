package com.zybooks.inspobook

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
        replaceFragment(InspoBooksFragment())
        bottomNavView.selectedItemId = R.id.mybooks

        bottomNavView.setOnItemSelectedListener{item ->
            //when is the switch statement in Kotlin
            when(item.itemId){
                R.id.profile -> {
                    replaceFragment(UserProfileFragment())
                    true
                }
                R.id.mybooks -> {
                    replaceFragment(InspoBooksFragment())
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

//        val fm = supportFragmentManager
//        var fragmentContain = fm.findFragmentById(R.id.fragment_container)
//        if (fragment == null) {
//            fragment = LoginFragment()
//            fm.beginTransaction()
//                .add(R.id.fragment_container, fragment)
//                .commit()
//        }
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
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    //replace main activity layout with fragment
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