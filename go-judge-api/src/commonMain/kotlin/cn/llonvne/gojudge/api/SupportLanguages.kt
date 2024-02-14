package cn.llonvne.gojudge.api

import kotlinx.serialization.Serializable

sealed interface ISupportLanguages {
    val languageId: Int
    val languageName: String
    val languageVersion: String
    val path: String
}

@Serializable
enum class SupportLanguages(
    override val languageId: Int,
    override val languageName: String,
    override val languageVersion: String,
    override val path: String
) : ISupportLanguages {
    Java(1, "java", "11", "java"),
    Python3(2, "python", "3", "python3"),
    Kotlin(3, "kotlin", "1.9.20", "kotlin"),
    Cpp11(4, "cpp", "11", "gpp11"),
    Cpp14(5, "cpp", "14", "gpp14"),
    Cpp17(6, "cpp", "17", "gpp17"),
    Cpp98(7, "cpp", "98", "gpp98"),
    Cpp20(8, "cpp", "20", "gpp20"),
    Cpp23(9, "cpp", "23", "gpp23"),
}

private val idSet = SupportLanguages.entries.associateBy { it.languageId }

fun SupportLanguages.Companion.fromId(id: Int): SupportLanguages? = idSet[id]