package cn.llonvne.model

import cn.llonvne.entity.problem.Code
import cn.llonvne.entity.problem.CodeVisibilityType
import cn.llonvne.entity.problem.Language
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

}