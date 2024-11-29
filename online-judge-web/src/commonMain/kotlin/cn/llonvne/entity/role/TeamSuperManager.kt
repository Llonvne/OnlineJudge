package cn.llonvne.entity.role

import cn.llonvne.entity.group.GroupType
import kotlinx.serialization.Serializable

/**
 * 超级队伍管理员，拥有对一切队伍的管理权限
 */
@Serializable
sealed interface TeamSuperManager :
    TeamIdRole,
    CreateGroup {
    @Serializable
    data class TeamSuperManagerImpl(
        override val teamId: Int = 0,
        override val teamTypes: List<GroupType> = GroupType.entries,
    ) : TeamSuperManager {
        override fun check(provide: Role): Boolean = provide is TeamSuperManager
    }

    companion object {
        fun get(): TeamSuperManager = TeamSuperManagerImpl()
    }
}
