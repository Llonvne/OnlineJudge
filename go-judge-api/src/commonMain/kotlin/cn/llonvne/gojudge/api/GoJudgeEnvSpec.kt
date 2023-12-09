package cn.llonvne.gojudge.api

import com.benasher44.uuid.uuid4

fun portValidCheck(port: Int) = port in 1..65535

const val LATEST = "latest"

const val LOCALHOST = "localhost"

val Long.Kib get() = this * 1024

val Long.Mib get() = this.Kib * 1024

@Suppress("unused")
sealed interface GoJudgeVersion {

    val tag: String

    data object Latest : GoJudgeVersion {
        override val tag: String = LATEST
    }

    data class Customized(override val tag: String) : GoJudgeVersion
}

data class GoJudgePortMapping(
    val outer: Int = 5050, val inner: Int = 5050
) {
    init {
        require(portValidCheck(outer)) {
            "outer:$outer is not a valid port in 1..65535"
        }
        require(portValidCheck(inner)) {
            "inner:$inner is not a valid port in 1..65535"
        }
    }

    val asDockerPortMappingString get() = "$inner:$outer"
}

sealed interface GoJudgePortMappings {
    val binds: List<GoJudgePortMapping>

    data object Default : GoJudgePortMappings {
        override val binds: List<GoJudgePortMapping> = listOf(GoJudgePortMapping())
    }

    @Suppress("unused")
    data class Customized(override val binds: List<GoJudgePortMapping>) : GoJudgePortMappings
}

sealed interface ContainerName {
    val name: String

    data class GeneratorWithPrefix(val prefix: String, val randomLatterLength: Int = 6) :
        ContainerName {
        override val name: String = "$prefix:${uuid4().toString().subSequence(0..randomLatterLength)}"
    }

    @Suppress("unused")
    data class Customized(override val name: String) : ContainerName
}

data class HttpAddr(val url: String, val port: Int) {
    init {
        require(portValidCheck(port))
    }

    override fun toString(): String {
        return "$url:$port"
    }
}

interface IsDefaultSetting {
    val isDefault: Boolean
}

data class GoJudgeEnvSpec(
    val httpAddr: HttpAddr = DEFAULT_HTTP_ADDR,
    val enableGrpc: Boolean = DEFAULT_ENABLE_GRPC,
    val grpcAddr: HttpAddr = DEFAULT_GRPC_ADDR,
    val logLevel: GoJudgeLogLevel = GoJudgeLogLevel.INFO,
    val authToken: GoJudgeAuthTokenSetting = GoJudgeAuthTokenSetting.Disabled,
    val goDebugEndPoint: Boolean = DEFAULT_GO_DEBUG_ENDPOINT_ENABLE,
    val prometheusMetrics: Boolean = DEFAULT_PROMETHEUS_METRICS_ENDPOINT_ENABLE,
    val goDebugAndPrometheusMetricsAddr: HttpAddr = DEFAULT_MONITOR_ADDR,
    val concurrencyNumber: ConcurrencyNumberSetting = ConcurrencyNumberSetting.EqualCpuCore,
    val fileStore: FileStoreSetting = FileStoreSetting.Memory,
    // 默认的CGroup前缀是 gojudge ，可以用 -cgroup-prefix flag指定。
    val cGroupPrefix: CGroupPrefixSetting = CGroupPrefixSetting.Default,
    // 限制 src copyIn 路径用逗号分割（必须是绝对路径）（示例： /bin,/usr ）
    val srcPrefix: List<String> = listOf(),
    val timeLimitCheckerInterval: GoJudgeTimeInterval = GoJudgeTimeInterval.Default,
    val outputLimit: OutputLimitSetting = OutputLimitSetting.Default,
    // 指定用于检查超出内存限制的额外内存限制（默认为 16KiB）
    val extraMemoryLimit: ExtraMemoryLimitSetting = ExtraMemoryLimitSetting.Default,
    val copyOutLimit: CopyOutLimitSetting = CopyOutLimitSetting.Default,
    val openFileLimit: OpenFileLimitSetting = OpenFileLimitSetting.Default,
    val linuxOnlySpec: LinuxOnlySpec = LinuxOnlySpec.default(),
    val preFork: PreForkSetting = PreForkSetting.Default,
    val fileTimeout: FileTimeoutSetting = FileTimeoutSetting.Disabled
) {
    companion object {
        val DEFAULT_HTTP_ADDR = HttpAddr(LOCALHOST, 5050)

        val DEFAULT_GRPC_ADDR = HttpAddr(LOCALHOST, 5051)

        const val DEFAULT_ENABLE_GRPC = false

        const val DEFAULT_GO_DEBUG_ENDPOINT_ENABLE = false

        const val DEFAULT_PROMETHEUS_METRICS_ENDPOINT_ENABLE = false

        val DEFAULT_MONITOR_ADDR = HttpAddr(LOCALHOST, 5052)
    }

    @Suppress("unused")
    sealed interface GoJudgeLogLevel : IsDefaultSetting {

        override val isDefault get() = this is INFO

        data object INFO : GoJudgeLogLevel

        data object SILENT : GoJudgeLogLevel

        data object RELEASE : GoJudgeLogLevel
    }

    @Suppress("unused")
    sealed interface GoJudgeAuthTokenSetting : IsDefaultSetting {
        override val isDefault: Boolean get() = this is Disabled

        data object Disabled : GoJudgeAuthTokenSetting

        data class Enable(val token: String) : GoJudgeAuthTokenSetting
    }

    @Suppress("unused")
    sealed interface ConcurrencyNumberSetting : IsDefaultSetting {
        override val isDefault: Boolean get() = this is EqualCpuCore

        data object EqualCpuCore : ConcurrencyNumberSetting

        data class Customized(val number: Int) : ConcurrencyNumberSetting {
            init {
                require(number > 0)
            }
        }
    }

    @Suppress("unused")
    sealed interface FileStoreSetting : IsDefaultSetting {
        override val isDefault: Boolean get() = this is Memory

        data object Memory : FileStoreSetting
        data object Dir : FileStoreSetting
    }

    @Suppress("unused")
    sealed interface CGroupPrefixSetting : IsDefaultSetting {

        val prefix: String

        override val isDefault: Boolean get() = this is Default

        data object Default : CGroupPrefixSetting {
            override val prefix: String = "gojudge"
        }

        data class Customized(override val prefix: String) : CGroupPrefixSetting {}
    }

    @Suppress("unused")
    sealed interface GoJudgeTimeInterval : IsDefaultSetting {
        val time: String
        override val isDefault: Boolean get() = this is Default

        data object Default : GoJudgeTimeInterval {
            override val time: String = "100ms"
        }

        data class Ms(val ms: Int) : GoJudgeTimeInterval {
            override val time: String = "${ms}ms"
        }

        data class Second(val second: Int) : GoJudgeTimeInterval {
            override val time: String = "${second}s"
        }
    }

    @Suppress("unused")
    sealed interface OutputLimitSetting : IsDefaultSetting {

        val byte: Long

        override val isDefault: Boolean
            get() = this is Default

        data object Default : OutputLimitSetting {
            override val byte: Long
                get() = 256L.Mib
        }

        data class Customize(override val byte: Long) : OutputLimitSetting
    }

    @Suppress("unused")
    sealed interface ExtraMemoryLimitSetting : IsDefaultSetting {
        val byte: Long

        override val isDefault: Boolean
            get() = this is Default

        data object Default : ExtraMemoryLimitSetting {
            override val byte: Long = 16L.Kib
        }

        data class Customized(override val byte: Long) : ExtraMemoryLimitSetting
    }

    @Suppress("unused")
    sealed interface CopyOutLimitSetting : IsDefaultSetting {
        val byte: Long
        override val isDefault: Boolean
            get() = this is Default

        data object Default : CopyOutLimitSetting {
            override val byte: Long = 64L.Mib
        }

        data class Customized(override val byte: Long) : CopyOutLimitSetting
    }

    @Suppress("unused")
    sealed interface OpenFileLimitSetting : IsDefaultSetting {
        val limit: Long
        override val isDefault: Boolean
            get() = this is Default

        data object Default : OpenFileLimitSetting {
            override val limit: Long = 256
        }

        data class Customized(override val limit: Long) : OpenFileLimitSetting
    }

    @Suppress("unused")
    sealed interface LinuxOnlySpec : IsDefaultSetting {

        data object NotLinuxPlatform : LinuxOnlySpec {
            override val isDefault: Boolean
                get() = true
        }

        companion object {
            fun default() = NotLinuxPlatform
        }

        data class LinuxPlatformSpec(
            // specifies cpuset.cpus cgroup for each container (Linux only)
            val cpuSets: CpuSetting = CpuSetting.Default,
            val containerCredStart: ContainerCredStartSetting = ContainerCredStartSetting.Default,
            val enableCpuRate: CpuRateSetting = CpuRateSetting.Disable,
            // specifies seecomp filter setting to load when running program (need build tag seccomp) (Linux only)
            val seccompConf: SeccompConfSetting = SeccompConfSetting.Disable,
            // 指定使用默认挂载时 /tmp 的 /w tmpfs 参数（仅限 Linux）
            val tmpFsParam: TmsFsParamSetting = TmsFsParamSetting.Default,
            val mountConf: MountConfSetting = MountConfSetting.Default
        ) : LinuxOnlySpec {
            override val isDefault: Boolean
                get() = false

            sealed interface CpuSetting : IsDefaultSetting {

                override val isDefault: Boolean
                    get() = this is Default

                data object Default : CpuSetting

                data class Customized(val settings: String) : CpuSetting
            }

            sealed interface ContainerCredStartSetting : IsDefaultSetting {
                val start: Int

                override val isDefault: Boolean
                    get() = this is Default

                data object Default : ContainerCredStartSetting {
                    override val start: Int
                        get() = 10000
                }

                data class Customized(override val start: Int) : ContainerCredStartSetting
            }

            sealed interface CpuRateSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Disable

                data object Disable : CpuRateSetting

                data object Enable : CpuRateSetting
            }

            sealed interface SeccompConfSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Disable

                data object Disable : SeccompConfSetting

                data class Customized(val settings: String) : SeccompConfSetting
            }

            sealed interface TmsFsParamSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Default

                data object Default : TmsFsParamSetting
                data class Customized(val command: String) : TmsFsParamSetting
            }

            sealed interface MountConfSetting : IsDefaultSetting {
                override val isDefault: Boolean
                    get() = this is Default

                data object Default : MountConfSetting

                data class Customized(val settings: String) : MountConfSetting
            }
        }
    }

    @Suppress("unused")
    sealed interface PreForkSetting : IsDefaultSetting {
        override val isDefault: Boolean
            get() = this is Default

        data object Default : PreForkSetting

        data class Customized(val instance: Int) : PreForkSetting
    }

    @Suppress("unused")
    sealed interface FileTimeoutSetting : IsDefaultSetting {
        override val isDefault: Boolean
            get() = this is Disabled


        data object Disabled : FileTimeoutSetting

        data class Timeout(val minutes: Int) : FileTimeoutSetting {
            val seconds = (minutes * 60).toString() + "s"
        }
    }
}