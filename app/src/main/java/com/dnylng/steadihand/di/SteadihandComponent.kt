package com.dnylng.steadihand.di

import com.dnylng.steadihand.di.viewmodel.ViewModelModule
import com.dnylng.steadihand.features.pdfreader.PdfReaderFragment
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

    fun inject(target: PdfReaderFragment)
}