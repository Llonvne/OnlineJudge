package cn.llonvne.database.entity

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId

@KomapperEntity
data class ProblemSupportLanguage(
    @KomapperId @KomapperAutoIncrement
    val problemSupportId: Int? = null,
    val problemId: Int,
    val languageId: Int
)

