package cn.llonvne.kvision.service

import cn.llonvne.entity.problem.Code
import cn.llonvne.entity.problem.CodeVisibilityType
import cn.llonvne.entity.problem.Language
import cn.llonvne.security.AuthenticationToken
import io.kvision.annotations.KVService
import kotlinx.serialization.Serializable

@KVService
interface ICodeService {

    @Serializable
    data class SaveCodeReq(
        val code: String,
        val languageId: Int?,
        val visibilityType: CodeVisibilityType = CodeVisibilityType.Public,
    )

    @Serializable
    sealed interface SaveCodeResp {
        @Serializable
        data class SuccessfulSaveCode(val code: Code) : SaveCodeResp

    }

    suspend fun saveCode(token: AuthenticationToken?, saveCodeReq: SaveCodeReq): SaveCodeResp

    @Serializable
    sealed interface GetCodeResp {
        @Serializable
        data class SuccessfulGetCode(val codeDto: CodeDto) : GetCodeResp

        @Serializable
        data object CodeNotFound : GetCodeResp
    }

    suspend fun getCode(value: AuthenticationToken?, shareId: Int): GetCodeResp
}

@Serializable
data class CodeDto(
    val rawCode: String,
    val language: Language?,
    val shareUserId: Int,
    val shareUsername: String,
    val visibilityType: CodeVisibilityType,
)


