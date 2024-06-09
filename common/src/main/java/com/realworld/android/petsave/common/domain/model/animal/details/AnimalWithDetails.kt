package com.realworld.android.petsave.common.domain.model.animal.details

import com.realworld.android.petsave.common.domain.model.animal.AdoptionStatus
import com.realworld.android.petsave.common.domain.model.animal.Media
import java.time.LocalDateTime

/**
 * [Animal]과 동일하지만 [Details] Property가 추가되어 있다.
 */
data class AnimalWithDetails(
    val id: Long,
    val name: String,
    val type: String,
    val details: Details,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedAt: LocalDateTime
)
