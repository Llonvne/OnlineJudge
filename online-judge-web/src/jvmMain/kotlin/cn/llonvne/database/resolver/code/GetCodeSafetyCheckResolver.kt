package cn.llonvne.database.resolver.code

import cn.llonvne.database.repository.CodeRepository
import cn.llonvne.database.resolver.code.GetCodeSafetyCheckResolver.GetCodeSafetyCheckResult
import cn.llonvne.database.resolver.code.GetCodeSafetyCheckResolver.GetCodeSafetyCheckResult.*
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.kvision.service.CodeNotFound
import cn.llonvne.kvision.service.CodeService
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import kotlinx.serialization.Serializable
import org.springframework.data.relational.core.sql.Not
import org.springframework.stereotype.Service

@Service
class GetCodeSafetyCheckResolver(
) {
    @Serializable
    sealed interface GetCodeSafetyCheckResult<out R> {
        data object PermissionDenied : GetCodeSafetyCheckResult<Nothing>

        data class GetCodeSafetyCheckPassed<R>(val result: R) : GetCodeSafetyCheckResult<R>
    }

    suspend fun <R> resolve(
        getCodeId: CodeService.GetCodeId,
        code: Code,
        value: AuthenticationToken?,
        onPass: () -> R
    ): GetCodeSafetyCheckResult<R> {
        TODO()
    }
}