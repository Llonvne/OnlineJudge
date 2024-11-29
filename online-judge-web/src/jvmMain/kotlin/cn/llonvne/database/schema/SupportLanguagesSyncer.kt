package cn.llonvne.database.schema

import cn.llonvne.database.entity.def.problem.language
import cn.llonvne.entity.problem.Language
import cn.llonvne.gojudge.api.SupportLanguages
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Component

@Component
private class SupportLanguagesSyncer : SchemaInitializer {
    private val languageMeta = Meta.language

    override suspend fun init(db: R2dbcDatabase) {
        val languages =
            SupportLanguages.entries.map {
                Language(
                    it.languageId,
                    it.languageName,
                    it.languageVersion,
                )
            }

        db.runQuery {
            QueryDsl
                .drop(languageMeta)
                .andThen(
                    QueryDsl.create(languageMeta),
                ).andThen(
                    QueryDsl.insert(languageMeta).multiple(languages),
                )
        }
    }
}
