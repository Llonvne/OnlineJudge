package cn.llonvne.database.aware

import cn.llonvne.database.resolver.GroupMembersResolver
import cn.llonvne.database.resolver.GroupRoleResolver
import cn.llonvne.entity.group.Group
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.GroupManager
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.GroupMemberDtoImpl
import org.springframework.stereotype.Service

/**
 * 提供小组相关信息(不包括在 [Group] 内部的),一般通过 [GroupInfoAware] 作为 context 作为接收器参数
 */
@Service
class GroupInfoAwareProvider(
    private val groupMembersResolver: GroupMembersResolver,
    private val groupRoleResolver: GroupRoleResolver
) {

    suspend fun <R> awareOf(groupId: GroupId, id: Int, group: Group, action: suspend GroupInfoAware.() -> R): R {
        return GroupInfoAware(groupId, id, group).action()
    }


    inner class GroupInfoAware(
        val groupId: GroupId,
        val id: Int,
        val group: Group
    ) {

        suspend fun ownerName(): String {
            return groupMembersResolver.fromRole(GroupManager.GroupMangerImpl(id)).firstOrNull()?.username ?: "<未找到>"
        }

        suspend fun membersOfGuest(): List<GroupMemberDtoImpl> {
            return groupMembersResolver.fromGroupId(id).mapNotNull {
                GroupMemberDtoImpl(
                    username = it.username,
                    role = groupRoleResolver.resolve(id, it) ?: return@mapNotNull null
                )
            }
        }

        suspend fun memberOfManager(): List<GroupMemberDtoImpl> {
            return membersOfGuest()
        }
    }
}