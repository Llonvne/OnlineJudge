package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.code
import cn.llonvne.database.entity.def.shareCodeComment
import cn.llonvne.entity.problem.ShareCodeComment
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType
import cn.llonvne.entity.problem.ShareCodeComment.Companion.ShareCodeCommentType.Deleted
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.CodeVisibilityType
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.define
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.map
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class CodeRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
) {
    private val codeMeta = Meta.code
    private val commentMeta =
        Meta.shareCodeComment.define {
            where {
                // 默认不查询被删除的评论
                it.type notEq Deleted
            }
        }

    suspend fun save(code: Code) =
        db.runQuery {
            QueryDsl.insert(codeMeta).single(code).returning()
        }

    suspend fun get(shareId: Int): Code? = db.runQuery { QueryDsl.from(codeMeta).where { codeMeta.codeId eq shareId }.singleOrNull() }

    suspend fun getCodeByHash(hash: String): Code? =
        db.runQuery { QueryDsl.from(codeMeta).where { codeMeta.hashLink eq hash }.singleOrNull() }

    suspend fun getCodeOwnerId(shareCodeId: Int): Int? =
        db.runQuery {
            QueryDsl
                .from(codeMeta)
                .where {
                    codeMeta.codeId eq shareCodeId
                }.select(codeMeta.authenticationUserId)
                .singleOrNull()
        }

    suspend fun isIdExist(shareCodeId: Int): Boolean =
        db.runQuery {
            QueryDsl
                .from(codeMeta)
                .where {
                    codeMeta.codeId eq shareCodeId
                }.select(count())
        } == 1.toLong()

    suspend fun setCodeVisibility(
        shareId: Int,
        visibilityType: CodeVisibilityType,
    ): Long =
        db.runQuery {
            QueryDsl
                .update(codeMeta)
                .set {
                    codeMeta.visibilityType eq visibilityType
                }.where {
                    codeMeta.codeId eq shareId
                }
        }

    suspend fun setHashLink(
        shareId: Int,
        hashLink: String?,
    ): Long =
        db.runQuery {
            QueryDsl
                .update(codeMeta)
                .set {
                    codeMeta.hashLink eq hashLink
                }.where {
                    codeMeta.codeId eq shareId
                }
        }

    suspend fun deleteComment(ids: List<Int>): List<ShareCodeComment> =
        db.runQuery {
            QueryDsl
                .update(commentMeta)
                .set {
                    commentMeta.type eq Deleted
                }.where {
                    commentMeta.commentId inList ids
                }.returning()
        }

    suspend fun comment(comment: ShareCodeComment): ShareCodeComment =
        db.runQuery {
            QueryDsl
                .insert(commentMeta)
                .single(
                    comment,
                ).returning()
        }

    suspend fun getComments(
        shareCodeId: Int,
        limit: Int = 500,
    ): List<ShareCodeComment> =
        db.runQuery {
            QueryDsl
                .from(commentMeta)
                .where {
                    commentMeta.shareCodeId eq shareCodeId
                }.limit(500)
        }

    suspend fun setCodeCommentType(
        shareId: Int,
        type: CodeCommentType,
    ): Long =
        db.runQuery {
            QueryDsl
                .update(codeMeta)
                .set {
                    codeMeta.commentType eq type
                }.where {
                    codeMeta.codeId eq shareId
                }
        }

    suspend fun isCommentIdExist(commentId: Int) =
        db.runQuery {
            QueryDsl
                .from(commentMeta)
                .where {
                    commentMeta.commentId eq commentId
                }.select(count())
                .map {
                    it == 1L
                }
        }

    suspend fun setShareCodeCommentVisibilityType(
        commentId: Int,
        type: ShareCodeCommentType,
    ) {
        db.runQuery {
            QueryDsl
                .update(commentMeta)
                .set {
                    commentMeta.type eq type
                }.where {
                    commentMeta.commentId eq commentId
                }
        }
    }

    suspend fun getCodeLanguageId(codeId: Int): Int? =
        db.runQuery {
            QueryDsl
                .from(codeMeta)
                .where {
                    codeMeta.codeId eq codeId
                }.select(codeMeta.languageId)
                .singleOrNull()
        }

    suspend fun getCodeLength(codeId: Int): Int? =
        db.runQuery {
            QueryDsl
                .from(codeMeta)
                .where {
                    codeMeta.codeId eq codeId
                }.select(codeMeta.code)
                .singleOrNull()
                .map {
                    it?.length
                }
        }

    suspend fun getCodeVisibilityType(codeId: Int): CodeVisibilityType? =
        db.runQuery {
            QueryDsl
                .from(codeMeta)
                .where {
                    codeMeta.codeId eq codeId
                }.select(codeMeta.visibilityType)
                .singleOrNull()
        }
}
