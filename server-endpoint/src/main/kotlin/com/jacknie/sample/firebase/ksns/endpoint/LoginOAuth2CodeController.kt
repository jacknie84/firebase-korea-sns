package com.jacknie.sample.firebase.ksns.endpoint

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.util.UriComponentsBuilder

@Controller
@RequestMapping("/login/oauth2/code/{clientRegistrationId}")
class LoginOAuth2CodeController(
    private val authorizedClientRepository: OAuth2AuthorizedClientRepository,
) {

    @Value("\${app.client.redirectUri}")
    private lateinit var redirectUri: String

    @RequestMapping
    fun code(@PathVariable("clientRegistrationId") clientRegistrationId: String, request: HttpServletRequest, response: HttpServletResponse): String {
        val client = authorizedClientRepository.loadAuthorizedClient<OAuth2AuthorizedClient>(clientRegistrationId, null, request)
        val code = client.accessToken.tokenValue
        val uriString = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("code", code)
            .queryParam("clientRegistrationId", clientRegistrationId)
            .toUriString()
        return "redirect:$uriString"
    }
}
