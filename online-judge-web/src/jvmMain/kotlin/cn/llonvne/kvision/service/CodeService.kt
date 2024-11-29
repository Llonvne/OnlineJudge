package cn.llonvne.kvision.service

import cn.llonvne.database.repository.CodeRepository
import cn.llonvne.database.repository.LanguageRepository
import cn.llonvne.database.repository.UserRepository
import cn.llonvne.dtos.CodeDto
import cn.llonvne.dtos.CreateCommentReq
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.CodeVisibilityType
import cn.llonvne.entity.problem.share.CodeVisibilityType.*
import cn.llonvne.kvision.service.ICodeService.*
import cn.llonvne.kvision.service.ICodeService.GetCodeResp.SuccessfulGetCode
import cn.llonvne.kvision.service.ICodeService.GetCommitsOnCodeResp.SuccessfulGetCommits
import cn.llonvne.kvision.service.ICodeService.SaveCodeResp.SuccessfulSaveCode
import cn.llonvne.kvision.service.ICodeService.SetCodeCommentTypeResp.SuccessSetCommentType
import cn.llonvne.kvision.service.ICodeService.SetCodeCommentVisibilityTypeResp.SuccessSetCodeCommentVisibilityType
import cn.llonvne.kvision.service.ICodeService.SetCodeVisibilityResp.SuccessToPublicOrPrivate
import cn.llonvne.kvision.service.ICodeService.SetCodeVisibilityResp.SuccessToRestrict
import cn.llonvne.security.Token
import cn.llonvne.security.UserLoginLogoutTokenValidator
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
    private val userRepository: UserRepository,
    private val authentication: UserLoginLogoutTokenValidator,
) : ICodeService {
    override suspend fun save(
        token: Token?,
        saveCodeReq: SaveCodeReq,
    ): SaveCodeResp {
        val user =
            authentication.validate(token) {
                requireLogin()
            } ?: return PermissionDenied
        if (saveCodeReq.languageId != null) {
            if (!languageRepository.isIdExist(saveCodeReq.languageId)) {
                return LanguageNotFound
            }
        }
        return SuccessfulSaveCode(codeRepository.save(saveCodeReq.toCode(user)))
    }

    enum class GetCodeId {
        HashLink,
        Id,
    }

    private suspend fun getCodeSafetyCheck(
        getCodeId: GetCodeId,
        code: Code,
        value: Token?,
    ): GetCodeResp {
        when (code.visibilityType) {
            Public -> {
            }

            Private -> {
                val user = authentication.getAuthenticationUser(value)

                if (user == null) {
                    return PermissionDenied
                } else if (user.id !=
                    codeRepository
                        .getCodeOwnerId(code.codeId ?: return CodeNotFound)
                ) {
                    return PermissionDenied
                }
            }

            Restrict -> {
                val user = authentication.getAuthenticationUser(value)

                when {
                    getCodeId == GetCodeId.HashLink -> {}
                    user == null -> {
                        return PermissionDenied
                    }

                    user.id == codeRepository.getCodeOwnerId(code.codeId ?: return CodeNotFound) -> {
                    }
                }
            }
        }

        return SuccessfulGetCode(
            CodeDto(
                codeId = code.codeId ?: return CodeNotFound,
                rawCode = code.code,
                language = languageRepository.getByIdOrNull(code.languageId),
                shareUserId = code.authenticationUserId,
                shareUsername =
                    userRepository.getByIdOrNull(code.authenticationUserId)?.username
                        ?: "未知",
                visibilityType = code.visibilityType,
                commentType = code.commentType,
                hashLink = code.hashLink,
                codeType = code.codeType,
            ),
        )
    }

    override suspend fun getCode(
        value: Token?,
        shareId: Int,
    ): GetCodeResp {
        val code = codeRepository.get(shareId) ?: return CodeNotFound
        return getCodeSafetyCheck(GetCodeId.Id, code, value)
    }

    override suspend fun getCodeByHash(
        value: Token?,
        hash: String,
    ): GetCodeResp {
        val code = codeRepository.getCodeByHash(hash) ?: return CodeNotFound
        return getCodeSafetyCheck(GetCodeId.HashLink, code, value)
    }

    override suspend fun setCodeCommentType(
        token: Token?,
        shareId: Int,
        type: CodeCommentType,
    ): SetCodeCommentTypeResp {
        if (!codeRepository.isIdExist(shareId)) {
            return CodeNotFound
        }
        val user = authentication.getAuthenticationUser(token)
        if (user?.id != codeRepository.getCodeOwnerId(shareId)) {
            return PermissionDenied
        }
        if (codeRepository.setCodeCommentType(shareId, type) != 1L) {
            return CodeNotFound
        }
        return SuccessSetCommentType
    }

    override suspend fun setCodeCommentVisibilityType(
        token: Token?,
        shareId: Int,
        commentId: Int,
        type: ShareCodeCommentType,
    ): SetCodeCommentVisibilityTypeResp {
        val user = authentication.getAuthenticationUser(token)
        if (user == null) {
            return PermissionDenied
        } else if (user.id != codeRepository.getCodeOwnerId(shareId)) {
            return PermissionDenied
        }

        if (!codeRepository.isCommentIdExist(commentId)) {
            return CommentNotFound
        }

        codeRepository.setShareCodeCommentVisibilityType(commentId, type)
        return SuccessSetCodeCommentVisibilityType
    }

    private suspend fun SaveCodeReq.toCode(user: AuthenticationUser): Code =
        Code(
            authenticationUserId = user.id,
            code = code,
            languageId = languageId,
            codeType = Code.CodeType.Share,
        )

    override suspend fun commit(commitOnCodeReq: CommitOnCodeReq): CommitOnCodeResp {
        if (commitOnCodeReq.token == null) {
            return PermissionDenied
        }

        val user = authentication.getAuthenticationUser(commitOnCodeReq.token) ?: return PermissionDenied

        val result =
            codeRepository.comment(
                ShareCodeComment(
                    committerAuthenticationUserId = user.id,
                    content = commitOnCodeReq.content,
                    shareCodeId = commitOnCodeReq.codeId,
                    type = commitOnCodeReq.type,
                ),
            )

        if (result.commentId == null) {
            return InternalError("CommitId 在插入后仍不存在...")
        }

        if (result.createdAt == null) {
            return InternalError("ShareCodeComment.CreateAt 在插入后仍不存在")
        }

        return CommitOnCodeResp.SuccessfulCommit(
            CreateCommentReq(
                result.commentId,
                user.username,
                commitOnCodeReq.codeId,
                commitOnCodeReq.content,
                createdAt = result.createdAt,
                visibilityType = result.type,
            ),
        )
    }

    override suspend fun getComments(
        token: Token?,
        sharCodeId: Int,
    ): GetCommitsOnCodeResp {
        if (!codeRepository.isIdExist(sharCodeId)) {
            return CodeNotFound
        } else {
            return codeRepository
                .getComments(sharCodeId)
                .mapNotNull {
                    val committerUsername =
                        userRepository.getByIdOrNull(it.committerAuthenticationUserId)
                            ?: return@mapNotNull null

                    val sharCodeOwnerId = codeRepository.getCodeOwnerId(sharCodeId)

                    val user =
                        authentication.getAuthenticationUser(token) ?: return@mapNotNull null

                    if (it.type == ShareCodeCommentType.Private) {
                        if (token == null) {
                            return@mapNotNull null
                        } else if (!(it.committerAuthenticationUserId == user.id || user.id == sharCodeOwnerId)) {
                            return@mapNotNull null
                        }
                    }

                    CreateCommentReq(
                        it.commentId ?: return@mapNotNull null,
                        committerUsername.username,
                        sharCodeId,
                        it.content,
                        createdAt = it.createdAt ?: return@mapNotNull null,
                        visibilityType = it.type,
                    )
                }.let { SuccessfulGetCommits(it) }
        }
    }

    override suspend fun deleteComments(commentIds: List<Int>): List<Int> =
        codeRepository.deleteComment(commentIds).mapNotNull {
            it.commentId
        }

    override suspend fun setCodeVisibility(
        token: Token?,
        shareId: Int,
        result: CodeVisibilityType,
    ): SetCodeVisibilityResp {
        val user = authentication.getAuthenticationUser(token) ?: return PermissionDenied

        val codeOwnerId = codeRepository.getCodeOwnerId(shareId) ?: return CodeNotFound

        if (codeOwnerId != user.id) {
            return PermissionDenied
        }

        val changeLine = codeRepository.setCodeVisibility(shareId, result)
        if (changeLine != 1L) {
            return CodeNotFound
        }
        return when (result) {
            Public -> onNotRestrictType(shareId)
            Private -> onNotRestrictType(shareId)
            Restrict -> onRestrictType(shareId)
        }
    }

    private suspend fun onNotRestrictType(shareId: Int): SuccessToPublicOrPrivate {
        codeRepository.setHashLink(shareId = shareId, null)
        return SuccessToPublicOrPrivate
    }

    private suspend fun onRestrictType(shareId: Int): SuccessToRestrict {
        val hashLink = uuid4().toString()
        codeRepository.setHashLink(shareId = shareId, hashLink)
        return SuccessToRestrict(hashLink)
    }
}
