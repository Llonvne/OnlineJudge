package cn.llonvne.database.resolver

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.security.asJson
import cn.llonvne.security.check
import org.springframework.stereotype.Service

@Service
class GroupIdToUserResolver(private val authenticationUserRepository: AuthenticationUserRepository) {
    suspend fun resolve(need: TeamIdRole): List<AuthenticationUser> {
        val str = need.asJson
        val matchedUser = authenticationUserRepository.matchRoleStr(str)
            .filter { user -> user.check(need) }
        return matchedUser
    }
}