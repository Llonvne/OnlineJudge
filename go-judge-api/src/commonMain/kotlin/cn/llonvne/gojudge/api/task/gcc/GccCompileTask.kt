package cn.llonvne.gojudge.api.task.gcc

import arrow.core.Option
import arrow.core.some
import cn.llonvne.gojudge.api.spec.runtime.Cmd
import cn.llonvne.gojudge.api.spec.runtime.Result
import cn.llonvne.gojudge.api.task.AbstractTask
import cn.llonvne.gojudge.api.task.CodeInput

internal class GccCompileTask : AbstractTask<CodeInput>() {
    override val sourceCodeExtension: Option<String>
        get() = "c".some()
    override val compiledFileExtension: Option<String>
        get() = "out".some()

    override fun getCompileCmd(
        input: CodeInput,
        filenames: Filenames,
    ): Cmd {
        TODO("Not yet implemented")
    }

    override fun getRunCmd(
        input: CodeInput,
        compileResult: Result,
        runFilename: Filename,
        runFileId: String,
    ): Cmd {
        TODO("Not yet implemented")
    }
}
