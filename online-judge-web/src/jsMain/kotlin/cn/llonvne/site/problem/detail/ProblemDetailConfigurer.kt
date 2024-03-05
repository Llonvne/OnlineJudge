package cn.llonvne.site.problem.detail

import cn.llonvne.compoent.submission.SubmitProblemResolver
import cn.llonvne.site.problem.detail.CodeEditorShower.Companion.CodeEditorConfigurer

class ProblemDetailConfigurer {
    var disableHistory: Boolean = false

    var submitProblemResolver: SubmitProblemResolver = SubmitProblemResolver()

    var codeEditorConfigurer: CodeEditorConfigurer =
        CodeEditorConfigurer()
}