package cn.llonvne.model

import cn.llonvne.entity.problem.CodeVisibilityType
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.kvision.service.ICodeService
import io.kvision.remote.getService


object CodeModel {

    private val codeService = getService<ICodeService>()

    suspend fun saveCode(
        rawCode: String,
        languageId: Int?
    ): ICodeService.SaveCodeResp = codeService.saveCode(
        AuthenticationModel.userToken.value,
        ICodeService.SaveCodeReq(rawCode, languageId = languageId, CodeVisibilityType.Public)
    )

    suspend fun getCode(shareId: Int) = codeService.getCode(
        AuthenticationModel.userToken.value, shareId
    )

    suspend fun commit(sharCodeId: Int, content: String, type: ShareCodeComment.Companion.ShareCodeCommentType) =
        codeService.commit(ICodeService.CommitOnCodeReq(AuthenticationModel.userToken.value, content, sharCodeId,type))

    suspend fun getCommentByCodeId(sharCodeId: Int) =
        codeService.getComments(AuthenticationModel.userToken.value, sharCodeId)

    suspend fun deleteCommentByIds(commentIds: List<Int>) = codeService.deleteComments(commentIds)
}