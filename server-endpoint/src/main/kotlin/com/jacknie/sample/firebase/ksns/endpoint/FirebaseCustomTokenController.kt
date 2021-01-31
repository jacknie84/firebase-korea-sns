package com.jacknie.sample.firebase.ksns.endpoint

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/firebase/{clientRegistrationId}/custom-token")
class FirebaseCustomTokenController(
    private val authorizedClientRepository: OAuth2AuthorizedClientRepository,
    private val firebaseCustomTokenService: FirebaseCustomTokenService,
) {

    @PostMapping
    fun getCustomToken(@PathVariable("clientRegistrationId") clientRegistrationId: String,
                       @RequestParam("code") code: String,
                       request: HttpServletRequest,
                       response: HttpServletResponse): ResponseEntity<Any> {
        if (!listOf("kakao", "naver").contains(clientRegistrationId)) {
            return ResponseEntity.notFound().build()
        }
        val client = authorizedClientRepository
            .loadAuthorizedClient<OAuth2AuthorizedClient>(clientRegistrationId, null, request)
            ?: return ResponseEntity.badRequest().body("could not found authorized client by client registration id: $clientRegistrationId")
        if (client.accessToken.tokenValue != code) {
            return ResponseEntity.badRequest().body("invalid authorization code: $code")
        }
        val customToken = firebaseCustomTokenService.getFirebaseCustomToken(client)
        authorizedClientRepository.removeAuthorizedClient(clientRegistrationId, null, request, response)
        return ResponseEntity.ok(customToken)
    }
}
