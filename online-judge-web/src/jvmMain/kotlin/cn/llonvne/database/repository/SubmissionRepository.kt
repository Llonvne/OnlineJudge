package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.problem.submission
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository


@Repository
class SubmissionRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase
) {
    val submissionMeta = Meta.submission

    suspend fun list(limit: Int = 500) = db.runQuery {
        QueryDsl.from(submissionMeta)
            .limit(limit)
    }

    suspend fun getById(id: Int) = db.runQuery {
        QueryDsl.from(submissionMeta)
            .where {
                submissionMeta.submissionId eq id
            }.singleOrNull()
    }
}