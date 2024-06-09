package com.realworld.android.petsave.common.di

import com.realworld.android.petsave.common.data.FakeRepository
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import com.realworld.android.petsave.common.utils.CoroutineDispatchersProvider
import com.realworld.android.petsave.common.utils.DispatchersProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.testing.TestInstallIn
import io.reactivex.disposables.CompositeDisposable

@Module
@TestInstallIn(
    components = [ActivityRetainedComponent::class],
    replaces = [ActivityRetainedModule::class]
)
abstract class TestActivityRetainedModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindAnimalRepository(repository: FakeRepository): AnimalRepository

    @Binds
    abstract fun bindDispatchersProvider(
        dispatchersProvider: CoroutineDispatchersProvider
    ): DispatchersProvider

    companion object {
        @Provides
        fun provideCompositeDisposable() = CompositeDisposable()
    }
}
