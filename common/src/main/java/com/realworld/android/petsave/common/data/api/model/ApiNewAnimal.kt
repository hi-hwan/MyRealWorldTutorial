package com.realworld.android.petsave.common.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// generateAdapter 가 true 로 설정하면 Moshi가 자동으로 어댑터를 생성한다.
// 그 후 클래스의 인스턴스를 생성한다.
@JsonClass(generateAdapter = true)
data class ApiBaseResponse<T>(
    @field:Json(name = "response") val response: ApiResponse<T>?,
)

data class ApiResponse<T>(
    @field:Json(name = "header") val header: ApiHeader?,
    @field:Json(name = "body") val body: ApiBody<T>?,
)

data class ApiHeader(
    @field:Json(name = "reqNo") val requestNumber: Long?,
    @field:Json(name = "resultCode") val resultCode: String,
    @field:Json(name = "resultMsg") val resultMessage: String,
    @field:Json(name = "errorMsg") val errorMessage: String?,
)

data class ApiBody<T>(
    @field:Json(name = "items") val requestNumber: ApiItems<T>?,
    @field:Json(name = "numOfRows") val numberOfRows: Int,
    @field:Json(name = "pageNo") val pageNumber: Int,
    @field:Json(name = "totalCount") val totalCount: Int,
)

data class ApiItems<T>(
    @field:Json(name = "item") val item: List<T>?,
)

// 시/도
data class ApiProvince(
    @field:Json(name = "orgCd") val code: String,
    @field:Json(name = "orgdownNm") val name: String,
)

// 시/군/구
data class ApiMunicipality(
    @field:Json(name = "uprCd") val upperRegionCode: String,
    @field:Json(name = "orgCd") val code: String,
    @field:Json(name = "orgdownNm") val name: String,
)

// 보호소
data class ApiShelter(
    @field:Json(name = "careRegNo") val code: String,
    @field:Json(name = "careNm") val name: String,
)

// 품종
data class ApiBread(
    @field:Json(name = "kindCd") val code: String,
    @field:Json(name = "KNm") val name: String,
)

// 유기 동물 상세 정보
data class ApiAbandonedAnimal(
    @field:Json(name = "desertionNo") val abandonmentNumber: String, // 유기동물 관리 번호
    @field:Json(name = "filename") val thumbnailImageUrl: String,    // 썸네일 이미지 URL
    @field:Json(name = "happenDt") val receptionDate: String,        // 발생 일자 (YYYYMMDD 형식)
    @field:Json(name = "happenPlace") val discoveryLocation: String, // 발생 장소
    @field:Json(name = "kindCd") val breed: String,                  // 동물 종류 및 품종
    @field:Json(name = "colorCd") val color: String,                 // 색상
    @field:Json(name = "age") val age: String,                       // 나이 (년도 기준)
    @field:Json(name = "weight") val weight: String,                 // 몸무게 (단위: Kg)
    @field:Json(name = "noticeNo") val noticeNumber: String,         // 공고 번호
    @field:Json(name = "noticeSdt") val noticeStartDate: String,     // 공고 시작일 (YYYYMMDD 형식)
    @field:Json(name = "noticeEdt") val noticeEndDate: String,       // 공고 종료일 (YYYYMMDD 형식)
    @field:Json(name = "popfile") val imageUrl: String,              // 이미지 파일 URL
    @field:Json(name = "processState") val status: String,           // 처리 상태 (예: '보호중', '입양 완료')
    @field:Json(name = "sexCd") val gender: String,                  // 성별 ('M': 남, 'F': 여)
    @field:Json(name = "neuterYn") val neuteredStatus: String,             // 중성화 여부 ('Y': 중성화됨, 'N': 중성화 안됨)
    @field:Json(name = "specialMark") val specialCharacteristics: String,       // 특징
    @field:Json(name = "careNm") val shelterName: String,            // 보호소 이름
    @field:Json(name = "careTel") val shelterPhoneNumber: String,    // 보호소 전화번호
    @field:Json(name = "careAddr") val shelterAddress: String,       // 보호소 주소
    @field:Json(name = "orgNm") val overseeingInstitution: String,        // 관할 기관명
    @field:Json(name = "chargeNm") val responsiblePerson: String,          // 담당자 이름
    @field:Json(name = "officetel") val officePhoneNumber: String   // 담당자 연락처
)