package cn.llonvne.database.schema

import org.komapper.r2dbc.R2dbcDatabase

fun interface SchemaInitializer {
    suspend fun init(db: R2dbcDatabase)
}