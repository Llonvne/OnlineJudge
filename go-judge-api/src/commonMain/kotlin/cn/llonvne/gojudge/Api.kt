package cn.llonvne.gojudge

interface GoJudgeFile {
    
}

data class LocalFile(
    val src: String // absolute path for the file
)

data class MemoryFile(
    val content: Any // file contents, can be String or ByteArray (Buffer in TypeScript)
)

data class PreparedFile(
    val fileId: String // fileId defines file uploaded by /file
)

data class Collector(
    val name: String, // file name in copyOut
    val max: Int,  // maximum bytes to collect from pipe
    val pipe: Boolean? = null // collect over pipe or not (default false)
)

data class Symlink(
    val symlink: String // symlink destination (v1.6.0+)
)

data class Cmd(
    val args: List<String>, // command line argument
    val env: List<String>? = null, // environment

    // specifies file input / pipe collector for program file descriptors
    val files: List<Any>? = null, // Any can be LocalFile, MemoryFile, PreparedFile, Collector
    val tty: Boolean? = null, // enables tty on the input and output pipes (should have just one input & one output)
    // Notice: must have TERM environment variables (e.g. TERM=xterm)

    // limitations
    val cpuLimit: Long? = null,     // ns
    val realCpuLimit: Long? = null, // deprecated: use clock limit instead (still working)
    val clockLimit: Long? = null,   // ns
    val memoryLimit: Long? = null,  // byte
    val stackLimit: Long? = null,   // byte (N/A on windows, macOS cannot set over 32M)
    val procLimit: Int? = null,
    val cpuRateLimit: Int? = null, // limit cpu usage (1000 equals 1 cpu)
    val cpuSetLimit: String? = null, // Linux only: set the cpuSet for cgroup
    val strictMemoryLimit: Boolean? = null, // deprecated: use dataSegmentLimit instead (still working)
    val dataSegmentLimit: Boolean? = null, // Linux only: use (+ rlimit_data limit) enable by default if cgroup not enabled
    val addressSpaceLimit: Boolean? = null, // Linux only: use (+ rlimit_address_space limit)

    // copy the correspond file to the container dst path
    val copyIn: Map<String, Any>? = null, // Any can be LocalFile, MemoryFile, PreparedFile, Symlink

    // copy out specifies files need to be copied out from the container after execution
    // append '?' after file name will make the file optional and do not cause FileError when missing
    val copyOut: List<String>? = null,
    // similar to copyOut but stores file in go judge and returns fileId, later download through /file/:fileId
    val copyOutCached: List<String>? = null,
    // specifies the directory to dump container /w content
    val copyOutDir: String,
    // specifies the max file size to copy out
    val copyOutMax: Int? = null // byte
)

enum class Status {
    Accepted, // normal
    MemoryLimitExceeded, // mle
    TimeLimitExceeded, // tle
    OutputLimitExceeded, // ole
    FileError, // fe
    NonzeroExitStatus,
    Signalled,
    InternalError // system error
}

data class PipeIndex(
    val index: Int, // the index of cmd
    val fd: Int    // the fd number of cmd
)

data class PipeMap(
    val `in`: PipeIndex,  // input end of the pipe
    val out: PipeIndex, // output end of the pipe
    // enable pipe proxy from in to out,
    // content from in will be discarded if out closes
    val proxy: Boolean? = null,
    val name: String? = null,   // copy out proxy content if proxy enabled
    // limit the copy out content size,
    // proxy will still functioning after max
    val max: Int? = null
)

enum class FileErrorType {
    CopyInOpenFile,
    CopyInCreateFile,
    CopyInCopyContent,
    CopyOutOpen,
    CopyOutNotRegularFile,
    CopyOutSizeExceeded,
    CopyOutCreateFile,
    CopyOutCopyContent,
    CollectSizeExceeded
}

data class FileError(
    val name: String, // error file name
    val type: FileErrorType, // type
    val message: String? = null // detailed message
)

data class Request(
    val requestId: String? = null, // for WebSocket requests
    val cmd: List<Cmd>,
    val pipeMapping: List<PipeMap>? = null
)

data class CancelRequest(
    val cancelRequestId: String
)

// WebSocket request
typealias WSRequest = Any // Any can be Request or CancelRequest

data class Result(
    val status: Status,
    val error: String? = null, // potential system error message
    val exitStatus: Int,
    val time: Long,   // ns (cgroup recorded time)
    val memory: Long, // byte
    val runTime: Long, // ns (wall clock time)
    // copyFile name -> content
    val files: Map<String, String>? = null,
    // copyFileCached name -> fileId
    val fileIds: Map<String, String>? = null,
    // fileError contains detailed file errors
    val fileError: List<FileError>? = null
)

// WebSocket results
data class WSResult(
    val requestId: String,
    val results: List<Result>,
    val error: String? = null
)