package cn.llonvne.entity.problem.context

import cn.llonvne.entity.problem.context.ProblemTestCases.ProblemTestCase
import cn.llonvne.gojudge.api.task.Output
import kotlinx.serialization.Serializable

@Serializable
data class SubmissionTestCases(
    val testCases: List<SubmissionTestCase>,
) {
    val showOnJudgeResultDisplay
        get() =
            testCases.filter {
                it.visibility in
                    setOf(
                        TestCaseType.ViewAndJudge,
                    )
            }

    @Serializable
    data class SubmissionTestCase(
        override val id: String,
        override val name: String,
        override val input: String,
        override val expect: String,
        override val visibility: TestCaseType,
        val originOutput: Output,
        val outputStr: String?,
    ) : TestCase {
        companion object {
            fun from(
                problemTestCase: ProblemTestCase,
                output: Output,
            ): SubmissionTestCase =
                SubmissionTestCase(
                    id = problemTestCase.id,
                    name = problemTestCase.name,
                    input = problemTestCase.input,
                    expect = problemTestCase.expect,
                    visibility = problemTestCase.visibility,
                    originOutput = output,
                    outputStr =
                        when (output) {
                            is Output.Success ->
                                output.runResult.files
                                    ?.get("stdout")
                                    .toString()
                            else -> null
                        },
                )
        }
    }
}
