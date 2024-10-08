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

package com.realworld.android.petsave.common.domain.model.organization

data class Organization(
    val id: String,
    val contact: Contact,
    val distance: Float
) {

    data class Contact(
        val email: String,
        val phone: String,
        val address: Address
    ) {
        val formattedAddress: String = address.createFormattedAddress()
        val formattedContactInfo: String = createFormattedContactInfo()

        private fun createFormattedContactInfo(): String {
            val builder = StringBuilder()

            if (email.isNotEmpty()) {
                builder.append("Email: ").append(email)
            }

            if (phone.isNotEmpty()) {
                if (email.isNotEmpty()) {
                    builder.append("\n")
                }

                builder.append("Phone: ").append(phone)
            }

            return builder.toString()
        }
    }

    data class Address(
        val address1: String,
        val address2: String,
        val city: String,
        val state: String,
        val postcode: String,
        val country: String
    ) {
        fun createFormattedAddress(): String {
            val detailsSeparator = " "
            val newLineSeparator = "\n"
            val builder = StringBuilder();

            if (address1.isNotEmpty()) {
                builder.append(address1)
            }

            if (address2.isNotEmpty()) {
                if (address1.isNotEmpty()) {
                    builder.append(newLineSeparator)
                }

                builder.append(address2)
            }

            builder
                .append(city)
                .append(detailsSeparator)
                .append(state)
                .append(detailsSeparator)
                .append(postcode)
                .append(newLineSeparator)
                .append(country)

            return builder.toString()
        }
    }
}