package cn.llonvne.database.entity.def.problem

import cn.llonvne.entity.problem.Language
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId

@KomapperEntityDef(entity = Language::class)
private data class LanguageDef(
    @KomapperId @KomapperAutoIncrement
    val languageId: Int,
    val languageName: String,
    val languageVersion: String
)