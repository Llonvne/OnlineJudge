package cn.llonvne.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class BPasswordEncoder {
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    companion object {
        suspend operator fun <R> PasswordEncoder.invoke(
            block: suspend context(PasswordEncoder)
            () -> R,
        ): R = block(this)
    }
}
