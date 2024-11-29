package cn.llonvne.database.resolver.group

import cn.llonvne.database.aware.GroupInfoAwareProvider.GroupInfoAware
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.group.GroupVisibility
import cn.llonvne.entity.group.GroupVisibility.*
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp
import cn.llonvne.kvision.service.PermissionDenied
import org.springframework.stereotype.Service

/**
 * 用于确定在以未登入用户加载小组信息时是否显示，遵从 [GroupVisibility] 的说明
 */
@Service
class GroupGuestPassResolver {
    context(GroupInfoAware)
    suspend fun resolve(loadAsGuest: suspend () -> LoadGroupResp): LoadGroupResp =
        when (group.visibility) {
            Public -> loadAsGuest()
            Private -> {
                PermissionDenied
            }

            Restrict -> {
                when (groupId) {
                    is GroupId.HashGroupId -> loadAsGuest()
                    is GroupId.IntGroupId -> PermissionDenied
                    is GroupId.ShortGroupName -> PermissionDenied
                }
            }
        }
}
