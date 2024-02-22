package cn.llonvne.entity.problem.context.passer

import cn.llonvne.entity.problem.context.SubmissionTestCases
import cn.llonvne.entity.problem.context.passer.PasserResult.BooleanResult
import kotlinx.serialization.Serializable

/**
 * 定义题目任何算通过
 */
@Serializable
sealed interface ProblemPasser<out R : PasserResult> {

    val description: String

    /**
     * 通过测试结果集合判断题目是否通过测试，并获得测试结果
     */
    fun pass(submissionTestCases: SubmissionTestCases): R


    /**
     * 通过所有测试
     */
    @Serializable
    data object PassAllCases : ProblemPasser<BooleanResult> {
        override fun pass(submissionTestCases: SubmissionTestCases): BooleanResult {
            TODO("Not yet implemented")
        }

        override val description: String
            get() = "需通过所有测试才算题目通过"
    }

    /**
     * 通过一定程度
     */
    @Serializable
    data class OfPassRate(val rate: Double) : ProblemPasser<BooleanResult> {
        override fun pass(submissionTestCases: SubmissionTestCases): BooleanResult {
            TODO()
        }

        override val description: String
            get() = "通过 $rate 比例测试即可算题目通过"
    }

}