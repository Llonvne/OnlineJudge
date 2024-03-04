package cn.llonvne.model

import cn.llonvne.entity.problem.context.passer.PasserResult.BooleanResult
import cn.llonvne.entity.problem.share.Code
import cn.llonvne.kvision.service.*
import cn.llonvne.kvision.service.ISubmissionService.*
import cn.llonvne.kvision.service.ISubmissionService.CreateSubmissionReq.PlaygroundCreateSubmissionReq
import cn.llonvne.kvision.service.ISubmissionService.ProblemSubmissionResp.ProblemSubmissionRespImpl
import cn.llonvne.message.Message
import cn.llonvne.message.Messager
import cn.llonvne.site.PlaygroundSubmission
import io.kvision.remote.getService

object SubmissionModel {
    private val submissionService = getService<ISubmissionService>()

    suspend fun list() = submissionService.list(
        ListSubmissionReq(AuthenticationModel.userToken.value)
    )

    suspend fun getById(id: Int): SubmissionGetByIdResp = submissionService.getById(id)

    suspend fun codeGetById(id: Int) = submissionService.getViewCode(id)

    suspend fun getSupportLanguage(problemId: Int) =
        submissionService.getSupportLanguageId(AuthenticationModel.userToken.value, problemId)

    suspend fun submit(playgroundSubmission: PlaygroundSubmission): CreateSubmissionResp {
        return submissionService.create(
            AuthenticationModel.userToken.value,
            PlaygroundCreateSubmissionReq(
                languageId = playgroundSubmission.language.toInt(),
                rawCode = playgroundSubmission.code,
                stdin = playgroundSubmission.stdin ?: "",
                codeType = Code.CodeType.Playground
            )
        )
    }

    suspend fun getJudgeResultByCodeID(codeId: Int) =
        submissionService.getOutputByCodeId(AuthenticationModel.userToken.value, codeId)

    suspend fun getLastNPlaygroundSubmission(lastN: Int = 5) =
        submissionService.getLastNPlaygroundSubmission(
            AuthenticationModel.userToken.value, lastN
        )

    suspend fun getLastNProblemSubmission(
        problemId: Int,
        lastN: Int = 5
    ) = submissionService.getLastNProblemSubmission(
        AuthenticationModel.userToken.value, problemId, lastN
    )


    suspend fun submit(
        problemSubmissionReq: ProblemSubmissionReq,
        onSuccess: suspend (ProblemSubmissionRespImpl) -> Unit
    ) {
        when (val resp = submissionService.submit(AuthenticationModel.userToken.value, problemSubmissionReq)) {
            LanguageNotFound -> Messager.toastInfo("提交的语言不受支持，请刷新网页重新提交")
            PermissionDenied -> Messager.toastInfo("你还未登入，或者登入已经过期")
            is PermissionDeniedWithMessage -> Messager.toastInfo(resp.message)
            ProblemNotFound -> Messager.toastInfo("你提交的题目不存在或者设置了权限")
            is ProblemSubmissionRespImpl -> onSuccess(resp)
            is InternalError -> Messager.toastInfo(resp.reason)
        }
    }
}