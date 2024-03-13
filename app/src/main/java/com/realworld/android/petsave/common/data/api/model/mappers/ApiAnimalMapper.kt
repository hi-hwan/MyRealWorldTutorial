package com.realworld.android.petsave.common.data.api.model.mappers

import com.realworld.android.petsave.common.data.api.model.ApiAnimal
import com.realworld.android.petsave.common.domain.model.animal.AdoptionStatus
import com.realworld.android.petsave.common.domain.model.animal.Media
import com.realworld.android.petsave.common.domain.model.animal.details.*
import com.realworld.android.petsave.common.domain.model.organization.Organization
import com.realworld.android.petsave.common.utils.DateTimeUtils
import javax.inject.Inject

class ApiAnimalMapper @Inject constructor(
    private val apiBreedsMapper: ApiBreedsMapper,
    private val apiColorsMapper: ApiColorsMapper,
    private val apiHealthDetailsMapper: ApiHealthDetailsMapper,
    private val apiHabitatAdaptationMapper: ApiHabitatAdaptationMapper,
    private val apiPhotoMapper: ApiPhotoMapper,
    private val apiVideoMapper: ApiVideoMapper,
    private val apiContactMapper: ApiContactMapper
) : ApiMapper<ApiAnimal, AnimalWithDetails> {

    override fun mapToDomain(apiEntity: ApiAnimal): AnimalWithDetails {
        return AnimalWithDetails(
            id = apiEntity.id ?: throw MappingException("Animal ID cannot be null"),
            name = apiEntity.name.orEmpty(),
            type = apiEntity.type.orEmpty(),
            details = parseAnimalDetails(apiEntity),
            media = mapMedia(apiEntity),
            tags = apiEntity.tags.orEmpty().map { it.orEmpty() },
            adoptionStatus = parseAdoptionStatus(apiEntity.status),
            publishedAt = DateTimeUtils.parse(apiEntity.publishedAt.orEmpty())
        )
    }

    private fun parseAnimalDetails(apiEntity: ApiAnimal): Details {
        return Details(
            description = apiEntity.description.orEmpty(),
            age = parseAge(apiEntity.age),
            species = apiEntity.species.orEmpty(),
            breed = apiBreedsMapper.mapToDomain(apiEntity.breeds),
            colors = apiColorsMapper.mapToDomain(apiEntity.colors),
            gender = parserGender(apiEntity.gender),
            size = parseSize(apiEntity.size),
            coat = parseCoat(apiEntity.coat),
            healthDetails = apiHealthDetailsMapper.mapToDomain(apiEntity.attributes),
            habitatAdaptation = apiHabitatAdaptationMapper.mapToDomain(apiEntity.environment),
            organization = mapOrganization(apiEntity)
        )
    }

    private fun parseAge(age: String?): Age {
        if (age.isNullOrEmpty()) return Age.UNKNOWN

        // will throw IllegalStateException if the string does not match any enum value
        return Age.valueOf(age.uppercase())
    }

    private fun parserGender(gender: String?): Gender {
        if (gender.isNullOrEmpty()) return Gender.UNKNOWN

        return Gender.valueOf(gender.uppercase())
    }

    private fun parseSize(size: String?): Size {
        if (size.isNullOrEmpty()) return Size.UNKNOWN

        return Size.valueOf(
            size.replace(' ', '_').uppercase()
        )
    }

    private fun parseCoat(coat: String?): Coat {
        if (coat.isNullOrEmpty()) return Coat.UNKNOWN

        return Coat.valueOf(coat.uppercase())
    }

    private fun mapMedia(apiAnimal: ApiAnimal): Media {
        return Media(
            photos = apiAnimal.photos?.map { apiPhotoMapper.mapToDomain(it) }.orEmpty(),
            videos = apiAnimal.videos?.map { apiVideoMapper.mapToDomain(it) }.orEmpty()
        )
    }

    private fun parseAdoptionStatus(status: String?): AdoptionStatus {
        if (status.isNullOrEmpty()) return AdoptionStatus.UNKNOWN

        return AdoptionStatus.valueOf(status.uppercase())
    }

    private fun mapOrganization(apiAnimal: ApiAnimal): Organization {
        return Organization(
            id = apiAnimal.organizationId
                ?: throw MappingException("Organization ID cannot be null"),
            contact = apiContactMapper.mapToDomain(apiAnimal.contact),
            distance = apiAnimal.distance ?: -1f
        )
    }
}
