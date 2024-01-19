package cn.llonvne.entity.problem

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val languageId: Int,
    val languageName: String,
    val languageVersion: String
) {
    override fun toString(): String {
        return "$languageName:$languageVersion"
    }
}
