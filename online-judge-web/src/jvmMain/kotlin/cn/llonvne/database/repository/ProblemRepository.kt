package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.problem.language
import cn.llonvne.database.entity.def.problem.problem
import cn.llonvne.database.entity.def.problem.tag.problemTag
import cn.llonvne.database.entity.problemSupportLanguage
import cn.llonvne.entity.problem.Language
import cn.llonvne.entity.problem.context.Problem
import cn.llonvne.entity.problem.ProblemTag
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.lower
import org.komapper.core.dsl.query.flatMap
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Service

@Service
class ProblemRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
) {
    private val problemMeta = Meta.problem
    private val problemTagMeta = Meta.problemTag
    private val problemLanguageMeta = Meta.problemSupportLanguage
    private val languageMeta = Meta.language

    suspend fun isIdExist(id: Int?): Boolean {
        if (id == null) {
            return false
        }
        return db.runQuery {
            QueryDsl.from(problemMeta).where {
                problemMeta.problemId eq id
            }.select(count())
        } != 0.toLong()
    }

    suspend fun list(limit: Int = 500): List<Problem> = db.runQuery {
        QueryDsl.from(problemMeta)
            .limit(limit)
    }

    suspend fun getById(id: Int?): Problem? {
        if (id == null) {
            return null
        }
        return db.runQuery {
            QueryDsl.from(problemMeta)
                .where {
                    problemMeta.problemId eq id
                }.singleOrNull()
        }
    }

    suspend fun getProblemTags(problemId: Int): List<ProblemTag> = db.runQuery {
        QueryDsl.from(problemTagMeta).where {
            problemTagMeta.problemId eq problemId
        }
    }

    suspend fun getSupportLanguage(problemId: Int, limit: Int = 500): List<Language> {
        val q1 = QueryDsl.from(problemLanguageMeta).where {
            problemLanguageMeta.problemId eq problemId
        }
        val q2 = q1.flatMap {
            QueryDsl.from(languageMeta).where {
                languageMeta.languageId inList it.map { it.languageId }
            }
        }

        return db.runQuery { q2 }
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

    suspend fun create(problem: Problem) = db.runQuery {
        QueryDsl.insert(problemMeta).single(problem)
    }
}
