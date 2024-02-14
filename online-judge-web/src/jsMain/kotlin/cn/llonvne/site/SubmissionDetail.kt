package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.*
import cn.llonvne.dtos.ViewCodeDto
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.kvision.service.CodeNotFound
import cn.llonvne.kvision.service.ISubmissionService.*
import cn.llonvne.kvision.service.ISubmissionService.ViewCodeGetByIdResp.SuccessfulGetById
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.html.code
import io.kvision.html.div
import io.kvision.html.h4
import io.kvision.routing.Routing
import io.kvision.state.ObservableValue
import io.kvision.state.bind
import kotlinx.coroutines.launch

fun Container.submissionDetail(routing: Routing, submissionId: Int) {
    val submissionObservableValue = ObservableValue<ViewCodeGetByIdResp?>(null)

    val alert = div {}
    add(alert)

    AppScope.launch {
        val result = runCatching {
            SubmissionModel.codeGetById(submissionId)
        }.onFailure {
            alert.alert(AlertType.Danger) {
                h4 { +"请检查你的网络设置" }
            }
        }
        submissionObservableValue.value = result.getOrNull()
    }

    div().bind(submissionObservableValue) { submission ->
        if (submission == null) {
            loading()
        } else {
            when (submission) {
                LanguageNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                ProblemNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                SubmissionNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                UserNotFound -> {
                    codeNotfound(submission, submissionId)
                }

                is SuccessfulGetById -> {
                    showStatus(submission.viewCodeDto)
                }

                CodeNotFound -> {
                    codeNotfound(submission, submissionId)
                }
            }
        }
    }
}

fun Container.showStatus(submission: ViewCodeDto) {
    when (submission.status) {
        SubmissionStatus.Received -> circleCheck()
        SubmissionStatus.Finished -> circleCheck()
    }

    code(submission.rawCode)
}

fun Container.codeNotfound(type: ViewCodeGetByIdResp, id: Int) {
    notFound(object : NotFoundAble {
        override val header: String
            get() = "代码走丢啦"
        override val notice: String
            get() = "请检查你的提交ID是否正确，如果确认ID正确，但无法打开代码，请联系我们 ^_^"
        override val errorCode: String
            get() = "ErrorCode:${type}-${id}"
    })
}