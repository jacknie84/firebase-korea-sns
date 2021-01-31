package com.jacknie.sample.firebase.ksns.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException

class BasicAuthenticationProvider : AuthenticationProvider {

    @Value("\${app.client.id}")
    private lateinit var clientId: String

    @Value("\${app.client.secret}")
    private lateinit var clientSecret: String

    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication as UsernamePasswordAuthenticationToken
        val clientId = token.principal.toString()
        val clientSecret = token.credentials.toString()
        if (this.clientId != clientId) {
            throw UsernameNotFoundException("could not found client by client id: $clientId")
        }
        if (this.clientSecret != clientSecret) {
            throw BadCredentialsException("invalid client secret")
        }
        return UsernamePasswordAuthenticationToken(clientId, clientSecret, listOf(SimpleGrantedAuthority("ROLE_CLIENT")))
    }

    override fun supports(authentication: Class<*>): Boolean {
        return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
