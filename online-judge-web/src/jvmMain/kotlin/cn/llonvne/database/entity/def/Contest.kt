package cn.llonvne.database.entity.def

import cn.llonvne.entity.contest.Contest
import kotlinx.datetime.LocalDateTime
import org.komapper.annotation.*

@KomapperEntityDef(entity = Contest::class)
private data class ContestDef(
    @KomapperId @KomapperAutoIncrement
    val contestId: Int = -1,
    val ownerId: Int,
    val title: String,
    val description: String = "",
    val contestScoreType: Contest.ContestScoreType,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val rankType: Contest.ContestRankType,
    val groupId: Int? = null,
    val contextStr: String,
    val hashLink: String,
    @KomapperVersion
    val version: Int? = null,
    @KomapperCreatedAt
    val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt
    val updatedAt: LocalDateTime? = null
)