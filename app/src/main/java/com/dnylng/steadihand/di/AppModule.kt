package com.dnylng.steadihand.di

import com.dnylng.steadihand.features.pdfreader.PdfReaderViewModel
import com.dnylng.steadihand.features.stabilization.StabilizationSensor
import com.dnylng.steadihand.features.stabilization.StabilizationService
import com.dnylng.steadihand.features.stabilization.SteadihandSensor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Stabilization
    single { StabilizationService(stabilizationSensor = get()) }
    single<StabilizationSensor> { SteadihandSensor(app = get()) }

    // ViewModels
    viewModel { PdfReaderViewModel(app = get(), stabilizationService = get()) }
}