package cn.llonvne.kvision.service

import cn.llonvne.entity.ModifyUserForm
import cn.llonvne.entity.role.IUserRole
import cn.llonvne.security.Token
import io.kvision.annotations.KVService
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@KVService
interface IMineService {
    @Serializable
    sealed interface DashboardResp {
        @Serializable
        data class OnlineJudgeStatistics(
            val totalUserCount: Int,
            val totalSubmissionToday: Int,
            val totalContestLastTwoWeek: Int
        )

        @Serializable
        data class BackendInfo(
            val name: String,
            val host: String,
            val port: String,
            val cpuCoresCount: Int,
            val cpuUsage: Double,
            val totalMemory: Int,
            val usedMemory: Int,
            val isOnline: Boolean
        )

        @Serializable
        data class JudgeServerInfo(
            val name: String,
            val host: String,
            val port: String,
            val cpuCoresCount: Int,
            val cpuUsage: Double,
            val memoryUsage: Int,
            val isOnline: Boolean
        )

        @Serializable
        data class DashboardRespImpl(
            val statistics: OnlineJudgeStatistics,
            val backendInfo: BackendInfo,
            val judgeServerInfo: JudgeServerInfo
        ) : DashboardResp
    }

    suspend fun dashboard(token: Token?): DashboardResp

    @Serializable
    sealed interface UsersResp {
        @Serializable
        data class UsersRespImpl(
            val users: List<UserManageListUserDto>
        ) : UsersResp

        @Serializable
        data class UserManageListUserDto(
            val userId: Int,
            val username: String,
            val createAt: LocalDateTime,
            val userRole: IUserRole
        )
    }

    suspend fun users(): UsersResp
    suspend fun deleteUser(value: Token?, id: Int): Boolean
    suspend fun modifyUser(value: Token?, result: ModifyUserForm): Boolean
}