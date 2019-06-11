package com.dnylng.steadihand.di

import android.app.Application
import com.dnylng.steadihand.features.stabilization.StabilizationService
import com.dnylng.steadihand.features.stabilization.SteadihandSensor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(
    private val application: Application
) {

    @Provides
    @Singleton
    fun provideApplication() = application

    @Provides
    @Singleton
    fun provideStabilizationService() = StabilizationService(SteadihandSensor(application))
}