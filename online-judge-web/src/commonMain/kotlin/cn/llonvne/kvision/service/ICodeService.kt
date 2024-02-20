package cn.llonvne.kvision.service

import cn.llonvne.dtos.CodeDto
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.CodeVisibilityType
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

        fun onSuccess(block: (SuccessfulGetCode) -> Unit): GetCodeResp {
            if (this is SuccessfulGetCode) {
                block(this)
            }
            return this
        }

        fun onFailure(block: (GetCodeResp) -> Unit): GetCodeResp {
            if (this !is SuccessfulGetCode) {
                block(this)
            }
            return this
        }
    }

    suspend fun getCode(value: AuthenticationToken?, shareId: Int): GetCodeResp

    @Serializable
    data class CommitOnCodeReq(
        val token: AuthenticationToken?,
        val content: String,
        val codeId: Int,
        val type: ShareCodeCommentType
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


    suspend fun getComments(authenticationToken: AuthenticationToken?, sharCodeId: Int): GetCommitsOnCodeResp

    suspend fun deleteComments(commentIds: List<Int>): List<Int>

    @Serializable
    sealed interface SetCodeVisibilityResp {
        @Serializable
        data object SuccessToPublicOrPrivate : SetCodeVisibilityResp

        @Serializable
        data class SuccessToRestrict(val link: String) : SetCodeVisibilityResp
    }

    suspend fun setCodeVisibility(
        token: AuthenticationToken?,
        shareId: Int,
        result: CodeVisibilityType
    ): SetCodeVisibilityResp

    suspend fun getCodeByHash(value: AuthenticationToken?, hash: String): GetCodeResp

    @Serializable
    sealed interface SetCodeCommentTypeResp {
        @Serializable
        data object SuccessSetCommentType : SetCodeCommentTypeResp
    }

    suspend fun setCodeCommentType(
        token: AuthenticationToken?,
        shareId: Int,
        type: CodeCommentType
    ): SetCodeCommentTypeResp

    @Serializable
    sealed interface SetCodeCommentVisibilityTypeResp {
        @Serializable
        data object SuccessSetCodeCommentVisibilityType : SetCodeCommentVisibilityTypeResp
    }

    suspend fun setCodeCommentVisibilityType(
        token: AuthenticationToken?,
        shareId: Int,
        commentId: Int,
        type: ShareCodeCommentType
    ): SetCodeCommentVisibilityTypeResp
}






