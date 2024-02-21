package cn.llonvne.entity.role

import kotlinx.serialization.Serializable

/**
 * 权限认证接口
 */
@Serializable
sealed interface Role {
    /**
     * 检查 [provide] 是否可以通过认证
     */
    fun check(provide: Role): Boolean
}

