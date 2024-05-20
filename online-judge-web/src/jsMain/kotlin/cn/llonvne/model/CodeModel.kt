package cn.llonvne.model

import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.kvision.service.ICodeService
import cn.llonvne.kvision.service.ICodeService.*
import io.kvision.remote.getService


object CodeModel {

    private val codeService = getService<ICodeService>()

    suspend fun saveCode(
        rawCode: String,
        languageId: Int?
    ): SaveCodeResp = codeService.save(
        AuthenticationModel.userToken.value,
        SaveCodeReq(rawCode, languageId = languageId, CodeVisibilityType.Public)
    )

    suspend fun getCode(shareId: Int) = codeService.getCode(
        AuthenticationModel.userToken.value, shareId
    )

    suspend fun getCode(hash: String) = codeService.getCodeByHash(AuthenticationModel.userToken.value, hash)

    suspend fun commit(sharCodeId: Int, content: String, type: ShareCodeCommentType) =
        codeService.commit(CommitOnCodeReq(AuthenticationModel.userToken.value, content, sharCodeId, type))

    suspend fun getCommentByCodeId(sharCodeId: Int) =
        codeService.getComments(AuthenticationModel.userToken.value, sharCodeId)

    suspend fun deleteCommentByIds(commentIds: List<Int>) = codeService.deleteComments(commentIds)
    suspend fun setCodeVisibility(shareId: Int, result: CodeVisibilityType) =
        codeService.setCodeVisibility(AuthenticationModel.userToken.value, shareId, result)

    suspend fun setCodeCommentType(shareId: Int, type: CodeCommentType) =
        codeService.setCodeCommentType(AuthenticationModel.userToken.value, shareId, type)

    suspend fun setCodeCommentVisibilityType(
        shareId: Int,
        commentId: Int,
        type: ShareCodeCommentType
    ) =
        codeService.setCodeCommentVisibilityType(
            AuthenticationModel.userToken.value,
            shareId = shareId,
            commentId = commentId, type
        )
}