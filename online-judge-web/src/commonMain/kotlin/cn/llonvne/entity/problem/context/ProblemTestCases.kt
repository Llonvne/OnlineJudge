package cn.llonvne.entity.problem.context

import cn.llonvne.entity.problem.context.passer.PasserResult
import cn.llonvne.entity.problem.context.passer.ProblemPasser
import kotlinx.serialization.Serializable

@Serializable
data class ProblemTestCases(
    val testCases: List<ProblemTestCase>,
    val passer: ProblemPasser<PasserResult>,
) {
    @Serializable
    data class ProblemTestCase(
        override val id: String,
        override val name: String,
        override val input: String,
        override val expect: String,
        override val visibility: TestCaseType,
    ) : TestCase

    fun canShow() =
        testCases.filter {
            it.visibility in
                setOf(
                    TestCaseType.OnlyForView,
                    TestCaseType.ViewAndJudge,
                )
        }

    fun canJudge() =
        testCases.filter {
            it.visibility in
                setOf(
                    TestCaseType.ViewAndJudge,
                    TestCaseType.OnlyForJudge,
                )
        }
}
