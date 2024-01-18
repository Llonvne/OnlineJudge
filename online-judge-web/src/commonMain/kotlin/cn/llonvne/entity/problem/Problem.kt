package cn.llonvne.entity.problem

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Problem(
    // 题目 ID
    val problemId: Int? = null,
    // 作者 ID
    val authorId: Int,

    // 题目名字
    val problemName: String,
    // 题目描述
    val problemDescription: String,
    // 时间限制
    val timeLimit: Long,
    // 内存限制
    val memoryLimit: Long,

    //--- 数据库信息区 ---//
    val version: Int? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {
    companion object
}



