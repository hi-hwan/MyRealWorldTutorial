package com.realworld.android.petsave.common.data.api

class Authenticator {

    fun sign(data: ByteArray): ByteArray {
        return byteArrayOf()
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
        return false
    }

    fun publicKey(): String {
        return "replace_me"
    }
}