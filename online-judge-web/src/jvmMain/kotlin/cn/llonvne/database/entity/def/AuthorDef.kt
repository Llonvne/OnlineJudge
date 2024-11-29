package cn.llonvne.database.entity.def

import cn.llonvne.entity.Author
import org.komapper.annotation.*

@KomapperEntityDef(Author::class)
private data class AuthorDef(
    @KomapperId @KomapperAutoIncrement
    val authorId: Nothing,
    val authorName: Nothing,
    val introduction: Nothing,
    val authenticationUserId: Int? = null,
    // --- 数据库信息区 ---//
    @KomapperVersion
    val version: Nothing,
    @KomapperCreatedAt
    val createdAt: Nothing,
    @KomapperUpdatedAt
    val updatedAt: Nothing,
)
