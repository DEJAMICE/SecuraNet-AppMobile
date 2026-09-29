package com.example.securanet

import android.app.Application
import com.example.securanet.common.AppContainer
import com.example.securanet.common.DefaultAppContainer

class SecuraNetApplication : Application() {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
