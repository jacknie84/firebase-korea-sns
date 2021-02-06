package com.jacknie.sample.firebase.ksns.config

import com.google.firebase.FirebaseApp
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
class WebSecurityConfiguration1 : WebSecurityConfigurerAdapter(true) {

    override fun configure(http: HttpSecurity) {
        http
            .oauth2Client().and()
            .requestMatchers().antMatchers("/oauth2/authorization/**", "/login/oauth2/code/**")
    }

}

@Configuration
@Order(1)
class WebSecurityConfiguration2 : WebSecurityConfigurerAdapter(true) {

    override fun configure(http: HttpSecurity) {
        http
            .authenticationProvider(basicAuthenticationProvider())
            .cors().and()
            .sessionManagement().and()
            .securityContext().and()
            .httpBasic().and()
            .requestMatchers().antMatchers("/firebase/*/custom-token").and()
            .authorizeRequests().antMatchers("/firebase/*/custom-token").authenticated()
    }

    @Bean
    fun basicAuthenticationProvider() = BasicAuthenticationProvider()

}

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
class WebSecurityConfiguration3(private val firebaseApp: FirebaseApp) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .headers().frameOptions().sameOrigin().and()
            .oauth2ResourceServer().jwt().decoder(FirebaseJwtDecoder(firebaseApp)).and().and()
            .requestMatchers().antMatchers("/resources").and()
            .authorizeRequests().anyRequest().authenticated()
    }

}

@Configuration
class CorsConfiguration {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfigurationSource = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.addAllowedOrigin("http://localhost:3000")
        config.addAllowedMethod("*")
        config.addAllowedHeader("*")
        config.allowCredentials = true
        corsConfigurationSource.registerCorsConfiguration("/**", config)
        return corsConfigurationSource
    }
}
