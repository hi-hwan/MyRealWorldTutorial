package com.realworld.android.petsave.search.domain.model

import com.realworld.android.petsave.common.domain.model.animal.Animal

data class SearchResults(
    val animals: List<Animal>,
    val searchParameters: SearchParameters
)
