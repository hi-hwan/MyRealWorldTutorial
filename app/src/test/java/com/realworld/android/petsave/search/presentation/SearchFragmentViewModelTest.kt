package com.realworld.android.petsave.search.presentation

import com.google.common.truth.Truth.assertThat
import com.realworld.android.petsave.RxImmediateSchedulerRule
import com.realworld.android.petsave.TestCoroutineRule
import com.realworld.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.realworld.android.petsave.search.domain.usecases.GetSearchFilters
import com.realworld.android.petsave.common.data.FakeRepository
import com.realworld.android.petsave.common.presentation.Event
import com.realworld.android.petsave.common.utils.DispatchersProvider
import com.realworld.android.petsave.search.domain.usecases.SearchAnimals
import com.realworld.android.petsave.search.domain.usecases.SearchAnimalsRemotely
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchFragmentViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @get: Rule
    val testCoroutineRule = TestCoroutineRule() // 메인 디스패처 대신 테스트 디스패처로 변경

    @get: Rule
    val rxImmediateThinScheduler = RxImmediateSchedulerRule() // RxJava 스케줄러를 모두 즉시 사용

    private lateinit var viewModel: SearchFragmentViewModel
    private lateinit var repository: FakeRepository
    private lateinit var getSearchFilters: GetSearchFilters

    private val uiAnimalsMapper = UiAnimalMapper()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        val dispatchersProvider = object : DispatchersProvider {
            override fun io() = testCoroutineRule.testDispatcher
        }

        repository = FakeRepository()
        getSearchFilters = GetSearchFilters(repository, dispatchersProvider)

        viewModel = SearchFragmentViewModel(
            uiAnimalsMapper,
            getSearchFilters,
            SearchAnimals(repository),
            SearchAnimalsRemotely(repository, dispatchersProvider),
            CompositeDisposable()
        )
    }

    @Test
    fun `SearchFragmentViewModel remote search with success`() = runTest { // For coroutine
        // Given
        val (name, age, type) = repository.remotelySearchableAnimal
        val (ages, types) = getSearchFilters()

        val expectedRemoteAnimals = repository.remoteAnimals.map {
            uiAnimalsMapper.mapToView(it)
        }

        val expectedViewState = SearchViewState(
            noSearchQuery = false,
            searchResults = expectedRemoteAnimals,
            ageFilterValues = Event(ages),
            typeFilterValues = Event(types),
            searchingRemotely = false,
            noRemoteResults = false
        )

        // When
        viewModel.onEvent(SearchEvent.PrepareForSearch)
        viewModel.onEvent(SearchEvent.TypeValueSelected(type))
        viewModel.onEvent(SearchEvent.AgeValueSelected(age))
        viewModel.onEvent(SearchEvent.QueryInput(name))

        // Then
        val viewState = viewModel.state.value

        assertThat(viewState).isEqualTo(expectedViewState)
    }
}