package cn.llonvne.kvision.service

import cn.llonvne.database.repository.GroupRepository
import cn.llonvne.database.resolver.*
import cn.llonvne.database.resolver.GroupMangerDowngradeResolver.GroupManagerDowngradeResult
import cn.llonvne.database.resolver.GroupMangerDowngradeResolver.GroupManagerDowngradeResult.BeDowngradeUserNotFound
import cn.llonvne.database.resolver.GroupMangerDowngradeResolver.GroupManagerDowngradeResult.DowngradeToIdNotMatchToGroupId
import cn.llonvne.database.resolver.GroupMemberUpgradeResolver.GroupMemberUpgradeResult.*
import cn.llonvne.database.resolver.GroupMemberUpgradeResolver.GroupMemberUpgradeResult.UpdateToIdNotMatchToGroupId
import cn.llonvne.database.resolver.GroupMemberUpgradeResolver.GroupMemberUpgradeResult.UserAlreadyHasThisRole
import cn.llonvne.entity.group.GroupId
import cn.llonvne.entity.role.*
import cn.llonvne.getLogger
import cn.llonvne.kvision.service.IGroupService.*
import cn.llonvne.kvision.service.IGroupService.CreateGroupResp.CreateGroupOk
import cn.llonvne.kvision.service.IGroupService.UpgradeGroupManagerResp.*
import cn.llonvne.security.AuthenticationToken
import cn.llonvne.security.RedisAuthenticationService
import cn.llonvne.security.userRole
import cn.llonvne.track
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class GroupService(
    private val authentication: RedisAuthenticationService,
    private val groupRepository: GroupRepository,
    private val roleService: RoleService,
    private val groupIdResolver: GroupIdResolver,
    private val groupLoadResolver: GroupLoadResolver,
    private val joinGroupResolver: JoinGroupResolver,
    private val groupKickResolver: GroupKickResolver,
    private val memberUpgradeResolver: GroupMemberUpgradeResolver,
    private val groupMangerDowngradeResolver: GroupMangerDowngradeResolver
) : IGroupService {

    private val logger = getLogger()

    override suspend fun createTeam(
        authenticationToken: AuthenticationToken, createGroupReq: CreateGroupReq
    ): CreateGroupResp {
        val user = authentication.validate(authenticationToken) {
            check(CreateGroup.require(createGroupReq.groupType))
        } ?: return PermissionDenied

        if (!groupRepository.shortNameAvailable(createGroupReq.groupShortName)) {
            return GroupShortNameUnavailable(createGroupReq.groupShortName)
        }

        return logger.track(authenticationToken, createGroupReq.groupName, createGroupReq.groupShortName) {
            info("$authenticationToken try to create $createGroupReq")
            val group = groupRepository.create(createGroupReq)
            info("$authenticationToken created ${createGroupReq.groupName},id is ${group.groupId}")

            roleService.addRole(
                user.id, GroupOwner.GroupOwnerImpl(
                    group.groupId ?: return@track InternalError("创建小组后仍然 id 为空")
                )
            )

            return@track CreateGroupOk(group)
        }
    }

    override suspend fun load(
        authenticationToken: AuthenticationToken?, groupId: GroupId
    ): LoadGroupResp = logger.track(authenticationToken, groupId) {
        val id = groupIdResolver.resolve(groupId) ?: return@track GroupIdNotFound(groupId)

        val user = authentication.getAuthenticationUser(authenticationToken)

        return@track groupLoadResolver.resolve(groupId, id, user)
    }

    override suspend fun join(groupId: GroupId, authenticationToken: AuthenticationToken): JoinGroupResp {
        val id = groupIdResolver.resolve(groupId) ?: return GroupIdNotFound(groupId)

        val user = authentication.validate(authenticationToken) { requireLogin() }

        if (user == null) {
            return PermissionDenied
        }

        return joinGroupResolver.resolve(groupId, id, user)
    }

    override suspend fun quit(groupId: GroupId, value: AuthenticationToken?): QuitGroupResp {
        val user = authentication.validate(value) { requireLogin() } ?: return PermissionDenied

        val id = groupIdResolver.resolve(groupId) ?: return GroupIdNotFound(groupId)

        roleService.removeRole(user, user.userRole.groupIdRoles(id))

        return QuitOk
    }

    override suspend fun kick(token: AuthenticationToken?, groupId: GroupId, kickMemberId: Int): KickGroupResp {
        val groupIntId = groupIdResolver.resolve(groupId) ?: return GroupIdNotFound(groupId)

        val kicker = authentication.validate(token) {
            check(KickMember.KickMemberImpl(groupIntId))
        } ?: return PermissionDeniedWithMessage("你没有权限踢人哦")

        return groupKickResolver.resolve(groupId, groupIntId, kicker, kickMemberId)
    }

    override suspend fun upgradeGroupManager(
        token: AuthenticationToken?,
        groupId: GroupId,
        updatee: Int
    ): UpgradeGroupManagerResp {
        val groupIntId = groupIdResolver.resolve(groupId) ?: return GroupIdNotFound(groupId)
        val owner = authentication.validate(token) {
            check(GroupOwner.GroupOwnerImpl(groupIntId))
        } ?: return PermissionDeniedWithMessage("你没有权限升级管理员哦")
        val result =
            memberUpgradeResolver.resolve(groupId, groupIntId, owner, updatee, GroupManager.GroupMangerImpl(groupIntId))
        return when (result) {
            UpdateToIdNotMatchToGroupId -> UpOrDowngradeToIdNotMatchToGroupId(updatee)
            UserAlreadyHasThisRole -> IGroupService.UserAlreadyHasThisRole(updatee)
            BeUpdatedUserNotFound -> BeUpOrDowngradedUserNotfound(updatee)
            Success -> UpgradeManagerOk
        }
    }

    override suspend fun downgradeToMember(
        authenticationToken: AuthenticationToken?,
        groupId: GroupId,
        userId: Int
    ): DowngradeToMemberResp {
        val groupIntid = groupIdResolver.resolve(groupId) ?: return GroupIdNotFound(groupId)
        val owner = authentication.validate(authenticationToken) {
            check(GroupOwner.GroupOwnerImpl(groupIntid))
        } ?: return PermissionDeniedWithMessage("你没有权限降级管理员哦")

        return when (groupMangerDowngradeResolver.resolve(
            groupId,
            groupIntid,
            owner,
            userId,
        )) {
            DowngradeToIdNotMatchToGroupId -> IGroupService.UserAlreadyHasThisRole(userId)
            BeDowngradeUserNotFound -> BeUpOrDowngradedUserNotfound(userId)
            GroupManagerDowngradeResult.Success -> DowngradeToMemberResp.DowngradeToMemberOk(userId)
            GroupManagerDowngradeResult.UserAlreadyHasThisRole -> IGroupService.UserAlreadyHasThisRole(userId)
        }
    }
}