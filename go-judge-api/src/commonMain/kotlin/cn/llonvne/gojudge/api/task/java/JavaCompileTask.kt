package cn.llonvne.gojudge.api.task.java

import arrow.core.Option
import arrow.core.some
import cn.llonvne.gojudge.api.Task
import cn.llonvne.gojudge.api.spec.runtime.*
import cn.llonvne.gojudge.api.task.AbstractTask
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.internal.cmd

fun javaCompileTask(): Task<CodeInput, Output> = JavaCompileTask()

private class JavaCompileTask : AbstractTask<CodeInput>() {
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

    override fun transformSourceOrCompiledFilename(filenames: Filenames): Filenames {
        return filenames.copy(
            source = filenames.source.copy(name = "Main"),
            compiled = filenames.compiled.copy(name = "Main")
        )
    }

    override fun transformRunFilename(filename: Filename): Filename {
        return filename.copy(name = "Main", extension = "class".some())
    }

    override fun getRunCmd(input: CodeInput, compileResult: Result, runFilename: Filename, runFileId: String) = cmd {
        args = listOf("java", runFilename.name)
        env = useUsrBinEnv
        files = useStdOutErrForFiles(input.stdin)
        copyIn = useFileIdCopyIn(fileId = runFileId, newName = runFilename.asString())
    }
}