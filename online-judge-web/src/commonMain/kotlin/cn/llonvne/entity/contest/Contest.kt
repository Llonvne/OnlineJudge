package cn.llonvne.entity.contest

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val contestContextJson = Json { encodeDefaults = true }

@Serializable
data class Contest(
    val contestId: Int = -1,
    val ownerId: Int,
    val title: String,
    val description: String = "",
    val contestScoreType: ContestScoreType,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val rankType: ContestRankType,
    val groupId: Int? = null,
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val contextStr: String = ContestContext(listOf()).json(),
    val hashLink: String
) {

    val context: ContestContext = Json.decodeFromString(contextStr)

    /**
     * 定义比赛如何执行记分
     */
    @Serializable
    enum class ContestScoreType {
        ACM, IOI, IO
    }

    @Serializable
    enum class ContestStatus {
        NotBegin, Running, Ended
    }

    @Serializable
    enum class ContestRankType {
        OpenForEveryOne,
        OpenForParticipant,
        OpenForManger,
        OpenForOwner,
        Closed;

        val chinese
            get() = when (this) {
                OpenForEveryOne -> "对所有人开放"
                OpenForParticipant -> "仅对参与者开放"
                OpenForManger -> "仅对比赛管理员开放"
                OpenForOwner -> "仅对所有者开放"
                Closed -> "不开放"
            }
    }

}