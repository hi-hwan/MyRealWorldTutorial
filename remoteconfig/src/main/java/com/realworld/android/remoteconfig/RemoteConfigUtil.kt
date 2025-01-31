package com.realworld.android.remoteconfig

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object RemoteConfigUtil {
    private const val SECRET_IMAGE_URL = "secret_image_url"

    // 다양한 Remote Config 키에 대한 기본값
    private val DEFAULTS: HashMap<String, Any> = hashMapOf(
        SECRET_IMAGE_URL to "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg"
    )

    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun init(debug: Boolean = false) {
        remoteConfig = getFirebaseRemoteConfig(debug)
    }

    private fun getFirebaseRemoteConfig(debug: Boolean): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            // Cache 간격 지정
            minimumFetchIntervalInSeconds = if (debug) 0 else 60 * 60
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        // Firebase Remote Config 대시보드에서 새 값을 설정할 때까지 기본값 사용
        remoteConfig.setDefaultsAsync(DEFAULTS)
        remoteConfig.fetchAndActivate()

        return remoteConfig
    }

    fun getSecretImageUrl() = remoteConfig.getString(SECRET_IMAGE_URL)
}