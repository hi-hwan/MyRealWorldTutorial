package com.realworld.android.petsave.common.data.api

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class ClientAuthenticator : Authenticator() {
    var serverPublicKeyString: String = ""
}
class ServerAuthenticator : Authenticator()

open class Authenticator {

    private val publicKey: PublicKey
    private val privateKey: PrivateKey

    init {
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        keyPairGenerator.initialize(256)
        val keyPair = keyPairGenerator.genKeyPair()

        publicKey = keyPair.public
        privateKey = keyPair.private
    }

    fun sign(data: ByteArray): ByteArray {
        // SHA-512, ECDSA
        val signature = Signature.getInstance("SHA512withECDSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    //You can use this method to test your sign method above
    /*
    fun verify(signature: ByteArray, data: ByteArray): Boolean {
      val verifySignature = Signature.getInstance("SHA512withECDSA")
      verifySignature.initVerify(publicKey)
      verifySignature.update(data)
      return verifySignature.verify(signature)
    }
     */

    fun verify(signature: ByteArray, data: ByteArray, publicKeyString: String): Boolean {
        val verifySignature = Signature.getInstance("SHA512withECDSA")
        // Base64는 네트워크를 통해 원시 데이터 바이트를 문자열로 전달할 수 있게 해주는 형식
        val bytes = Base64.decode(publicKeyString, Base64.NO_WRAP)
        val publicKey = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(bytes))
        verifySignature.initVerify(publicKey)
        verifySignature.update(data)
        return verifySignature.verify(signature)
    }

    fun publicKey(): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }
}