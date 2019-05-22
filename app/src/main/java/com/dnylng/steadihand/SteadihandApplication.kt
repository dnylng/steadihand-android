package com.dnylng.steadihand

import android.app.Application
import com.dnylng.steadihand.di.AppModule
import com.dnylng.steadihand.di.DaggerSteadihandComponent
import com.dnylng.steadihand.di.SteadihandComponent

class SteadihandApplication: Application() {

    companion object {
        lateinit var component: SteadihandComponent
    }

    override fun onCreate() {
        super.onCreate()

        component = DaggerSteadihandComponent
            .builder()
            .appModule(AppModule(this))
            .build()
    }
}