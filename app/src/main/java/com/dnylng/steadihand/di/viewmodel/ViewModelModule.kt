package com.dnylng.steadihand.di.viewmodel

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

//    @Binds
//    @IntoMap
//    @ViewModelKey(InsertViewModel::class)
//    abstract fun bindTodayViewModel(viewModel: InsertViewModel): ViewModel

}