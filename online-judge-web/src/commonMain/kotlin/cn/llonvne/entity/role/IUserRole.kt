package cn.llonvne.entity.role

import kotlinx.serialization.Serializable

/**
 * [UserRole 的数据传输对象]
 */
@Serializable
data class IUserRole(
    val roles: List<Role>,
)

inline fun <reified R : Role> List<Role>.check(required: R): Boolean =
    map { provide ->
        required.check(provide)
    }.contains(true)
