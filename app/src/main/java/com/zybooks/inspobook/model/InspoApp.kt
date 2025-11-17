package com.zybooks.inspobook.model

import android.app.Application
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker

class InspoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        UnsplashPhotoPicker.init(
            this,                         // application context
            "8ztm8QyC5nVBG3HjhbwlUusVB7VVZepIlC10qjlJNjk",       // from Unsplash Developer Portal
            "2-h7kjPawmoBDfBTpD45k3IhAJ1i-9GNdwOcoxfXE94",       // from Unsplash Developer Portal
            30                             // page size
        )
    }
}