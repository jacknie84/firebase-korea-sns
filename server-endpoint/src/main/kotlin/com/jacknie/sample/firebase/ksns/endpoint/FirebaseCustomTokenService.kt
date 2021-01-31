package com.jacknie.sample.firebase.ksns.endpoint

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient

interface FirebaseCustomTokenService {

    fun getFirebaseCustomToken(client: OAuth2AuthorizedClient): FirebaseCustomToken
}
