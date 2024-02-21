package cn.llonvne.database.resolver

import cn.llonvne.database.resolver.GroupMemberUpgradeResolver.GroupMemberUpgradeResult.*
import cn.llonvne.entity.AuthenticationUser
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.TeamIdRole
import cn.llonvne.kvision.service.RoleService
import org.springframework.stereotype.Service

@Service
class GroupMemberUpgradeResolver(
    private val roleService: RoleService
) {

    enum class GroupMemberUpgradeResult {
        UpdateToIdNotMatchToGroupId,
        UserAlreadyHasThisRole,
        BeUpdatedUserNotFound,
        Success
    }

    suspend fun resolve(
        groupId: GroupId,
        groupIntId: Int,
        updater: AuthenticationUser,
        beUpdatedUserId: Int,
        updateTo: TeamIdRole
    ): GroupMemberUpgradeResult {

        if (updateTo.teamId != groupIntId) {
            return UpdateToIdNotMatchToGroupId
        }

        val beUpdatedUserRole = roleService.get(beUpdatedUserId) ?: return BeUpdatedUserNotFound

        if (beUpdatedUserRole.roles.contains(updateTo)) {
            return UserAlreadyHasThisRole
        }

        return if (roleService.addRole(beUpdatedUserId, updateTo)){
            Success
        } else {
            BeUpdatedUserNotFound
        }
    }
}