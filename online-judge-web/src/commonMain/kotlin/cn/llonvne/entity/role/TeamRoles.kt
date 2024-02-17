package cn.llonvne.entity.role

sealed interface Role {
}

sealed interface CreateTeam : Role {
    companion object : CreateTeam
}

sealed interface DeleteTeam : Role {
    companion object : DeleteTeam
}

sealed interface InviteMember : Role {
    companion object : InviteMember
}

sealed interface KickMember : Role {
    companion object : KickMember
}

sealed interface TeamManager : CreateTeam, DeleteTeam, InviteMember, KickMember {
    companion object : TeamManager
}



