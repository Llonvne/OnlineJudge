package cn.llonvne.database.aware

import cn.llonvne.database.aware.GroupInfoAwareProvider.GroupInfoAware
import cn.llonvne.database.resolver.group.GroupMembersResolver
import cn.llonvne.database.resolver.group.GroupRoleResolver
import cn.llonvne.entity.group.Group
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.GroupOwner
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GroupMemberDtoImpl
import org.springframework.stereotype.Service

/**
 * 提供小组相关信息(不包括在 [Group] 内部的),一般通过 [GroupInfoAware] 作为 context 作为接收器参数
 */
@Service
class GroupInfoAwareProvider(
    private val groupMembersResolver: GroupMembersResolver,
    private val groupRoleResolver: GroupRoleResolver,
) {
    suspend fun <R> awareOf(
        groupId: GroupId,
        id: Int,
        group: Group,
        action: suspend GroupInfoAware.() -> R,
    ): R = GroupInfoAware(groupId, id, group).action()

    inner class GroupInfoAware(
        val groupId: GroupId,
        val id: Int,
        val group: Group,
    ) {
        suspend fun ownerName(): String = groupMembersResolver.fromRole(GroupOwner.GroupOwnerImpl(id)).firstOrNull()?.username ?: "<未找到>"

        suspend fun membersOfGuest(): List<GroupMemberDtoImpl> {
            return groupMembersResolver.fromGroupId(id).mapNotNull {
                GroupMemberDtoImpl(
                    username = it.username,
                    role = groupRoleResolver.resolve(id, it) ?: return@mapNotNull null,
                    userId = it.id,
                )
            }
        }

        suspend fun memberOfManager(): List<GroupMemberDtoImpl> = membersOfGuest()

        suspend fun membersOfMember(): List<GroupMemberDtoImpl> = membersOfGuest()

        suspend fun membersOfOwner() = membersOfMember()
    }
}
