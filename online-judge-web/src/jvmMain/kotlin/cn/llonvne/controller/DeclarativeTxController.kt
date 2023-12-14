package cn.llonvne.controller

import cn.llonvne.database.entity.User
import cn.llonvne.database.entity.employee
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/")
@RestController
@Transactional
class DeclarativeTxController(private val database: R2dbcDatabase) {

}