package cn.llonvne.database.entity.def.problem.tag

import cn.llonvne.entity.problem.ProblemTag
import cn.llonvne.entity.types.badge.BadgeColor
import kotlinx.datetime.LocalDateTime
import org.komapper.annotation.*

@KomapperEntityDef(ProblemTag::class)
private data class ProblemTagDef(
    @KomapperId @KomapperAutoIncrement
    val problemTagId: Nothing,
    val problemId: Int,
    val tag: String,
    val color: BadgeColor,
    // --- 数据库信息区 ---//
    @KomapperVersion
    val version: Int? = null,
    @KomapperCreatedAt
    val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt
    val updatedAt: LocalDateTime? = null,
)
