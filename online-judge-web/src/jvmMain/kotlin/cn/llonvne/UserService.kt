package cn.llonvne

import cn.llonvne.database.entity.def.authenticationUser
import cn.llonvne.entity.AuthenticationUser
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class UserService(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val database: R2dbcDatabase,
) : IUserService {
    private val userMeta = Meta.authenticationUser

    class UserNotFound : Exception()

    override suspend fun byId(id: Int): AuthenticationUser {
        val query =
            QueryDsl
                .from(userMeta)
                .where {
                    userMeta.id.eq(id)
                }
        return database.runQuery(query).firstOrNull() ?: throw UserNotFound()
    }
}
