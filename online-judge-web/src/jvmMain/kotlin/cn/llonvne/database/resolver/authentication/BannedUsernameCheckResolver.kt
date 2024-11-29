package cn.llonvne.database.resolver.authentication

import org.springframework.stereotype.Service

@Service
class BannedUsernameCheckResolver {
    private val bannedUsernameKeyWords = listOf("admin")

    enum class BannedUsernameCheckResult {
        Pass,
        Failed,
    }

    fun resolve(username: String): BannedUsernameCheckResult {
        bannedUsernameKeyWords.forEach { bannedKeyWord ->
            if (username.lowercase().contains(bannedKeyWord)) {
                return BannedUsernameCheckResult.Failed
            }
        }
        return BannedUsernameCheckResult.Pass
    }
}
