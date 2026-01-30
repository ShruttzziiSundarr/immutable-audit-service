package com.fin.shadow_ledger.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Value("\${security.api-key}")
    private lateinit var validApiKey: String

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/", "/index.html", "/pay.html", "/dashboard.html", "/*.css", "/*.js").permitAll()
                it.requestMatchers("/h2-console/**").permitAll()
                it.requestMatchers("/api/v1/auth/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(ApiKeyFilter(validApiKey), UsernamePasswordAuthenticationFilter::class.java)
            .headers { it.frameOptions { frame -> frame.disable() } }

        return http.build()
    }
}

class ApiKeyFilter(private val validKey: String) : OncePerRequestFilter() {
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val path = req.requestURI

        // Allow public paths
        if (path == "/" ||
            path.endsWith(".html") ||
            path.endsWith(".css") ||
            path.endsWith(".js") ||
            path.contains("/auth") ||
            path.contains("/h2-console")) {
            chain.doFilter(req, res)
            return
        }

        val requestKey = req.getHeader("X-API-KEY")
        if (requestKey == null || requestKey != validKey) {
            res.status = 401
            res.contentType = "application/json"
            res.writer.write("""{"error": "Unauthorized", "message": "Invalid or missing API Key"}""")
            return
        }
        chain.doFilter(req, res)
    }
}
