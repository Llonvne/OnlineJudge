package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.authenticationUser
import cn.llonvne.database.entity.def.createAtNow
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.kvision.service.IAuthenticationService
import cn.llonvne.kvision.service.IAuthenticationService.LoginResult.IncorrectUsernameOrPassword
import cn.llonvne.kvision.service.IAuthenticationService.LoginResult.SuccessfulLogin
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.BPasswordEncoder.Companion.invoke
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.map
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationUserRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
    private val passwordEncoder: PasswordEncoder,
) {

    private val userMeta = Meta.authenticationUser

    suspend fun new(name: String, password: String) = passwordEncoder {
        db.runQuery {
            QueryDsl.insert(userMeta).single(AuthenticationUser.createAtNow(name, password)).returning()
        }
    }

    suspend fun login(username: String, rawPassword: String): IAuthenticationService.LoginResult = passwordEncoder {
        val user = db.runQuery {
            QueryDsl.from(userMeta).where {
                userMeta.username eq username
            }.singleOrNull()
        }
        return@passwordEncoder if (user == null) {
            IAuthenticationService.LoginResult.UserDoNotExist
        } else if (matches(rawPassword, user.encryptedPassword)) {
            SuccessfulLogin(AuthenticationToken.of(username, username, user.id))
        } else {
            IncorrectUsernameOrPassword
        }
    }

    internal suspend fun usernameAvailableQuery(username: String): Query<Boolean> {
        return QueryDsl.from(userMeta).where {
            userMeta.username eq username
        }.selectNotNull(count()).map {
            it == 0L
        }
    }

    internal suspend fun usernameAvailable(username: String): Boolean {
        val count = db.runQuery {
            QueryDsl.from(userMeta).where {
                userMeta.username eq username
            }.selectNotNull(count())
        }
        return count == 0.toLong()
    }

    internal suspend fun isIdExist(authenticationUserId: Int): Boolean {
        return db.runQuery {
            QueryDsl.from(userMeta).where {
                userMeta.id eq authenticationUserId
            }.map {
                it.isNotEmpty()
            }
        }
    }

    internal suspend fun getByIdOrNull(authenticationUserId: Int) = db.runQuery {
        QueryDsl.from(userMeta).where {
            userMeta.id eq authenticationUserId
        }.singleOrNull()
    }
}