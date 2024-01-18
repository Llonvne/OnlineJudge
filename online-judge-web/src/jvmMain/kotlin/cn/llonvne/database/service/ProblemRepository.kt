package cn.llonvne.database.service

import cn.llonvne.database.entity.def.problem.problem
import cn.llonvne.database.entity.def.problem.tag.problemTag
import cn.llonvne.entity.problem.Problem
import cn.llonvne.entity.problem.ProblemTag
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.lower
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.core.dsl.query.zip
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Service

@Service
class ProblemRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
    private val authorRepository: AuthorRepository
) {
    private val problemMeta = Meta.problem
    private val problemTagMeta = Meta.problemTag

    suspend fun getById(id: Int): Problem? = db.runQuery {
        QueryDsl.from(problemMeta)
            .where {
                problemMeta.problemId eq id
            }.singleOrNull()
    }

    suspend fun getProblemTags(problemId: Int): List<ProblemTag> = db.runQuery {
        QueryDsl.from(problemTagMeta).where {
            problemTagMeta.problemId eq problemId
        }
    }

    suspend fun search(text: String): List<Problem> {
        val toInt = text.toIntOrNull()

        val lowerText = text.lowercase()

        return db.runQuery {
            QueryDsl.from(problemMeta)
                .where {
                    or {
                        lower(problemMeta.problemName) contains lowerText
                    }
                    or {
                        lower(problemMeta.problemDescription) contains lowerText
                    }
                    if (toInt != null) {
                        or {
                            problemMeta.problemId eq toInt
                        }
                        or {
                            problemMeta.authorId eq toInt
                        }
                    }
                }
        }
    }
}
