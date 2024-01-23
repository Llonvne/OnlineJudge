package cn.llonvne.database.entity.def

import cn.llonvne.entity.problem.share.Code
import cn.llonvne.entity.problem.share.CodeCommentType
import org.komapper.annotation.*

@KomapperEntityDef(entity = Code::class)
private data class CodeDef(
    @KomapperId @KomapperAutoIncrement
    val codeId: Nothing,

    val authenticationUserId: Nothing,

    val code: Nothing,
    val languageId: Nothing,

    val codeType: Nothing,

    val visibilityType: Nothing,
    val commentType: CodeCommentType,
    val hashLink: String? = null,

    //--- 数据库信息区 ---//
    @KomapperVersion
    val version: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing
)