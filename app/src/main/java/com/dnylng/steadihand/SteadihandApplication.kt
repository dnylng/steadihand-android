package com.dnylng.steadihand

import android.app.Application
import com.dnylng.steadihand.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SteadihandApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SteadihandApplication)
            modules(listOf(
                appModule
            ))
        }
    }
}