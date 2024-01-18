package cn.llonvne.entity.problem

import cn.llonvne.entity.types.BadgeColor
import cn.llonvne.entity.types.BadgeColorGetter
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ProblemTag(
    val problemTagId: Int,
    val problemId: Int,
    val tag: String,
    override val color: BadgeColor,

    //--- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) : BadgeColorGetter

