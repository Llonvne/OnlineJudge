package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.NotFoundAble
import cn.llonvne.compoent.navigateButton
import cn.llonvne.compoent.notFound
import cn.llonvne.compoent.observable.observableProblemOf
import cn.llonvne.compoent.observable.observableSupportLanguagesList
import cn.llonvne.constants.Frontend
import cn.llonvne.kvision.service.ICodeService.SaveCodeResp.SuccessfulSaveCode
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult
import cn.llonvne.kvision.service.IProblemService.ProblemGetByIdResult.Ok
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.message.Messager
import cn.llonvne.model.CodeModel
import cn.llonvne.model.Storage
import io.kvision.core.*
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.TextArea
import io.kvision.html.*
import io.kvision.routing.Routing
import io.kvision.state.bind
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class SubmissionSubmit(
    val languageId: String?,
    val code: String?
)

fun Container.detail(routing: Routing, id: Int) {

    val alert = div { }

    observableProblemOf(id) { problem ->
        div(className = "row") {
            div(className = "col") {
                div().bind(problem) {
                    if (it != null) {
                        when (it) {
                            is Ok -> {
                                h1 {
                                    +it.problem.problemName
                                }
                                p {
                                    +it.problem.problemDescription
                                }
                                navigateButton(routing, Frontend.Index)
                            }

                            ProblemGetByIdResult.ProblemNotFound -> {
                                notFound(object : NotFoundAble {
                                    override val header: String
                                        get() = "题目未找到"
                                    override val notice: String
                                        get() = "请确认题目ID正确，如果确认题目ID正确，请联系我们 ^_^"
                                    override val errorCode: String = "$it-$id"
                                })
                            }
                        }
                    }
                }
            }
            div(className = "col") {

                observableSupportLanguagesList(id) { supportLanguages ->
                    formPanel<SubmissionSubmit>(className = "row").bind(supportLanguages) { languages ->
                        "row gy-2 gx-3 align-items-center p-1".split(" ").forEach {
                            addCssClass(it)
                        }

                        var code by Storage.remember("", "code")

                        add(SubmissionSubmit::languageId, TomSelect(options = languages.map {
                            it.languageId.toString() to it.toString()
                        }) {
                            multiple = false
                            this.placeholder = "请选择语言"
                            label = null
                            maxWidth = 200.px
                        })

                        button("提交", style = ButtonStyle.SUCCESS) {
                            onClick {
                                val data = getData()
                                Messager.toastInfo(data.languageId.toString())
                            }

                            maxWidth = 100.px
                            marginTop = 15.px

                            color = Color.name(Col.WHITE)
                        }

                        button("分享代码", style = ButtonStyle.LINK) {
                            maxWidth = 100.px
                            marginTop = 15.px

                            enableTooltip(
                                options = TooltipOptions(
                                    "通过 Online Judge 分享你的代码(你的代码将被上传)",
                                    animation = true
                                )
                            )

                            onClick {
                                val data = getData()

                                if (data.code == null) {
                                    Messager.toastError("分享的代码不可为空")
                                    return@onClick
                                }

                                suspend fun shareCode(languageId: Int?) {
                                    when (val resp = CodeModel.saveCode(data.code, languageId)) {
                                        LanguageNotFound -> {
                                            Messager.toastError("所选语言无效")
                                        }

                                        PermissionDenied -> {
                                            Messager.toastError("权限不足，请先登入")
                                        }

                                        is SuccessfulSaveCode -> {
                                            Messager.toastInfo("分享成功，代码号:${resp.code.codeId}")
                                        }
                                    }
                                }

                                AppScope.launch {
                                    if (data.languageId != null) {
                                        val languageId =
                                            data.languageId.toIntOrNull()
                                                ?: return@launch Messager.toastError("所选语言无效")
                                        shareCode(languageId)
                                    } else {
                                        shareCode(null)
                                    }
                                }
                            }
                        }

                        add(SubmissionSubmit::code, TextArea(value = code) {
                            onInput {
                                code = this.value ?: ""
                            }

                            this.cols = 70
                            this.rows = 20

                        }, required = true, requiredMessage = "提交代码不可为空")
                    }
                }

            }
        }
    }
}

