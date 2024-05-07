package com.realworld.android.petsave.common.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.realworld.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.realworld.android.petsave.common.data.api.utils.FakeServer
import com.realworld.android.petsave.common.data.cache.Cache
import com.realworld.android.petsave.common.data.cache.PetSaveDatabase
import com.realworld.android.petsave.common.data.cache.RoomCache
import com.realworld.android.petsave.common.data.di.CacheModule
import com.realworld.android.petsave.common.data.di.PreferencesModule
import com.realworld.android.petsave.common.data.preferences.FakePreferences
import com.realworld.android.petsave.common.data.preferences.Preferences
import com.realworld.android.petsave.common.domain.repositories.AnimalRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Instant
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(
    PreferencesModule::class,
    CacheModule::class
) // 테스트 모듈로 대체하기 위해 [PreferencesModule, CacheModule] 호출하지 않음
class PetFinderAnimalRepositoryTest {
    private val fakeServer = FakeServer() // MockWebServer
    private lateinit var repository: AnimalRepository
    private lateinit var api: PetFinderApi
    private lateinit var cache: Cache

    // Inject 전에 필요한 모든 Configuration을 처리할 여지를 제공
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // Room이 모든 작업을 즉시 실행하도록 보장하고 싶을때, 백그라운드 실행기를 동기식으로 교체하는 JUnit 규칙
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: PetSaveDatabase

    @Inject
    lateinit var apiAnimalMapper: ApiAnimalMapper

    @Inject
    lateinit var apiPaginationMapper: ApiPaginationMapper

    // Replacement and Injection
    @BindValue
    val preferences: Preferences = FakePreferences()

    @Module
    @InstallIn(SingletonComponent::class)
    object TestCacheModule {
        @Provides
        fun provideRoomDatabase(): PetSaveDatabase {
            return Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                PetSaveDatabase::class.java
            )
                .allowMainThreadQueries() // 테스트에서 쿼리를 실행하는 스레드를 무시
                .build()
        }
    }

    @Before
    fun setup() {
        fakeServer.start()

        preferences.deleteTokenInfo()
        preferences.putToken("validToken")
        preferences.putTokenExpirationTime(
            Instant.now().plusSeconds(3600).epochSecond
        )
        preferences.putTokenType("Bearer")

        // Completes injection
        hiltRule.inject()

        api = Retrofit.Builder()
            .baseUrl(fakeServer.baseEndpoint)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PetFinderApi::class.java)

        cache = RoomCache(database.animalsDao(), database.organizationsDao())

        repository = PetFinderAnimalRepository(api, cache, apiAnimalMapper, apiPaginationMapper)
    }

    @After
    fun teardown() {
        fakeServer.shutdown()
    }

    @Test
    fun requestMoreAnimals_success() = runBlocking {
        // Given
        val expectedAnimalId = 124L
        fakeServer.setHappyPathDispatcher()

        // When
        val paginatedAnimals = repository.requestMoreAnimals(1, 100)

        // Then
        val animal = paginatedAnimals.animals.first()
        assertThat(animal.id).isEqualTo(expectedAnimalId)
    }

    @Test
    fun insertAnimals_success() {
        // Given
        val expectedAnimalId = 124L

        runBlocking {
            fakeServer.setHappyPathDispatcher()

            val paginatedAnimals = repository.requestMoreAnimals(1, 100)
            val animal = paginatedAnimals.animals.first()

            // When
            repository.storeAnimals(listOf(animal))
        }

        // Then
        val testObserver = repository.getAnimals().test()

        testObserver.assertNoErrors()
        testObserver.assertNotComplete()
        testObserver.assertValue { it.first().id == expectedAnimalId }
    }
}