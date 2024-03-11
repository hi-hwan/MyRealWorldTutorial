package com.realworld.android.petsave.common.domain.model.animal

import java.time.LocalDateTime

/**
 * 동물
 *
 * @property id
 * @property name
 * @property type
 * @property media 동물의 사진과 동영상을 처리하는 [Media] 인스턴스
 * @property tags
 * @property adoptionStatus [AdoptionStatus] Enum value
 * @property publishedAt
 */
data class Animal (
    val id: Long,
    val name: String,
    val type: String,
    val media: Media,
    val tags: List<String>,
    val adoptionStatus: AdoptionStatus,
    val publishedAt: LocalDateTime
)