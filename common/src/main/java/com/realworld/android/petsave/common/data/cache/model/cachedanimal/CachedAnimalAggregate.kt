package com.realworld.android.petsave.common.data.cache.model.cachedanimal

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.realworld.android.petsave.common.domain.model.animal.details.AnimalWithDetails

// 관계를 모델링하는 데이터 클래스
// 캐시된 데이터를 도메인 데이터로 매핑하기 위해 사용
data class CachedAnimalAggregate(
    @Embedded
    val animal: CachedAnimalWithDetails,
    @Relation(
        parentColumn = "animalId",
        entityColumn = "animalId"
    )
    val photos: List<CachedPhoto>,
    @Relation(
        parentColumn = "animalId",
        entityColumn = "animalId"
    )
    val videos: List<CachedVideo>,
    @Relation(
        parentColumn = "animalId", // 엔티티를 쿼리하는 방식을 정의
        entityColumn = "tag", // 엔티티를 쿼리하는 방식을 정의
        associateBy = Junction(CachedAnimalTagCrossRef::class) // 다대다 관계를 생성, 교차 참조 클래스를 매개변수로 사용
    )
    val tags: List<CachedTag>
) {
    companion object {
        fun fromDomain(animalWithDetails: AnimalWithDetails): CachedAnimalAggregate {
            return CachedAnimalAggregate(
                animal = CachedAnimalWithDetails.fromDomain(animalWithDetails),
                photos = animalWithDetails.media.photos.map {
                    CachedPhoto.fromDomain(animalWithDetails.id, it)
                },
                videos = animalWithDetails.media.videos.map {
                    CachedVideo.fromDomain(animalWithDetails.id, it)
                },
                tags = animalWithDetails.tags.map { CachedTag(it) }
            )
        }
    }
}
