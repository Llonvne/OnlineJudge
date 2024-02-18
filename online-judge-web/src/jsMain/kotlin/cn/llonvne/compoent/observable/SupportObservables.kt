package cn.llonvne.compoent.observable

import cn.llonvne.entity.problem.Language
import cn.llonvne.kvision.service.ISubmissionService
import cn.llonvne.message.Messager
import cn.llonvne.model.SubmissionModel

fun observableSupportLanguagesList(
    problemId: Int,
    action: (ObservableListDsl<Language>) -> Unit
) {
    observableListOf {
        setUpdater {
            SubmissionModel.getSupportLanguage(problemId).let { resp ->
                when (resp) {
                    ISubmissionService.ProblemNotFound -> {
                        Messager.toastError("无法找到题目ID为:$problemId")
                        listOf()
                    }

                    is ISubmissionService.GetSupportLanguageByProblemIdResp.SuccessfulGetSupportLanguage -> {
                        clear()
                        resp.languages
                    }
                }
            }
        }

        action(this)
    }
}