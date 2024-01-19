package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.problem.language
import cn.llonvne.entity.problem.Language
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.map
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class LanguageRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
) {
    private val languageMeta = Meta.language

    suspend fun getByIdOrNull(id: Int?): Language? {
        if (id == null) {
            return null
        }
        return db.runQuery {
            getByIdOrNullQuery(id)
        }
    }

    fun getByIdOrNullQuery(id: Int): Query<Language?> {
        return QueryDsl.from(languageMeta).where {
            languageMeta.languageId eq id
        }.singleOrNull()
    }

    suspend fun isIdExist(id: Int) = db.runQuery {
        QueryDsl.from(languageMeta).where {
            languageMeta.languageId eq id
        }.select(count()).map {
            if (it == null) {
                return@map false
            } else {
                return@map it != 0L
            }
        }
    }
}