package cn.llonvne.entity.problem

import cn.llonvne.entity.problem.context.ProblemTestCases
import cn.llonvne.entity.problem.context.SubmissionTestCases
import cn.llonvne.entity.problem.context.passer.PasserResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json

@Serializable
sealed interface JudgeResult {
    fun json() = json.encodeToString<JudgeResult>(this)
}

@Serializable
data class PlaygroundJudgeResult(
    val submissionTestCases: SubmissionTestCases
) : JudgeResult {
    val output = submissionTestCases.testCases.first().originOutput
}

@Serializable
data class ProblemJudgeResult(
    val problemTestCases: ProblemTestCases,
    val submissionTestCases: SubmissionTestCases,
    val passerResult: PasserResult
) : JudgeResult