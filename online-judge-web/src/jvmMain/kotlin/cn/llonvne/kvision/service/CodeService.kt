package cn.llonvne.kvision.service

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.repository.CodeRepository
import cn.llonvne.database.repository.LanguageRepository
import cn.llonvne.dtos.CodeDto
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.entity.problem.Code
import cn.llonvne.entity.problem.ShareCodeComment
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
        token: AuthenticationToken?, saveCodeReq: ICodeService.SaveCodeReq
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
        val code = codeRepository.get(shareId) ?: return ICodeService.CodeNotFound

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
            authenticationUserId = token.authenticationUserId, code = code, languageId = languageId
        )
    }

    override suspend fun commit(commitOnCodeReq: ICodeService.CommitOnCodeReq): ICodeService.CommitOnCodeResp {

        if (commitOnCodeReq.token == null) {
            return PermissionDenied
        }

        val result = codeRepository.comment(
            ShareCodeComment(
                committerAuthenticationUserId = commitOnCodeReq.token.authenticationUserId,
                content = commitOnCodeReq.content,
                shareCodeId = commitOnCodeReq.codeId,
                type = commitOnCodeReq.type
            )
        )

        if (result.commentId == null) {
            return InternalError("CommitId 在插入后仍不存在...")
        }

        if (result.createdAt == null) {
            return InternalError("ShareCodeComment.CreateAt 在插入后仍不存在")
        }

        return ICodeService.CommitOnCodeResp.SuccessfulCommit(
            CreateCommentDto(
                result.commentId,
                commitOnCodeReq.token.username,
                commitOnCodeReq.codeId,
                commitOnCodeReq.content,
                createdAt = result.createdAt,
                visibilityType = result.type
            )
        )
    }

    override suspend fun getComments(
        authenticationToken: AuthenticationToken?, sharCodeId: Int
    ): ICodeService.GetCommitsOnCodeResp {
        if (!codeRepository.isIdExist(sharCodeId)) {
            return ICodeService.CodeNotFound
        } else {
            return codeRepository.getComments(sharCodeId).mapNotNull {

                val committerUsername = authenticationUserRepository.getByIdOrNull(it.committerAuthenticationUserId)
                    ?: return@mapNotNull null

                val sharCodeOwnerId = codeRepository.getCodeOwnerId(sharCodeId)



                if (it.type == ShareCodeComment.Companion.ShareCodeCommentType.Private) {
                    if (authenticationToken == null) {
                        return@mapNotNull null
                    } else if (!(it.committerAuthenticationUserId == authenticationToken.authenticationUserId || authenticationToken.authenticationUserId == sharCodeOwnerId)) {
                        return@mapNotNull null
                    }
                }

                CreateCommentDto(
                    it.commentId ?: return@mapNotNull null,
                    committerUsername.username,
                    sharCodeId,
                    it.content,
                    createdAt = it.createdAt ?: return@mapNotNull null,
                    visibilityType = it.type
                )
            }.let {
                ICodeService.GetCommitsOnCodeResp.SuccessfulGetCommits(it)
            }
        }
    }

    override suspend fun deleteComments(commentIds: List<Int>): List<Int> {
        return codeRepository.deleteComment(commentIds).mapNotNull {
            it.commentId
        }
    }

}