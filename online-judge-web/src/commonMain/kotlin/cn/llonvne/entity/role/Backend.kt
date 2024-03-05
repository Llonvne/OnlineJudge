package cn.llonvne.entity.role

import kotlinx.serialization.Serializable

/***
 * 指示一个用户是否能访问后台
 */
sealed interface Backend : Role {
    @Serializable
    data object BackendImpl : Backend {
        override fun check(provide: Role): Boolean {
            return provide is Backend
        }
    }
}