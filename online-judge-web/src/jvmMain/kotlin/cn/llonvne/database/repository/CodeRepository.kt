package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.code
import cn.llonvne.entity.problem.Code
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class CodeRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val db: R2dbcDatabase
) {
    private val codeMeta = Meta.code

    suspend fun save(code: Code) = db.runQuery {
        QueryDsl.insert(codeMeta)
            .single(code).returning()
    }

    suspend fun get(shareId: Int): Code? {
        return db.runQuery { QueryDsl.from(codeMeta).where { codeMeta.codeId eq shareId }.singleOrNull() }
    }
}