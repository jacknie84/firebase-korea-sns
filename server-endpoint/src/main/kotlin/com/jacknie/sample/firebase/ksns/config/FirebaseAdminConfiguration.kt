package com.jacknie.sample.firebase.ksns.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class FirebaseAdminConfiguration {

    @Value("\${firebase.admin.service-account-key}")
    private lateinit var serviceAccountKey: Resource

    @Value("\${spring.application.name}")
    private lateinit var firebaseAppName: String

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun firebaseApp(): FirebaseApp {
        return try {
            val credentials = GoogleCredentials.fromStream(serviceAccountKey.inputStream)
            val options = FirebaseOptions.builder().setCredentials(credentials).build()
            FirebaseApp.initializeApp(options, firebaseAppName)
        } catch (e: IllegalStateException) {
            logger.info("an app with the same name($firebaseAppName) has already been initialized")
            FirebaseApp.getInstance(firebaseAppName)
        }
    }
}
