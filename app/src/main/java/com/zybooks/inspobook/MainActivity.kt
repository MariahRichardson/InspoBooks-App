package com.zybooks.inspobook

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private lateinit var firstFrag: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



//        val fm = supportFragmentManager
//        var fragmentContain = fm.findFragmentById(R.id.fragment_container)
//        if (fragment == null) {
//            fragment = LoginFragment()
//            fm.beginTransaction()
//                .add(R.id.fragment_container, fragment)
//                .commit()
//        }
    }

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