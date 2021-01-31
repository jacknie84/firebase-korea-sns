package com.jacknie.sample.firebase.ksns.config

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import java.time.Instant
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException

class FirebaseJwtDecoder(firebaseApp: FirebaseApp) : JwtDecoder {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance(firebaseApp) }

    override fun decode(token: String): Jwt {
        val firebaseToken = try {
            firebaseAuth.verifyIdToken(token, true)
        } catch (e: FirebaseAuthException) {
            throw JwtException("${e.message}(errorCode: ${e.errorCode}, authErrorCode: ${e.authErrorCode})", e)
        }
        val iat = firebaseToken.claims["iat"]?.toString()?.toLongOrNull()
        val exp = firebaseToken.claims["exp"]?.toString()?.toLongOrNull()
        val issuedAt = Instant.ofEpochSecond(iat!!)
        val expiresAt = Instant.ofEpochSecond(exp!!)
        return Jwt(token, issuedAt, expiresAt, emptyMap(), firebaseToken.claims)
    }
}
