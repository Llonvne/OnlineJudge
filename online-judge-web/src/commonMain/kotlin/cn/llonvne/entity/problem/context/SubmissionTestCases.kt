package cn.llonvne.entity.problem.context

import kotlinx.serialization.Serializable

@Serializable
data class SubmissionTestCases(val testCases: List<SubmissionTestCase>) {
    @Serializable
    data class SubmissionTestCase(
        override val id: String,
        override val name: String,
        override val input: String,
        override val output: String,
        override val visibility: TestCaseType,
    ) : TestCase
}

