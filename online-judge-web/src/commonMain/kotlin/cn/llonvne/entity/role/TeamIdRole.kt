package cn.llonvne.entity.role

import cn.llonvne.entity.role.TeamIdRole.Companion.simpleName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * 特定队伍权限身份接口，表示该权限对于唯一一个 [teamId]
 */
@Serializable
sealed interface TeamIdRole : Role {
    /**
     * [teamId] 标识符号
     */
    val teamId: Int

    companion object : SuperRole {
        fun TeamIdRole.simpleName(cls: KClass<*>): String {
            return "<${cls.simpleName}-id-$teamId>"
        }

        override fun superRole(): Role {
            return TeamSuperManager.get()
        }

        fun getManagerRoles(groupId: Int): List<TeamIdRole> {
            return listOf(
                GroupManager.GroupMangerImpl(groupId),
                GroupOwner.GroupOwnerImpl(groupId)
            )
        }
    }
}

inline fun <reified T : TeamIdRole> T.checkInternal(provide: Role): Boolean {

    if (provide is TeamSuperManager) {
        return true
    }

    if (provide is T) {
        return provide.teamId == teamId
    }
    return false
}

@Serializable
sealed interface TeamMember : TeamIdRole {
    @Serializable
    data class TeamMemberImpl(override val teamId: Int) : TeamMember {
        override fun check(provide: Role): Boolean = checkInternal<TeamMember>(provide)
        override fun toString(): String {
            return simpleName(TeamMember::class)
        }
    }
}

@Serializable
sealed interface DeleteTeam : TeamIdRole {
    @Serializable
    data class DeleteTeamImpl(override val teamId: Int) : DeleteTeam {
        override fun check(provide: Role): Boolean = checkInternal<DeleteTeam>(provide)
        override fun toString(): String {
            return simpleName(DeleteTeam::class)
        }
    }
}

@Serializable
sealed interface InviteMember : TeamIdRole {
    @Serializable
    data class InviteMemberImpl(override val teamId: Int) : InviteMember {
        override fun check(provide: Role): Boolean = checkInternal<InviteMember>(provide)
        override fun toString(): String {
            return simpleName(InviteMember::class)
        }
    }
}

@Serializable
sealed interface KickMember : TeamIdRole {
    @Serializable
    data class KickMemberImpl(override val teamId: Int) : KickMember {
        override fun check(provide: Role): Boolean = checkInternal<KickMember>(provide)
        override fun toString(): String {
            return simpleName(KickMember::class)
        }
    }
}

@Serializable
sealed interface GroupManager : DeleteTeam, InviteMember, KickMember {
    @Serializable
    data class GroupMangerImpl(override val teamId: Int) : GroupManager {
        override fun check(provide: Role): Boolean = checkInternal<GroupManager>(provide)
        override fun toString(): String {
            return simpleName(GroupManager::class)
        }
    }
}

@Serializable
sealed interface GroupOwner : GroupManager {
    @Serializable
    data class GroupOwnerImpl(override val teamId: Int) : GroupOwner {
        override fun check(provide: Role): Boolean = checkInternal<GroupOwner>(provide)
        override fun toString(): String {
            return simpleName(GroupOwner::class)
        }
    }
}