package cn.llonvne.database.service

import cn.llonvne.database.entity.def.author
import cn.llonvne.entity.Author
import cn.llonvne.kvision.service.exception.AuthorAuthenticationUserIdNotExist
import cn.llonvne.kvision.service.exception.AuthorNotExist
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.map
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.Throws

@Service
@Transactional
class AuthorRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
    private val authenticationService: AuthenticationUserRepository
) {
    private val authorMeta = Meta.author
    suspend fun create(author: Author): Author {

        // 检查 authenticationUserId 是否有效
        isAuthenticationUserIdVaild(author)

        return db.runQuery {
            QueryDsl.insert(authorMeta).single(author)
        }
    }

    suspend fun isAuthorIdExist(id: Int): Boolean {
        return db.runQuery {
            QueryDsl.from(authorMeta)
                .where {
                    authorMeta.authorId eq id
                }.singleOrNull().map {
                    it != null
                }
        }
    }
    @Throws(AuthorNotExist::class)
    suspend fun getByIdOrThrow(id: Int): Author {
        return db.runQuery {
            QueryDsl.from(authorMeta)
                .where {
                    authorMeta.authorId eq id
                }.singleOrNull().map {
                    it ?: throw AuthorNotExist()
                }
        }
    }

    private suspend fun isAuthenticationUserIdVaild(author: Author) {
        val id = author.authenticationUserId

        // 如果 ID 为空，则不做检查
        if (id != null) {
            val isUserExistQuery = authenticationService.isIdExist(id)
            if (!isUserExistQuery) {
                throw AuthorAuthenticationUserIdNotExist()
            }
        }
    }
}