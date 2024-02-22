package cn.llonvne.entity.problem.context

import cn.llonvne.entity.problem.context.ProblemTestCases.*
import cn.llonvne.gojudge.api.task.Output
import kotlinx.serialization.Serializable

@Serializable
data class SubmissionTestCases(val testCases: List<SubmissionTestCase>) {
    @Serializable
    data class SubmissionTestCase(
        override val id: String,
        override val name: String,
        override val input: String,
        override val expect: String,
        override val visibility: TestCaseType,
        val originOutput: Output,
        val outputStr: String?
    ) : TestCase {

        companion object {
            fun from(
                problemTestCase: ProblemTestCase,
                problem: Problem,
                output: Output
            ): SubmissionTestCase {
                return SubmissionTestCase(
                    id = problemTestCase.id,
                    name = problemTestCase.name,
                    input = problemTestCase.input,
                    expect = problemTestCase.expect,
                    visibility = problemTestCase.visibility,
                    originOutput = output,
                    outputStr = when (output) {
                        is Output.Success -> output.runResult.files?.get("stdout").toString()
                        else -> null
                    }
                )
            }
        }
    }
}

