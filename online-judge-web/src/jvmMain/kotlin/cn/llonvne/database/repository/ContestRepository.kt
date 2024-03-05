package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.contest
import cn.llonvne.entity.contest.Contest
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class ContestRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase
) {

    private val contestMeta = Meta.contest

    suspend fun create(contest: Contest): Contest {
        return db.runQuery {
            QueryDsl.insert(contestMeta).single(contest).returning()
        }
    }

    suspend fun getById(id: Int): Contest? {
        return db.runQuery {
            QueryDsl.from(contestMeta).where {
                contestMeta.contestId eq id
            }.singleOrNull()
        }
    }

    suspend fun getByHash(hash: String): Contest? {
        return db.runQuery {
            QueryDsl.from(contestMeta).where {
                contestMeta.hashLink eq hash
            }.singleOrNull()
        }
    }
}