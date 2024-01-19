package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.NotFoundAble
import cn.llonvne.compoent.circleCheck
import cn.llonvne.compoent.loading
import cn.llonvne.compoent.notFound
import cn.llonvne.dtos.SubmissionListDto
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.problem.Submission
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.message.Messager
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.html.*
import io.kvision.navigo.Handler
import io.kvision.routing.Routing
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import io.kvision.utils.obj
import kotlinx.coroutines.launch

fun Container.code(routing: Routing, submissionId: Int) {
    val submissionObservableValue = ObservableValue<ISubmissionService.ViewCodeGetByIdResp?>(null)

    AppScope.launch {
        val result = SubmissionModel.codeGetById(submissionId)
        Messager.toastInfo(result.toString())
        submissionObservableValue.value = result
    }

    div().bind(submissionObservableValue) { submission ->
        if (submission == null) {
            loading()
        } else {
            when (submission) {
                ISubmissionService.LanguageNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                ISubmissionService.ProblemNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                ISubmissionService.SubmissionNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                ISubmissionService.UserNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                is ISubmissionService.ViewCodeGetByIdResp.SuccessfulGetById -> {
                    showStatus(submission.viewCodeDto)
                }
            }
        }
    }
}

fun Container.showStatus(submission: ViewCodeDto) {
    when (submission.status) {
        SubmissionStatus.Received -> circleCheck()
    }
}

fun Container.codeNotfound(type: ISubmissionService.ViewCodeGetByIdResp, id: Int) {
    notFound(object : NotFoundAble {
        override val header: String
            get() = "代码走丢啦"
        override val notice: String
            get() = "请检查你的提交ID是否正确，如果确认ID正确，但无法打开代码，请联系我们 ^_^"
        override val errorCode: String
            get() = "ErrorCode:${type}-${id}"
    })
}