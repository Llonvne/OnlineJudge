package cn.llonvne.database.repository

import cn.llonvne.database.entity.def.authenticationUser
import cn.llonvne.security.UserRole
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.singleOrNull
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Repository

@Repository
class RoleRepository(@Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase) {

    private val userMeta = Meta.authenticationUser

    suspend fun getRoleStrByUserId(id: Int): String? {
        return db.runQuery {
            QueryDsl.from(userMeta)
                .where { userMeta.id eq id }
                .select(userMeta.role)
                .singleOrNull()
        }
    }

    suspend fun setRoleStrByUserId(id: Int, role: UserRole): Long {

        val str = role.asJson

        return db.runQuery {
            QueryDsl.update(userMeta)
                .set {
                    userMeta.role eq str
                }.where {
                    userMeta.id eq id
                }
        }
    }
}