package cn.llonvne.entity.role

import kotlinx.serialization.Serializable

/**
 * 对于有状态的权限，可以通过该接口获得对于任何状态都符合的权限
 */
@Serializable
sealed interface SuperRole {
    fun superRole(): Role
}