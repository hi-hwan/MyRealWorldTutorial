package com.realworld.android.petsave.common.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.provider.Settings.Secure
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.util.HashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Encryption {
    companion object {

        private const val KEYSTORE_ALIAS = "PetSaveLoginKey"
        private const val PROVIDER = "AndroidKeyStore"

        @TargetApi(Build.VERSION_CODES.R)
        fun generateSecretKey() {
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true) // 잠금화면 설정하도록 요구
                .setUserAuthenticationParameters(
                    120,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                ) // 인증 후 120초 동안 키를 사용할 수 있도록 설정
                .build()
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, PROVIDER
            )
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        fun createLoginPassword(context: Context): ByteArray {
            val cipher = getCipher()
            val secretKey = getSecretKey()
            val random = SecureRandom()
            val passwordBytes = ByteArray(256)
            random.nextBytes(passwordBytes)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val ivParameters = cipher.parameters.getParameterSpec(GCMParameterSpec::class.java)
            val iv = ivParameters.iv
            PreferencesHelper.saveIV(context, iv)
            return cipher.doFinal(passwordBytes)
        }

        fun decryptPassword(context: Context, password: ByteArray): ByteArray {
            val cipher = getCipher()
            val secretKey = getSecretKey()
            val iv = PreferencesHelper.iv(context)
            val ivParameters = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameters)
            return cipher.doFinal(password)
        }

        fun encryptFile(context: Context, file: File): EncryptedFile {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
        }

        fun encrypt(
            dataToEncrypt: ByteArray,
            password: CharArray
        ): HashMap<String, ByteArray> {
            val map = HashMap<String, ByteArray>()
            val random = SecureRandom()
            val salt = ByteArray(256)
            random.nextBytes(salt)

            // Password-Based Key Derivation Function 2 (PBKDF2)
            // 암호화 작업을 위해 비밀번호 기반 객체
            // 반복 횟수(1324)가 높을수록 브루트 포스 공격 시 키를 처리하는 시간이 더 오래 걸린다.
            val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")

            // 초기화 벡터 추가
            val ivRandom = SecureRandom()
            val iv = ByteArray(16)
            ivRandom.nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            // Advanced Encryption Standard (AES)
            // Cipher Block Chaining (CBC)
            // Public-Key Cryptography Standard7 (PKCS7)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(dataToEncrypt)

            // 일반적으로 암호문 앞에 IV를 붙여서 저장하고,
            // 복호화 시 이를 잘라내어 사용하는 방식이 일반적.
            // 학습 목적에서는 배열 처리나 오프셋 계산을 피하기 위해 맵을 사용.
            // 이를 재사용하거나 순차적으로 증가시키는 것은 보안을 약화시킨다.
            // 절대로 키를 저장해서는 안된다.
            map["salt"] = salt
            map["iv"] = iv
            map["encrypted"] = encrypted
            return map
        }

        fun decrypt(map: HashMap<String, ByteArray>, password: CharArray): ByteArray? {
            var decrypted: ByteArray? = null
            try {
                val salt = map["salt"]
                val iv = map["iv"]
                val encrypted = map["encrypted"]

                val pbKeySpec = PBEKeySpec(password, salt, 1324, 256)
                val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
                val keySpec = SecretKeySpec(keyBytes, "AES")

                val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                val ivSpec = IvParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                decrypted = cipher.doFinal(encrypted)
            } catch (e: Exception) {
                Log.e("MYAPP", "decryption exception", e)
            }

            return decrypted
        }

        //NOTE: Here's a keystore version of the encryption for your reference :]
        private fun keystoreEncrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray> {
            val map = HashMap<String, ByteArray>()
            try {

                //Get the key
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val secretKeyEntry =
                    keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
                val secretKey = secretKeyEntry.secretKey

                //Encrypt data
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val ivBytes = cipher.iv
                val encryptedBytes = cipher.doFinal(dataToEncrypt)

                map["iv"] = ivBytes
                map["encrypted"] = encryptedBytes
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            return map
        }

        private fun keystoreDecrypt(map: HashMap<String, ByteArray>): ByteArray? {
            var decrypted: ByteArray? = null
            try {

                //Get the key
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)

                val secretKeyEntry =
                    keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
                val secretKey = secretKeyEntry.secretKey

                //Extract info from map
                val encryptedBytes = map["encrypted"]
                val ivBytes = map["iv"]

                //Decrypt data
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, ivBytes)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                decrypted = cipher.doFinal(encryptedBytes)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

            return decrypted
        }

        fun keystoreTest() {

            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                "MyKeyAlias",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                //.setUserAuthenticationRequired(true) // requires lock screen, invalidated if lock screen is disabled
                //.setUserAuthenticationValidityDurationSeconds(120) // only available x seconds from password authentication. -1 requires finger print - every time
                .setRandomizedEncryptionRequired(true) // different ciphertext for same plaintext on each call
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()

            val map = keystoreEncrypt("My very sensitive string!".toByteArray(Charsets.UTF_8))
            val decryptedBytes = keystoreDecrypt(map)
            decryptedBytes?.let {
                val decryptedString = String(it, Charsets.UTF_8)
                Log.e("MyApp", "The decrypted string is: $decryptedString")
            }
        }

        private fun getSecretKey(): SecretKey {
            val keyStore = KeyStore.getInstance(PROVIDER)

            // 키 저장소에 액세스하려면 먼저 로드해야 한다.
            keyStore.load(null)
            return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        }

        private fun getCipher(): Cipher {
            return Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_GCM + "/"
                        + KeyProperties.ENCRYPTION_PADDING_NONE
            )
        }
    }
}