package cn.llonvne.kvision.service

import cn.llonvne.dtos.CodeDto
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.entity.problem.Code
import cn.llonvne.entity.problem.CodeVisibilityType
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
    }

    suspend fun getCode(value: AuthenticationToken?, shareId: Int): GetCodeResp

    @Serializable
    data class CommitOnCodeReq(
        val token: AuthenticationToken?,
        val content: String,
        val codeId: Int,
    )

    @Serializable
    sealed interface CommitOnCodeResp {
        @Serializable
        data class SuccessfulCommit(val shareCodeCommitDto: CreateCommentDto) : CommitOnCodeResp
    }

    suspend fun commit(commitOnCodeReq: CommitOnCodeReq): CommitOnCodeResp

    @Serializable
    sealed interface GetCommitsOnCodeResp {
        @Serializable
        data class SuccessfulGetCommits(val commits: List<CreateCommentDto>) : GetCommitsOnCodeResp
    }

    @Serializable
    data object CodeNotFound : GetCodeResp, GetCommitsOnCodeResp

    suspend fun getComments(authenticationToken: AuthenticationToken?, sharCodeId: Int): GetCommitsOnCodeResp
}






