package cn.llonvne.kvision.service

import cn.llonvne.database.repository.AuthenticationUserRepository
import cn.llonvne.database.repository.CodeRepository
import cn.llonvne.database.repository.LanguageRepository
import cn.llonvne.dtos.CodeDto
import cn.llonvne.dtos.CreateCommentDto
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.security.AuthenticationToken
import com.benasher44.uuid.uuid4
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

    enum class GetCodeId {
        HashLink, Id
    }

    private suspend fun getCodeSafetyCheck(
        getCodeId: GetCodeId,
        code: Code,
        value: AuthenticationToken?
    ): ICodeService.GetCodeResp {
        when (code.visibilityType) {
            CodeVisibilityType.Public -> {

            }

            CodeVisibilityType.Private -> {
                if (value == null) {
                    return PermissionDenied
                } else if (value.authenticationUserId != codeRepository.getCodeOwnerId(
                        code.codeId ?: return CodeNotFound
                    )
                ) {
                    return PermissionDenied
                }
            }

            CodeVisibilityType.Restrict -> {
                if (getCodeId == GetCodeId.HashLink) {

                } else if (value == null) {
                    return PermissionDenied
                } else if (value.authenticationUserId == codeRepository.getCodeOwnerId(
                        code.codeId ?: return CodeNotFound
                    )
                ) {

                }
            }
        }

        return ICodeService.GetCodeResp.SuccessfulGetCode(
            CodeDto(
                codeId = code.codeId ?: return CodeNotFound,
                rawCode = code.code,
                language = languageRepository.getByIdOrNull(code.languageId),
                shareUserId = code.authenticationUserId,
                shareUsername = authenticationUserRepository.getByIdOrNull(code.authenticationUserId)?.username
                    ?: "未知",
                visibilityType = code.visibilityType,
                commentType = code.commentType,
                hashLink = code.hashLink
            )
        )
    }

    override suspend fun getCode(value: AuthenticationToken?, shareId: Int): ICodeService.GetCodeResp {
        val code = codeRepository.get(shareId) ?: return CodeNotFound
        return getCodeSafetyCheck(GetCodeId.Id, code, value)
    }

    override suspend fun getCodeByHash(value: AuthenticationToken?, hash: String): ICodeService.GetCodeResp {
        val code = codeRepository.getCodeByHash(hash) ?: return CodeNotFound
        return getCodeSafetyCheck(GetCodeId.HashLink, code, value)
    }

    override suspend fun setCodeCommentType(
        token: AuthenticationToken?,
        shareId: Int,
        type: CodeCommentType
    ): ICodeService.SetCodeCommentTypeResp {

        if (token == null) {
            return PermissionDenied
        }
        if (!codeRepository.isIdExist(shareId)) {
            return CodeNotFound
        }
        if (token.authenticationUserId != codeRepository.getCodeOwnerId(shareId)) {
            return PermissionDenied
        }
        if (codeRepository.setCodeCommentType(shareId, type) != 1L) {
            return CodeNotFound
        }
        return ICodeService.SetCodeCommentTypeResp.SuccessSetCommentType
    }

    override suspend fun setCodeCommentVisibilityType(
        token: AuthenticationToken?,
        shareId: Int,
        commentId: Int,
        type: ShareCodeComment.Companion.ShareCodeCommentType
    ): ICodeService.SetCodeCommentVisibilityTypeResp {

        if (token == null) {
            return PermissionDenied
        } else if (token.authenticationUserId != codeRepository.getCodeOwnerId(shareId)) {
            return PermissionDenied
        }

        if (!codeRepository.isCommentIdExist(commentId)) {
            return CommentNotFound
        }

        codeRepository.setShareCodeCommentVisibilityType(commentId, type)
        return ICodeService.SetCodeCommentVisibilityTypeResp.SuccessSetCodeCommentVisibilityType
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
            return CodeNotFound
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

    override suspend fun setCodeVisibility(
        token: AuthenticationToken?,
        shareId: Int,
        result: CodeVisibilityType
    ): ICodeService.SetCodeVisibilityResp {
        if (token == null) {
            return PermissionDenied
        }

        val codeOwnerId = codeRepository.getCodeOwnerId(shareId) ?: return CodeNotFound

        if (codeOwnerId != token.authenticationUserId) {
            return PermissionDenied
        }

        val changeLine = codeRepository.setCodeVisibility(shareId, result)
        if (changeLine != 1L) {
            return CodeNotFound
        }
        return when (result) {
            CodeVisibilityType.Public -> onNotRestrictType(shareId)
            CodeVisibilityType.Private -> onNotRestrictType(shareId)
            CodeVisibilityType.Restrict -> onRestrictType(shareId)
        }
    }

    private suspend fun onNotRestrictType(shareId: Int): ICodeService.SetCodeVisibilityResp.SuccessToPublicOrPrivate {
        codeRepository.setHashLink(shareId = shareId, null)
        return ICodeService.SetCodeVisibilityResp.SuccessToPublicOrPrivate
    }

    private suspend fun onRestrictType(shareId: Int): ICodeService.SetCodeVisibilityResp.SuccessToRestrict {
        val hashLink = uuid4().toString()
        codeRepository.setHashLink(shareId = shareId, hashLink)
        return ICodeService.SetCodeVisibilityResp.SuccessToRestrict(hashLink)
    }
}