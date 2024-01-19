package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.problem.language
import cn.llonvne.entity.problem.Language
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class LanguageRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
) {
    private val languageMeta = Meta.language

    suspend fun getByIdOrNull(id: Int): Language? {
        return db.runQuery {
            QueryDsl.from(languageMeta).where {
                languageMeta.languageId eq id
            }.singleOrNull()
        }
    }
}