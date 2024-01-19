package cn.llonvne.kvision.service

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.repository.CodeRepository
import cn.llonvne.database.repository.LanguageRepository
import cn.llonvne.entity.problem.Code
import cn.llonvne.security.AuthenticationToken
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class CodeService(
    private val codeRepository: CodeRepository,
    private val languageRepository: LanguageRepository,
    private val authenticationUserRepository: AuthenticationUserRepository
) : ICodeService {
    override suspend fun saveCode(
        token: AuthenticationToken?,
        saveCodeReq: ICodeService.SaveCodeReq
    ): ICodeService.SaveCodeResp {
        return if (token == null) {
            PermissionDenied
        } else {
            if (saveCodeReq.languageId != null) {
                if (!languageRepository.isIdExist(saveCodeReq.languageId)) {
                    return LanguageNotFound
                }
            }

            ICodeService.SaveCodeResp.SuccessfulSaveCode(codeRepository.save(saveCodeReq.toCode(token)))
        }
    }

    override suspend fun getCode(value: AuthenticationToken?, shareId: Int): ICodeService.GetCodeResp {
        val code = codeRepository.get(shareId) ?: return ICodeService.GetCodeResp.CodeNotFound

        return ICodeService.GetCodeResp.SuccessfulGetCode(
            CodeDto(
                rawCode = code.code,
                language = languageRepository.getByIdOrNull(code.languageId),
                shareUserId = code.authenticationUserId,
                shareUsername = authenticationUserRepository.getByIdOrNull(code.authenticationUserId)?.username
                    ?: "未知",
                visibilityType = code.visibilityType
            )
        )
    }

    private fun ICodeService.SaveCodeReq.toCode(token: AuthenticationToken): Code {
        return Code(
            authenticationUserId = token.authenticationUserId,
            code = code,
            languageId = languageId
        )
    }
}