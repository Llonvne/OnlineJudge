package cn.llonvne.entity.problem.context

import kotlinx.serialization.Serializable

@Serializable
sealed interface TestCase {
    val id: String
    val name: String
    val input: String
    val expect: String
    val visibility: TestCaseType
}
