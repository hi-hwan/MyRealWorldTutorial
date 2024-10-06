package com.realworld.android.petsave.sharing.presentation.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.realworld.android.petsave.common.data.FakeRepository
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.utils.CoroutineDispatchersProvider
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.sharing.presentation.SharingFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.Reusable
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap

@Module
@DisableInstallInCheck // Hilt를 여전히 의존하고 있기 때문에 @InstallIn 주석을 확인하지 않도록 하기 위함
abstract class SharingModule {

    @Binds
    @IntoMap
    @ViewModelKey(SharingFragmentViewModel::class)
    abstract fun bindSharingFragmentViewModel(
        sharingFragmentViewModel: SharingFragmentViewModel
    ): ViewModel

    @Binds
    @Reusable // @Singleton과 유사, 하지만 동일한 인스턴스가 전체 앱의 수명 동안 살아 있음을 보장하지는 않음
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    abstract fun bindDispatchersProvider(
        dispatchersProvider: CoroutineDispatchersProvider
    ): DispatchersProvider

    @Binds
    abstract fun bindRepository(
        repository: FakeRepository
    ): AnimalRepository
}