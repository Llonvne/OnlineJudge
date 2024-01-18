package cn.llonvne.database.entity.def.problem

import cn.llonvne.entity.problem.Problem
import org.komapper.annotation.*

@KomapperEntityDef(Problem::class)
private data class ProblemDef(

    // 题目 ID
    @KomapperId @KomapperAutoIncrement
    val problemId: Nothing,
    // 作者 ID
    val authorId: Nothing,

    // 题目名字
    val problemName: Nothing,
    // 题目描述
    val problemDescription: Nothing,
    // 时间限制
    val timeLimit: Nothing,
    // 内存限制
    val memoryLimit: Nothing,

    //--- 数据库信息区 ---//
    @KomapperVersion
    val version: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing,
)