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

package com.realworld.android.petsave.common.data.di

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.realworld.android.petsave.common.data.api.ApiConstants
import com.realworld.android.petsave.common.data.api.Authenticator
import com.realworld.android.petsave.common.data.api.ClientAuthenticator
import com.realworld.android.petsave.common.data.api.PetFinderApi
import com.realworld.android.petsave.common.data.api.ReportManager
import com.realworld.android.petsave.common.data.api.ServerAuthenticator
import com.realworld.android.petsave.common.data.api.interceptors.AuthenticationInterceptor
import com.realworld.android.petsave.common.data.api.interceptors.LoggingInterceptor
import com.realworld.android.petsave.common.data.api.interceptors.NetworkStatusInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.annotation.Signed
import javax.inject.Singleton

// object 로 만들면 Dagger가 객체 인스턴스를 생성하는 비용을 들이지 않고도 종속성을 제송할 수 있다.
@InstallIn(SingletonComponent::class)
@Module
object ApiModule {

    @Provides
    @Singleton
    fun provideApi(okHttpClient: OkHttpClient): PetFinderApi {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_ENDPOINT)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PetFinderApi::class.java)
    }

    @Provides
    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        networkStatusInterceptor: NetworkStatusInterceptor,
        authenticationInterceptor: AuthenticationInterceptor,
    ): OkHttpClient {

        // Android N 이하 버전에서 동작할 수 있게 추가
        val hostname = "**.petfinder.com" // ** 추가하면 모든 서브도메인에 핀닝이 적용
        val certificate = CertificatePinner.Builder()
            .add(hostname, "sha256/d64mTAGzLoXnGwQTGqE/SMJqc2On+QNsyzxj8kW9UPU=")
            .add(hostname, "sha256/vxRon/El5KuI4vx5ey1DgmsYmRY0nDd5Cg4GfJ8S+bg=")
            .build()

        // 인증서 투명성 (Certificate Transparency)
        // 앱에 하드코딩된 값 없이 제출된 인증서를 감사하는 새로운 표준
        val ctInterceptor = certificateTransparencyInterceptor {
            +"*.petfinder.com" // 서브도메인
            +"petfinder.com" // *은 기본 도메인 포함하지 않기 때문에 추가
            //+"*.*" 모든 호스트 추가
            //-"legacy.petfinder.com 특정 호스트 제거
        }

        // Network -> Authentication -> Logging
        return OkHttpClient.Builder()
            .certificatePinner(certificate)
            .addNetworkInterceptor(ctInterceptor)
            .addInterceptor(networkStatusInterceptor)
            .addInterceptor(authenticationInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .cache(null)
            .build()
    }

    @Provides
    fun provideHttpLoggingInterceptor(loggingInterceptor: LoggingInterceptor): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor(loggingInterceptor)

        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return interceptor
    }

    @Provides
    @Singleton
    fun provideReportManager(
        serverAuthenticator: ServerAuthenticator
    ): ReportManager {
        return ReportManager(serverAuthenticator)
    }

    @Provides
    @Singleton
    fun provideClientAuthenticator() = ClientAuthenticator()

    @Provides
    fun provideServerAuthenticator() = ServerAuthenticator()
}