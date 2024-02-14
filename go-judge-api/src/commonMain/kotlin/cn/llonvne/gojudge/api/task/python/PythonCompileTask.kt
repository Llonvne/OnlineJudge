package cn.llonvne.gojudge.api.task.python

import arrow.core.Option
import arrow.core.some
import cn.llonvne.gojudge.api.Task
import cn.llonvne.gojudge.api.spec.runtime.*
import cn.llonvne.gojudge.api.task.AbstractTask
import cn.llonvne.gojudge.api.task.CodeInput
import cn.llonvne.gojudge.api.task.Output
import cn.llonvne.gojudge.internal.cmd

fun python3CompileTask(): Task<CodeInput, Output> = PythonCompileTask()

private class PythonCompileTask(
    private val pyCmd: String = "python3"
) : AbstractTask<CodeInput>() {
    /**
     * 源代码拓展名
     * 返回 None 时将没有后缀
     */
    override val sourceCodeExtension: Option<String>
        get() = "py".some()

    /**
     * 编译后源代码拓展名
     * 返回 None 时将没有后缀
     */
    override val compiledFileExtension: Option<String>
        get() = "py".some()

    override fun transformSourceOrCompiledFilename(filenames: Filenames): Filenames {
        return Filenames(filenames.source, filenames.source)
    }

    override fun getCompileCmd(input: CodeInput, filenames: Filenames): Cmd {
        return cmd {
            args = listOf()
            env = useUsrBinEnv
            files = useStdOutErrForFiles()
            copyIn = useMemoryCodeCopyIn(filenames.source.asString(), input.code)
            copyOut = useStdOutErrForCopyOut()
            copyOutCached = listOf(filenames.source.asString(), filenames.compiled.asString())
        }
    }

    override fun transformCompileStatus(compileStatus: Status, compileResult: Result): Status {
        return Status.Accepted
    }

    override fun getRunCmd(input: CodeInput, compileResult: Result, runFilename: Filename, runFileId: String): Cmd {
        return cmd {
            args = listOf(pyCmd, runFilename.name)
            env = useUsrBinEnv
            files = useStdOutErrForFiles(input.stdin)
            copyIn = useFileIdCopyIn(fileId = runFileId, newName = runFilename.asString())
        }
    }
}