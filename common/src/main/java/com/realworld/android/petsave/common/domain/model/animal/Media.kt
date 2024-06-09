/*
 * Copyright (c) 2022 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.realworld.android.petsave.common.domain.model.animal

/**
 * 도메인 모델 객체와 관련된 로직이 있는 경우 해당 로직을 객체 내에 포함시키는 것이 좋다.
 * 높은 응집력이라는 개념에서 이것은 그 좋은 예다
 */
data class Media(
    val photos: List<Photo>,
    val videos: List<Video>
) {

    companion object {
        private const val EMPTY_MEDIA = ""
    }

    fun getFirstSmallestAvailablePhoto(): String {
        if (photos.isEmpty()) return EMPTY_MEDIA

        return photos.first().getSmallestAvailablePhoto()
    }

    data class Photo(
        val medium: String,
        val full: String
    ) {

        companion object {
            // Null 객체 패턴의 단순화된 버전
            const val NO_SIZE_AVAILABLE = ""
        }

        /**
         * 사용 가능한 가장 작은 크기의 사진을 반환하며, 이는 동물 목록에 동물 이미지를 표시하는 데 유용하다.
         * 목록에 고해상도 이미지가 필요하지 않으며 이미지가 작을수록 API에서 요청하는 바이트 수가 줄든다.
         *
         * @return 사용 가능한 가장 작은 크기의 사진
         */
        fun getSmallestAvailablePhoto(): String {
            return when {
                isValidPhoto(medium) -> medium
                isValidPhoto(full) -> full
                else -> NO_SIZE_AVAILABLE
            }
        }

        /**
         * 사진 링크가 유효한지 확인한다.
         *
         * @param photo 사진 링크
         * @return 유효 여부
         */
        private fun isValidPhoto(photo: String): Boolean {
            return photo.isNotEmpty()
        }
    }

    data class Video(val video: String)
}
