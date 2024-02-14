package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.compoent.AlertType
import cn.llonvne.compoent.alert
import cn.llonvne.gojudge.api.SupportLanguages
import cn.llonvne.kvision.service.ISubmissionService.GetLastNPlaygroundSubmissionResp.SuccessGetLastNPlaygroundSubmission
import cn.llonvne.kvision.service.InternalError
import cn.llonvne.kvision.service.LanguageNotFound
import cn.llonvne.kvision.service.PermissionDenied
import cn.llonvne.ll
import cn.llonvne.message.Messager
import cn.llonvne.model.RoutingModule
import cn.llonvne.model.SubmissionModel
import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.form.formPanel
import io.kvision.form.select.TomSelect
import io.kvision.form.text.TextArea
import io.kvision.html.*
import io.kvision.utils.px
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class PlaygroundSubmission(
    val language: String, val code: String, val stdin: String? = ""
)

fun Container.playground() {

    alert(AlertType.Light) {
        h1 { +"代码训练场" }

        p {
            +"*训练场提交的代码会永久在服务器保存，且不可删除。请注意保护个人隐私，不要上传任何有关个人隐私的内容"
        }
    }

    div(className = "row") {
        div(className = "col") {
            alert(AlertType.Secondary) {
                val form = formPanel<PlaygroundSubmission> {
                    add(PlaygroundSubmission::language, TomSelect(
                        options = SupportLanguages.entries.map {
                            it.languageId.toString() to "${it.languageName} ${it.languageVersion}"
                        }, label = "语言"
                    ) {
                        maxWidth = 300.px
                    })

                    add(PlaygroundSubmission::stdin, TextArea(label = "标准输入"))

                    add(PlaygroundSubmission::code, TextArea(label = "代码") {
                        rows = 11
                    })

                }

                button("提交") {
                    onClick {
                        AppScope.launch {
                            val data = form.getData()

                            val resp = SubmissionModel.submit(
                                PlaygroundSubmission(data.language, data.code, data.stdin)
                            )

                            Messager.toastInfo(resp.toString())
                        }
                    }
                }
            }
        }
        div(className = "col") {

            h4 { +"最近一次运行结果" }
            val lastRun = div { }

            h4 { +"历史记录" }

            alert(AlertType.Dark) {

                AppScope.launch {
                    when (val resp = SubmissionModel.getLastNPlaygroundSubmission()) {
                        is InternalError -> {
                            Messager.toastError("内部错误:${resp.reason}")
                        }

                        LanguageNotFound -> {
                            Messager.toastError(resp.toString())
                        }

                        PermissionDenied -> {
                            Messager.toastError("你还为登入，无法获得历史记录")
                        }

                        is SuccessGetLastNPlaygroundSubmission -> {

                            Messager.toastInfo(resp.subs.toString())

                            val sortByTime = resp.subs.sortedBy {
                                it.submitTime
                            }

                            val first = sortByTime.firstOrNull()

                            if (first != null) {
                                lastRun.onClick {
                                    RoutingModule.routing.navigate("/share/${first.codeId}")
                                }
                            }

                            sortByTime.forEach { dto ->
                                alert(AlertType.Light) {
                                    link("训练场提交-语言:${dto.language}-时间:${dto.submitTime.ll()}") {
                                        onClick {
                                            RoutingModule.routing.navigate("/share/${dto.codeId}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

}