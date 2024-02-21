package cn.llonvne.site.team.show.member

import cn.llonvne.compoent.badge
import cn.llonvne.compoent.team.KickMemberResolver
import cn.llonvne.entity.role.GroupManager
import cn.llonvne.entity.role.GroupOwner
import cn.llonvne.entity.types.badge.BadgeColor
import cn.llonvne.kvision.service.IGroupService
import cn.llonvne.kvision.service.IGroupService.LoadGroupResp.*
import cn.llonvne.model.AuthenticationModel
import io.kvision.core.onClickLaunch
import io.kvision.html.Span



fun interface KickOp {
    fun load(target: Span, user: GroupMemberDto, kickMemberResolver: KickMemberResolver)

    companion object {
        private val emptyKickOp = KickOp { _, _, _ -> }

        fun from(resp: LoadGroupSuccessResp): KickOp {

            val impl = KickOpImpl()

            return when (resp) {
                is GuestLoadGroup -> emptyKickOp
                is MemberLoadGroup -> emptyKickOp
                is OwnerLoadGroup -> {
                    KickOp { target: Span, user: GroupMemberDto, kickMemberResolver: KickMemberResolver ->
                        onNotSelf(user) {
                            impl.load(target, user, kickMemberResolver)
                        }
                    }
                }

                is ManagerLoadGroup -> {
                    KickOp { target, user, kickMemberResolver ->
                        onNotSelf(user) {
                            onNotOwner(user) {
                                onNotManager(user) {
                                    impl.load(target, user, kickMemberResolver)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private class KickOpImpl : KickOp {

    override fun load(target: Span, user: GroupMemberDto, kickMemberResolver: KickMemberResolver) {
        target.badge(BadgeColor.Red) {
            onClickLaunch {
                kickMemberResolver.resolve(user.userId)
            }
            +"踢出"
        }
    }
}