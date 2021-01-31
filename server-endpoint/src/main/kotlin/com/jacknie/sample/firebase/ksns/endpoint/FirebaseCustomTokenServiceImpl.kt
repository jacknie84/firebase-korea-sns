package com.jacknie.sample.firebase.ksns.endpoint

import com.fasterxml.jackson.databind.JsonNode
import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations
import org.springframework.web.client.UnknownContentTypeException
import org.springframework.web.client.exchange

@Service
class FirebaseCustomTokenServiceImpl(firebaseApp: FirebaseApp, restTemplateBuilder: RestTemplateBuilder) : FirebaseCustomTokenService {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance(firebaseApp) }
    private val restOperations: RestOperations by lazy { restTemplateBuilder.errorHandler(OAuth2ErrorResponseErrorHandler()).build() }
    private val requestEntityConverter = OAuth2UserRequestEntityConverter()

    override fun getFirebaseCustomToken(client: OAuth2AuthorizedClient): FirebaseCustomToken {
        val requestEntity = requestEntityConverter.convert(OAuth2UserRequest(client.clientRegistration, client.accessToken))!!
        val userInfo = getUserInfo(client, requestEntity)
        val userRecord = getFirebaseUserRecord(client, userInfo)
        val customToken = firebaseAuth.createCustomToken(userRecord.uid)
        return FirebaseCustomToken(userRecord.uid, customToken)
    }

    private fun getUserInfo(client: OAuth2AuthorizedClient, requestEntity: RequestEntity<*>): UserInfo {
        return when (client.clientRegistration.registrationId) {
            "kakao" -> getKakaoUserInfo(client, requestEntity)
            "naver" -> getNaverUserInfo(client, requestEntity)
            else -> throw unsupportedClientRegistration(client.clientRegistration.registrationId)
        }
    }

    private fun getKakaoUserInfo(client: OAuth2AuthorizedClient, requestEntity: RequestEntity<*>): UserInfo {
        val responseEntity = exchange(client, requestEntity)
        return UserInfo(
            clientRegistrationId = client.clientRegistration.registrationId,
            id = responseEntity.body?.get("id")?.asText()?: throw notFoundUserId(client.clientRegistration.registrationId),
            displayName = responseEntity.body?.at("/properties/nickname")?.textValue()?: throw notFoundUserDisplayName(),
            email = responseEntity.body?.at("/kakao_account/email")?.textValue()?: throw notFoundUserEmail(),
            emailVerified = responseEntity.body?.at("/kakao_account/is_email_verified")?.booleanValue()?: false,
        )
    }

    private fun getNaverUserInfo(client: OAuth2AuthorizedClient, requestEntity: RequestEntity<*>): UserInfo {
        val responseEntity = exchange(client, requestEntity)
        return UserInfo(
            clientRegistrationId = client.clientRegistration.registrationId,
            id = responseEntity.body?.at("/response/id")?.asText()?: throw notFoundUserId(client.clientRegistration.registrationId),
            displayName = responseEntity.body?.at("/response/name")?.textValue()?: throw notFoundUserDisplayName(),
            email = responseEntity.body?.at("/response/email")?.textValue()?: throw notFoundUserEmail(),
            emailVerified = true,
        )
    }

    private fun exchange(client: OAuth2AuthorizedClient, requestEntity: RequestEntity<*>): ResponseEntity<JsonNode> {
        return try {
            restOperations.exchange(requestEntity)
        } catch (e: OAuth2AuthorizationException) {
            val userInfoUri = client.clientRegistration.providerDetails.userInfoEndpoint.uri
            val errorDescription = e.error.description?.let { ", Error Description: ${e.error.description}" }
            val errorDetails = "Error details: [UserInfo Uri: $userInfoUri, Error Code: ${e.error.errorCode}$errorDescription]"
            val description = "An error occurred while attempting to retrieve the UserInfo Resource: $errorDetails"
            val error = OAuth2Error("invalid_user_info_response", description, null)
            throw OAuth2AuthenticationException(error, error.toString(), e)
        } catch (e: UnknownContentTypeException) {
            val userInfoUri = client.clientRegistration.providerDetails.userInfoEndpoint.uri
            val description = """
                |An error occurred while attempting to retrieve the UserInfo Resource from
                |'$userInfoUri': response contains invalid content type '${e.contentType}'.
                |The UserInfo Response should return a JSON object (content type 'application/json')
                |that contains a collection of name and value pairs of the claims about the authenticated End-User.
                |Please ensure the UserInfo Uri in UserInfoEndpoint for Client Registration
                |'${client.clientRegistration.registrationId}' conforms to the UserInfo Endpoint,
                |as defined in OpenID Connect 1.0: 'https://openid.net/specs/openid-connect-core-1_0.html#UserInfo'
            """.trimMargin()
            val error = OAuth2Error("invalid_user_info_response", description, null)
            throw OAuth2AuthenticationException(error, error.toString(), e)
        } catch (e: RestClientException) {
            val description = "An error occurred while attempting to retrieve the UserInfo Resource: ${e.message}"
            val error = OAuth2Error("invalid_user_info_response", description, null)
            throw OAuth2AuthenticationException(error, error.toString(), e)
        }
    }

    private fun notFoundUserId(clientRegistrationId: String): OAuth2AuthenticationException {
        val description = "could not found user id in response(registrationId: $clientRegistrationId)"
        val error = OAuth2Error("not_found_user_id", description, null)
        return OAuth2AuthenticationException(error, error.toString())
    }

    private fun notFoundUserEmail(): OAuth2AuthenticationException {
        val description = "could not found user email"
        val error = OAuth2Error("not_found_user_email", description, null)
        return OAuth2AuthenticationException(error, error.toString())
    }

    private fun notFoundUserDisplayName(): OAuth2AuthenticationException {
        val description = "could not found user display name"
        val error = OAuth2Error("not_found_user_display_name", description, null)
        return OAuth2AuthenticationException(error, error.toString())
    }

    private fun unsupportedClientRegistration(clientRegistrationId: String): OAuth2AuthenticationException {
        val description = "unsupported client registration(registrationId: $clientRegistrationId)"
        val error = OAuth2Error("unsupported_client_registration", description, null)
        return OAuth2AuthenticationException(error, error.toString())
    }

    private fun getFirebaseUserRecord(client: OAuth2AuthorizedClient, userInfo: UserInfo): UserRecord {
        return try {
            firebaseAuth.getUserByEmail(userInfo.email)
        } catch (e: FirebaseAuthException) {
            when(e.errorCode) {
                ErrorCode.NOT_FOUND -> createFirebaseUser(client, userInfo)
                else -> throw firebaseAuthError(e)
            }
        }
    }

    private fun createFirebaseUser(client: OAuth2AuthorizedClient, userInfo: UserInfo): UserRecord {
        val customClaims = mapOf("vendor" to client.clientRegistration.registrationId, "vendorUserId" to userInfo.id)
        val createRequest = UserRecord.CreateRequest()
            .setEmail(userInfo.email)
            .setEmailVerified(userInfo.emailVerified)
            .setDisabled(false)
            .setDisplayName(userInfo.displayName)
        val userRecord = firebaseAuth.createUser(createRequest)
        val updateRequest = userRecord.updateRequest().setCustomClaims(customClaims)
        return firebaseAuth.updateUser(updateRequest)
    }

    private fun firebaseAuthError(e: FirebaseAuthException): OAuth2AuthenticationException {
        val error = OAuth2Error(e.errorCode.toString(), e.message, null)
        return OAuth2AuthenticationException(error, error.toString(), e)
    }

    data class UserInfo(

        var clientRegistrationId: String,

        var id: String,

        var email: String,

        var displayName: String,

        var emailVerified: Boolean,

    )
}
