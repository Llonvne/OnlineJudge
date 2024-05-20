package cn.llonvne.kvision.service

import cn.llonvne.dtos.CodeDto
import cn.llonvne.dtos.CreateCommentReq
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.security.Token
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

    suspend fun save(token: Token?, saveCodeReq: SaveCodeReq): SaveCodeResp

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

    suspend fun getCode(value: Token?, shareId: Int): GetCodeResp

    @Serializable
    data class CommitOnCodeReq(
        val token: Token?,
        val content: String,
        val codeId: Int,
        val type: ShareCodeCommentType
    )

    @Serializable
    sealed interface CommitOnCodeResp {
        @Serializable
        data class SuccessfulCommit(val shareCodeCommitDto: CreateCommentReq) : CommitOnCodeResp
    }

    suspend fun commit(commitOnCodeReq: CommitOnCodeReq): CommitOnCodeResp

    @Serializable
    sealed interface GetCommitsOnCodeResp {
        @Serializable
        data class SuccessfulGetCommits(val commits: List<CreateCommentReq>) : GetCommitsOnCodeResp
    }


    suspend fun getComments(token: Token?, sharCodeId: Int): GetCommitsOnCodeResp

    suspend fun deleteComments(commentIds: List<Int>): List<Int>

    @Serializable
    sealed interface SetCodeVisibilityResp {
        @Serializable
        data object SuccessToPublicOrPrivate : SetCodeVisibilityResp

        @Serializable
        data class SuccessToRestrict(val link: String) : SetCodeVisibilityResp
    }

    suspend fun setCodeVisibility(
        token: Token?,
        shareId: Int,
        result: CodeVisibilityType
    ): SetCodeVisibilityResp

    suspend fun getCodeByHash(value: Token?, hash: String): GetCodeResp

    @Serializable
    sealed interface SetCodeCommentTypeResp {
        @Serializable
        data object SuccessSetCommentType : SetCodeCommentTypeResp
    }

    suspend fun setCodeCommentType(
        token: Token?,
        shareId: Int,
        type: CodeCommentType
    ): SetCodeCommentTypeResp

    @Serializable
    sealed interface SetCodeCommentVisibilityTypeResp {
        @Serializable
        data object SuccessSetCodeCommentVisibilityType : SetCodeCommentVisibilityTypeResp
    }

    suspend fun setCodeCommentVisibilityType(
        token: Token?,
        shareId: Int,
        commentId: Int,
        type: ShareCodeCommentType
    ): SetCodeCommentVisibilityTypeResp
}






