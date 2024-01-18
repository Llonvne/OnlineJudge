package cn.llonvne.database.service

import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.stereotype.Service

@Service
class ProblemRepository(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val db: R2dbcDatabase,
) {
}