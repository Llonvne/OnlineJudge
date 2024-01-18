package cn.llonvne.database.schema

import kotlinx.coroutines.runBlocking
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component


@Component
class Schema(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        runBlocking {
            db.runQuery {
                QueryDsl.create(Meta.all())
            }
        }
    }
}