package cn.llonvne.controller

import cn.llonvne.database.entity.Employee
import cn.llonvne.database.entity.employee
import kotlinx.coroutines.flow.Flow
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

    @RequestMapping
    suspend fun list(): Flow<Employee> {
        return database.flowQuery {
            val m = Meta.employee
            QueryDsl.from(m).where {
                m.id.eq(1)
                m.name.eq("llonvne")
            }
        }
    }

    @RequestMapping(params = ["text"])
    suspend fun add(@RequestParam text: String): Employee {
        val message = Employee(name = "Llonvne")
        return database.runQuery {
            val m = Meta.employee
            QueryDsl.insert(m).single(message)
        }
    }
}