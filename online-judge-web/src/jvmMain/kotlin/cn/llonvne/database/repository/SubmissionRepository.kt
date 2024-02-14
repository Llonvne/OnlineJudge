package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.code
import cn.llonvne.database.entity.def.problem.submission
import cn.llonvne.entity.problem.Submission
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.types.ProblemStatus
import cn.llonvne.security.AuthenticationToken
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository


@Repository
class SubmissionRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase
) {
    private val submissionMeta = Meta.submission
    private val codeMeta = Meta.code

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

    suspend fun save(submission: Submission): Submission {
        return db.runQuery {
            QueryDsl.insert(submissionMeta).single(submission).returning()
        }
    }

    suspend fun getByCodeId(codeId: Int): Submission? {
        return db.runQuery {
            QueryDsl.from(submissionMeta)
                .where {
                    submissionMeta.codeId eq codeId
                }.singleOrNull()
        }
    }

    suspend fun getByAuthenticationUserID(
        userID: Int, codeType: Code.CodeType?, limit: Int = 500
    ): List<Submission> {
        return db.runQuery {
            QueryDsl.from(submissionMeta).innerJoin(codeMeta) {
                submissionMeta.codeId eq codeMeta.codeId
            }.where {
                submissionMeta.authenticationUserId eq userID
                and {
                    if (codeType != null) {
                        codeMeta.codeType eq codeType
                    }
                }
            }.limit(limit)
        }
    }

    fun getUserProblemStatus(token: AuthenticationToken?, problemId: Int): ProblemStatus {
        // TODO
        return ProblemStatus.NotBegin
    }
}