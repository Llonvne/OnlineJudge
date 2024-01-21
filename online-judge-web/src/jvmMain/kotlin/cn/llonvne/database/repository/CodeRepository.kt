package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.code
import cn.llonvne.database.entity.def.shareCodeComment
import cn.llonvne.entity.problem.Code
import cn.llonvne.entity.problem.ShareCodeComment
import net.sf.jsqlparser.statement.comment.Comment
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.define
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class CodeRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase
) {
    private val codeMeta = Meta.code
    private val commentMeta = Meta.shareCodeComment.define {
        where {
            // 默认不查询被删除的评论
            it.type notEq ShareCodeComment.Companion.ShareCodeCommentType.Deleted
        }
    }

    suspend fun save(code: Code) = db.runQuery {
        QueryDsl.insert(codeMeta).single(code).returning()
    }

    suspend fun get(shareId: Int): Code? {
        return db.runQuery { QueryDsl.from(codeMeta).where { codeMeta.codeId eq shareId }.singleOrNull() }
    }

    suspend fun getCodeOwnerId(shareCodeId: Int): Int? {
        return db.runQuery {
            QueryDsl.from(codeMeta).where {
                codeMeta.codeId eq shareCodeId
            }
                .select(codeMeta.authenticationUserId)
                .singleOrNull()
        }
    }

    suspend fun isIdExist(shareCodeId: Int): Boolean = db.runQuery {
        QueryDsl.from(codeMeta).where {
            codeMeta.codeId eq shareCodeId
        }.select(count())
    } == 1.toLong()

    suspend fun deleteComment(ids: List<Int>): List<ShareCodeComment> {
        return db.runQuery {
            QueryDsl.update(commentMeta).set {
                commentMeta.type eq ShareCodeComment.Companion.ShareCodeCommentType.Deleted
            }.where {
                commentMeta.commentId inList ids
            }.returning()
        }
    }

    suspend fun comment(comment: ShareCodeComment): ShareCodeComment {
        return db.runQuery {
            QueryDsl.insert(commentMeta).single(
                comment
            ).returning()
        }
    }

    suspend fun getComments(shareCodeId: Int, limit: Int = 500): List<ShareCodeComment> {
        return db.runQuery {
            QueryDsl.from(commentMeta).where {
                commentMeta.shareCodeId eq shareCodeId
            }.limit(500)
        }
    }
}