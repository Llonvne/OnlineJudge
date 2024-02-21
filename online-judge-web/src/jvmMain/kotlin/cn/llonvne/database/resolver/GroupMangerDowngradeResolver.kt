package cn.llonvne.database.resolver

import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.GroupManager
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.kvision.service.RoleService
import org.springframework.stereotype.Service

@Service
class GroupMangerDowngradeResolver(
    private val roleService: RoleService
) {

    enum class GroupManagerDowngradeResult {
        DowngradeToIdNotMatchToGroupId,
        BeDowngradeUserNotFound,
        UserAlreadyHasThisRole,
        Success
    }

    suspend fun resolve(
        groupId: GroupId,
        groupIntId: Int,
        operator: AuthenticationUser,
        beDowngradeUserId: Int,
    ): GroupManagerDowngradeResult {
        val beDowngradeUserRole =
            roleService.get(beDowngradeUserId) ?: return GroupManagerDowngradeResult.BeDowngradeUserNotFound

        val removedRoles = beDowngradeUserRole.roles.filterIsInstance<GroupManager.GroupMangerImpl>()

        return if (roleService.removeRole(beDowngradeUserId, removedRoles)) {
            GroupManagerDowngradeResult.Success
        } else {
            GroupManagerDowngradeResult.BeDowngradeUserNotFound
        }
    }
}