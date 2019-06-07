package com.dnylng.steadihand.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dnylng.steadihand.features.pdfreader.PdfReaderViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(PdfReaderViewModel::class)
    abstract fun bindPdfReaderViewModel(viewModel: PdfReaderViewModel): ViewModel

}