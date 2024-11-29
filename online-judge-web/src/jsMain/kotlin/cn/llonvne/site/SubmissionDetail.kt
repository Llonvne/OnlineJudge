package cn.llonvne.site

import cn.llonvne.compoent.NotFoundAble
import cn.llonvne.compoent.circleCheck
import cn.llonvne.compoent.loading
import cn.llonvne.compoent.notFound
import cn.llonvne.compoent.observable.observableOf
import cn.llonvne.dtos.CodeForView
import cn.llonvne.entity.problem.SubmissionStatus
import cn.llonvne.kvision.service.CodeNotFound
import cn.llonvne.kvision.service.ISubmissionService.*
import cn.llonvne.kvision.service.ISubmissionService.ViewCodeGetByIdResp.SuccessfulGetById
import cn.llonvne.kvision.service.JudgeResultParseError
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.html.code
import io.kvision.html.div
import io.kvision.routing.Routing
import io.kvision.state.bind

fun Container.submissionDetail(
    routing: Routing,
    submissionId: Int,
) {
    val alert = div {}
    add(alert)

    observableOf<ViewCodeGetByIdResp?>(null) {
        setUpdater {
            SubmissionModel.codeGetById(submissionId)
        }

        div().bind(this) { submission ->

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
                        showStatus(submission.codeForView)
                    }

                    CodeNotFound -> {
                        codeNotfound(submission, submissionId)
                    }

                    JudgeResultParseError -> {
                        codeNotfound(submission, submissionId)
                    }
                }
            }
        }
    }
}

fun Container.showStatus(submission: CodeForView) {
    when (submission.status) {
        SubmissionStatus.Received -> circleCheck()
        SubmissionStatus.Finished -> circleCheck()
    }

    code(submission.rawCode)
}

fun Container.codeNotfound(
    type: ViewCodeGetByIdResp,
    id: Int,
) {
    notFound(
        object : NotFoundAble {
            override val header: String
                get() = "代码走丢啦"
            override val notice: String
                get() = "请检查你的提交ID是否正确，如果确认ID正确，但无法打开代码，请联系我们 ^_^"
            override val errorCode: String
                get() = "ErrorCode:$type-$id"
        },
    )
}
