package cn.llonvne.database.resolver.group

import cn.llonvne.database.resolver.group.JoinGroupVisibilityCheckResolver.JoinGroupVisibilityCheckResult.*
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupVisibility
import org.springframework.stereotype.Service

/**
 * 检查 [GroupVisibility] 与 [GroupId] 的关系并确定是否允许加入/审批加入，遵从 [GroupVisibility.chinese] 的说明
 */
@Service
class JoinGroupVisibilityCheckResolver {
    /**
     * [Accepted] 直接加入
     * [Waiting] 发送申请
     * [Rejected] 拒绝加入
     */
    enum class JoinGroupVisibilityCheckResult {
        Accepted,
        Waiting,
        Rejected,
    }

    fun resolve(
        visibility: GroupVisibility,
        groupId: GroupId,
    ): JoinGroupVisibilityCheckResult =
        when (visibility) {
            GroupVisibility.Public -> Accepted
            GroupVisibility.Private -> Rejected
            GroupVisibility.Restrict -> onRestrict(groupId)
        }

    private fun onRestrict(groupId: GroupId): JoinGroupVisibilityCheckResult =
        when (groupId) {
            is GroupId.HashGroupId -> Accepted
            is GroupId.IntGroupId -> Waiting
            is GroupId.ShortGroupName -> Waiting
        }
}
