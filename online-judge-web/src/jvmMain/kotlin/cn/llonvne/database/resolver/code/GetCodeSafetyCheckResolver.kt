package cn.llonvne.database.resolver.code

import cn.llonvne.entity.problem.share.Code
import cn.llonvne.kvision.service.CodeService
import cn.llonvne.security.AuthenticationToken
import kotlinx.serialization.Serializable
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