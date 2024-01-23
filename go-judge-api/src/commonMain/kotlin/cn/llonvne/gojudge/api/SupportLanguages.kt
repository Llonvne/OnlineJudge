package cn.llonvne.gojudge.api

import kotlinx.serialization.Serializable

@Serializable
enum class SupportLanguages(
    val languageId: Int,
    val languageName: String,
    val languageVersion: String,
    val path: String
) {
    Java(1, "java", "11", "java"),
    Cpp(2, "cpp", "11", "gpp")
}

private val idSet = SupportLanguages.entries.associateBy { it.languageId }

fun SupportLanguages.Companion.fromId(id: Int): SupportLanguages? = idSet[id]