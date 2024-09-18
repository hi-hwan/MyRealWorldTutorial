package com.realworld.android.petsave.common.data.cache.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalWithDetails
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedPhoto
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedTag
import com.realworld.android.petsave.common.data.cache.model.cachedanimal.CachedVideo
import io.reactivex.Flowable

@Dao
abstract class AnimalsDao {
    // @Transaction 쿼리 결과가 너무 큰 경우 이 버퍼가 오버플로될 수 있어 데이터가 손상될 수 있는데
    // 이를 방지한다. 또한 하나의 결과에 대해 다른 테이블을 쿼리할 때 일관된 결과를 얻도록 보장한다.
    @Transaction
    @Query("SELECT * FROM animals")
    abstract fun getAllAnimals(): Flowable<List<CachedAnimalAggregate>>

    @Transaction
    @Query("SELECT * FROM animals WHERE animalId IS :animalId")
    abstract suspend fun getAnimal(animalId: Long): CachedAnimalAggregate

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAnimalAggregate(
        animal: CachedAnimalWithDetails,
        photos: List<CachedPhoto>,
        videos: List<CachedVideo>,
        tags: List<CachedTag>
    )

    suspend fun insertAnimalsWithDetails(animalAggregates: List<CachedAnimalAggregate>) {
        animalAggregates.forEach {
            insertAnimalAggregate(it.animal, it.photos, it.videos, it.tags)
        }
    }

    @Query("SELECT DISTINCT type FROM animals")
    abstract suspend fun getAllTypes(): List<String>

    @Transaction
    @Query("""
        SELECT * FROM animals
            WHERE name LIKE '%' || :name || '%' AND
            age LIKE '%' || :age || '%' AND
            type LIKE '%' || :type || '%'
    """)
    abstract fun searchAnimalsBy(
        name: String,
        age: String,
        type: String
    ): Flowable<List<CachedAnimalAggregate>>
}