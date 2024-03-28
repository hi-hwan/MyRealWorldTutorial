package com.realworld.android.petsave.common.data.api.interceptors

import com.google.common.truth.Truth.assertThat
import com.realworld.android.petsave.common.data.api.ApiConstants
import com.realworld.android.petsave.common.data.api.ApiParameters
import com.realworld.android.petsave.common.data.api.utils.JsonReader
import com.realworld.android.petsave.common.data.preferences.Preferences
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import java.time.Instant

// 에뮬레이터 혹은 단말기 없이 안드로이드 프레임워크에 액세스하기 위해 Robolectric 추가
@RunWith(RobolectricTestRunner::class)
class AuthenticationInterceptorTest {
    private lateinit var preferences: Preferences
    private lateinit var mockWebServer: MockWebServer
    private lateinit var authenticationInterceptor: AuthenticationInterceptor
    private lateinit var okHttpClient: OkHttpClient

    private val endpointSeparator = "/"
    private val animalsEndpointPath = endpointSeparator + ApiConstants.ANIMALS_ENDPOINT
    private val authEndpointPath = endpointSeparator + ApiConstants.AUTH_ENDPOINT
    private val validToken = "validToken"
    private val expiredToken = "expiredToken"

    @Before
    fun setup() {
        preferences = mock(Preferences::class.java)

        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        authenticationInterceptor = AuthenticationInterceptor(preferences)
        okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(authenticationInterceptor)
            .build()
    }

    @Test
    fun authenticationInterceptor_validToken() {
        // Given
        `when`(preferences.getToken()).thenReturn(validToken)
        `when`(preferences.getTokenExpirationTime()).thenReturn(
            Instant.now().plusSeconds(3600).epochSecond
        )
        mockWebServer.dispatcher = getDispatcherForValidToken()

        // When
        okHttpClient.newCall(
            Request.Builder()
                .url(mockWebServer.url(ApiConstants.ANIMALS_ENDPOINT))
                .build()
        ).execute()

        // Then
        val request = mockWebServer.takeRequest() // HTTP 요청을 기다린다.

        // request 를 범위로 설정하고 몇가지 요청 매개변수를 확인
        with (request) {
            assertThat(method).isEqualTo("GET")
            assertThat(path).isEqualTo(animalsEndpointPath)
            assertThat(getHeader(ApiParameters.AUTH_HEADER))
                .isEqualTo(ApiParameters.TOKEN_TYPE + validToken)
        }
    }

    @Test
    fun authenticationInterceptor_expiredToken() {
        // Given
        // `when`은 이 테스트에 대해 모의로 반환해야 할 내용을 설정
        `when`(preferences.getToken()).thenReturn(expiredToken)
        `when`(preferences.getTokenExpirationTime()).thenReturn(
            Instant.now().minusSeconds(3600).epochSecond
        )
        mockWebServer.dispatcher = getDispatcherForExpiredToken()

        // When
        okHttpClient.newCall(
            Request.Builder()
                .url(mockWebServer.url(ApiConstants.ANIMALS_ENDPOINT))
                .build()
        ).execute()

        // Then
        val tokenRequest = mockWebServer.takeRequest() // 첫 번째 요청은 새 토큰에 대한 요청
        val animalsRequest = mockWebServer.takeRequest() // 새 토큰 이후 /animals 엔드포인트에 요청

        with(tokenRequest) {
            assertThat(method).isEqualTo("POST")  // POST 요청인지 확인
            assertThat(path).isEqualTo(authEndpointPath) // Auth 엔드포인트를 가리키는지 확인
        }

        val inOrder = inOrder(preferences) // Preference 에 대한 작업을 확인

        // getToken 이 putToken 보다 먼저 호출되는지 확인
        inOrder.verify(preferences).getToken()
        inOrder.verify(preferences).putToken(validToken)

        // 각 Preferences 가 한 번씩만 호출되는지 확인한다.
        verify(preferences, times(1)).getToken()
        verify(preferences, times(1)).putToken(validToken)
        verify(preferences, times(1)).getTokenExpirationTime()
        verify(preferences, times(1)).putTokenExpirationTime(anyLong())
        verify(preferences, times(1)).putTokenType(ApiParameters.TOKEN_TYPE.trim())
        verifyNoMoreInteractions(preferences) // 이외의 다른 메서드가 호출되지 않는지 확인

        // 다른 테스트와 마찬가지로 동물요청을 확인
        with(animalsRequest) {
            assertThat(method).isEqualTo("GET")
            assertThat(path).isEqualTo(animalsEndpointPath)
            assertThat(getHeader(ApiParameters.AUTH_HEADER))
                .isEqualTo(ApiParameters.TOKEN_TYPE + validToken)
        }
    }

    private fun getDispatcherForValidToken() = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                animalsEndpointPath -> MockResponse().setResponseCode(200)
                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    private fun getDispatcherForExpiredToken() = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                authEndpointPath -> {
                    MockResponse().setResponseCode(200)
                        .setBody(JsonReader.getJson("networkresponses/validToken.json"))
                }

                animalsEndpointPath -> MockResponse().setResponseCode(200)
                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
}