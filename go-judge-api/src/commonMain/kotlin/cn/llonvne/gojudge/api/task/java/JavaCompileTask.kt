package cn.llonvne.gojudge.api.task.java

import arrow.core.Option
import arrow.core.some
import cn.llonvne.gojudge.api.spec.runtime.*
import cn.llonvne.gojudge.api.task.AbstractTask
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.services.runtime.cmd
import cn.llonvne.gojudge.services.runtime.useUsrBinEnv

class JavaCompileTask : AbstractTask<CodeInput>() {
    override val sourceCodeExtension: Option<String>
        get() = "java".some()
    override val compiledFileExtension: Option<String>
        get() = "class".some()

    override fun getCompileCmd(input: CodeInput, filenames: Filenames) = cmd {
        args = useJavacArgs(filenames)
        env = useUsrBinEnv
        files = useStdOutErrForFiles()
        copyIn = useMemoryCodeCopyIn(filenames.source.asString(), input.code)
        copyOut = useStdOutErrForCopyOut()
        copyOutCached = listOf(filenames.source.asString(), filenames.compiled.asString())
    }

    override fun getRunCmd(input: CodeInput, compileResult: Result, runFilename: Filename, runFileId: String) = cmd {
        args = listOf("java", runFilename.asString())
        env = useUsrBinEnv
        files = useStdOutErrForFiles(input.stdin)
        copyIn = useFileIdCopyIn(fileId = runFileId, newName = runFilename.asString())
    }
}