package com.dnylng.steadihand.di

import com.dnylng.steadihand.MainActivity
import com.dnylng.steadihand.di.viewmodel.ViewModelModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelModule::class
    ]
)
interface SteadihandComponent {

    fun inject(target: MainActivity)

}