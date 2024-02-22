package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.*
import cn.llonvne.compoent.observable.observableProblemOf
import cn.llonvne.compoent.observable.observableSupportLanguagesList
import cn.llonvne.compoent.submission.SubmitProblemSolutionResolver
import cn.llonvne.constants.Frontend
import cn.llonvne.entity.problem.SubmissionVisibilityType
import cn.llonvne.kvision.service.ICodeService.SaveCodeResp.SuccessfulSaveCode
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.Ok
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.ProblemNotFound
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.ll
import cn.llonvne.message.Messager
import cn.llonvne.model.CodeModel
import cn.llonvne.model.Storage
import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.TextArea
import io.kvision.html.*
import io.kvision.modal.Dialog
import io.kvision.routing.Routing
import io.kvision.state.bind
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class SubmissionSubmit(
    val languageId: String?,
    val code: String?,
    val visibilityTypeStr: String
)


fun detail(root: Container, routing: Routing, problemId: Int) {

}

fun interface ProblemDetailShower {
    fun show(root: Container)

    companion object {

        private fun problemNotFoundDetailShower(problemId: Int) = ProblemDetailShower { root ->
            root.notFound(object : NotFoundAble {
                override val header: String
                    get() = "题目未找到"
                override val notice: String
                    get() = "请确认题目ID正确，如果确认题目ID正确，请联系我们 ^_^"
                override val errorCode: String = "ProblemNotFound-$problemId"
            })
        }

        fun from(problemId: Int, resp: ProblemGetByIdResult) {
            when (resp) {
                is Ok -> TODO()
                ProblemNotFound -> problemNotFoundDetailShower(problemId = problemId)
            }
        }
    }
}


fun Container.detail(routing: Routing, id: Int) {
    observableProblemOf(id) { resp ->
        div { }.bind(resp) { problem ->
            when (problem) {
                is Ok -> {
                    alert(AlertType.Light) {
                        h1 {
                            +problem.problem.problemName
                        }
                        p {
                            +problem.problem.problemDescription
                        }

                        badges {
                            add {
                                +"内存限制:${problem.problem.memoryLimit}"
                            }

                            add {
                                +"时间限制:${problem.problem.timeLimit}"
                            }

                            add {
                                +"最后更新于:${problem.problem.updatedAt?.ll()}"
                            }
                        }
                    }
                }

                ProblemNotFound -> {
                    notFound(object : NotFoundAble {
                        override val header: String
                            get() = "题目未找到"
                        override val notice: String
                            get() = "请确认题目ID正确，如果确认题目ID正确，请联系我们 ^_^"
                        override val errorCode: String = "$problem-$id"
                    })
                }

                null -> {}
            }
        }

        div(className = "row") {
            div(className = "col") {
                alert(AlertType.Light) {
                    h4 {
                        +"题目任务"
                    }

                    p {

                    }
                }
            }

            div(className = "col") { }
        }


//        div(className = "col") {
//            observableSupportLanguagesList(id) { supportLanguages ->
//                val result = Dialog("提交") {
//                    formPanel<SubmissionSubmit>(className = "row").bind(supportLanguages) { languages ->
//                        "row gy-2 gx-3 align-items-center p-1".split(" ").forEach {
//                            addCssClass(it)
//                        }
//
//                        var code by Storage.remember("", "code")
//
//                        add(SubmissionSubmit::languageId, TomSelect(options = languages.map {
//                            it.languageId.toString() to it.toString()
//                        }) {
//                            multiple = false
//                            this.placeholder = "请选择语言"
//                            label = null
//                            maxWidth = 200.px
//                        })
//
//                        button("提交", style = ButtonStyle.SUCCESS) {
//                            onClick {
//                                val data = getData()
//                                setResult(data)
//                            }
//
//                            maxWidth = 100.px
//
//                            color = Color.name(Col.WHITE)
//                        }
//
//                        button("分享代码", style = ButtonStyle.LINK) {
//                            maxWidth = 100.px
//                            marginTop = 15.px
//
//                            enableTooltip(
//                                options = TooltipOptions(
//                                    "通过 Online Judge 分享你的代码(你的代码将被上传)",
//                                    animation = true
//                                )
//                            )
//
//                            onClick {
//                                val data = getData()
//
//                                if (data.code == null) {
//                                    Messager.toastError("分享的代码不可为空")
//                                    return@onClick
//                                }
//
//                                suspend fun shareCode(languageId: Int?) {
//                                    when (val resp = CodeModel.saveCode(data.code, languageId)) {
//                                        LanguageNotFound -> {
//                                            Messager.toastError("所选语言无效")
//                                        }
//
//                                        PermissionDenied -> {
//                                            Messager.toastError("权限不足，请先登入")
//                                        }
//
//                                        is SuccessfulSaveCode -> {
//                                            Messager.toastInfo("分享成功，代码号:${resp.code.codeId}")
//                                        }
//                                    }
//                                }
//
//                                AppScope.launch {
//                                    if (data.languageId != null) {
//                                        val languageId =
//                                            data.languageId.toIntOrNull()
//                                                ?: return@launch Messager.toastError("所选语言无效")
//                                        shareCode(languageId)
//                                    } else {
//                                        shareCode(null)
//                                    }
//                                }
//                            }
//                        }
//
//                        add(SubmissionSubmit::code, TextArea(value = code) {
//                            onInput {
//                                code = this.value ?: ""
//                            }
//
//                            this.cols = 70
//                            this.rows = 14
//
//                        }, required = true, requiredMessage = "提交代码不可为空")
//
//                        add(SubmissionSubmit::visibilityTypeStr, TomSelect(
//                            options = SubmissionVisibilityType.entries.map {
//                                it.ordinal.toString() to it.chinese
//                            }
//                        ) {
//                            label = "提交可见性"
//                            placeholder = "请选择提交可见性"
//                            margin = 0.px
//                        })
//                    }
//                }
//
//                button("提交") {
//                    onClickLaunch {
//                        val result = result.getResult()
//                    }
//                }
//            }
//        }
    }
}



