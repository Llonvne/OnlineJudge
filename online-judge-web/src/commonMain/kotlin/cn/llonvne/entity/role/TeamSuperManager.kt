package cn.llonvne.entity.role

import cn.llonvne.entity.group.GroupType
import kotlinx.serialization.Serializable

/**
 * 超级队伍管理员，拥有对一切队伍的管理权限
 */
@Serializable
sealed interface TeamSuperManager : TeamIdRole, CreateTeam {
    override val teamId: Int
        get() = 0

    override val teamTypes: List<GroupType>
        get() = GroupType.entries.toList()

    private data object TeamSuperManagerImpl : TeamSuperManager {
        override fun check(provide: Role): Boolean {
            return provide is TeamSuperManager
        }
    }

    companion object {
        fun get(): TeamSuperManager = TeamSuperManagerImpl
    }
}