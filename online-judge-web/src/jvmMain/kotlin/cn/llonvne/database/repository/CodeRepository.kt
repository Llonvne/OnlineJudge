package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.code
import cn.llonvne.database.entity.def.shareCodeComment
import cn.llonvne.entity.problem.Code
import cn.llonvne.entity.problem.ShareCodeComment
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.data.domain.Limit
import org.springframework.stereotype.Repository

@Repository
class CodeRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val db: R2dbcDatabase
) {
    private val codeMeta = Meta.code
    private val commentMeta = Meta.shareCodeComment

    suspend fun save(code: Code) = db.runQuery {
        QueryDsl.insert(codeMeta)
            .single(code).returning()
    }

    suspend fun get(shareId: Int): Code? {
        return db.runQuery { QueryDsl.from(codeMeta).where { codeMeta.codeId eq shareId }.singleOrNull() }
    }

    suspend fun isIdExist(shareCodeId: Int): Boolean = db.runQuery {
        QueryDsl.from(codeMeta).where {
            codeMeta.codeId eq shareCodeId
        }.select(count())
    } == 1.toLong()

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