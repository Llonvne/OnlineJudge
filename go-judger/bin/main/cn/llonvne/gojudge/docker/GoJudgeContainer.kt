package cn.llonvne.gojudge.docker

import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource
import cn.llonvne.gojudge.api.spec.bootstrap.GoJudgeEnvSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.Container
import org.testcontainers.containers.ExecInContainerPattern
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

//"criyle/go-judge"
internal const val GO_JUDGE_DOCKER_NAME = "judger"

/**
 * 判断当前是否为Liunx主机
 */
private val isLinux by lazy {
    val dockerHost = System.getenv("DOCKER_HOST")
    dockerHost != null && dockerHost.startsWith("unix://")
}

/**
 * Docker Container 的协程包装器
 */
class CoroutineContainer(private val container: GenericContainer<*>) {
    companion object {
        /**
         * 建立独立的线程为Docker协程服务
         */
        @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
        private val dockerCoroutinesContext = newSingleThreadContext("DockerThread")
    }

    /**
     * 获得Docker客户端
     */
    private val dockerClient = DockerClientFactory.lazyClient()

    private suspend fun on(block: suspend (container: GenericContainer<*>) -> Unit) {
        withContext(dockerCoroutinesContext) {
            block(container)
        }
    }

    internal suspend fun start() {
        on {
            container.start()
        }
    }


    suspend fun close() {
        on { container.close() }
    }

    suspend fun exec(command: String): Container.ExecResult {
        return withContext(dockerCoroutinesContext) {
            val result = ExecInContainerPattern
                .execInContainer(dockerClient, container.containerInfo, "/bin/sh", "-c", command)
            result
        }
    }
}

private val log = KotlinLogging.logger {}

fun configureGoJudgeContainer(
    name: String = UUID.randomUUID().toString().substring(0..6),
    isPrivilegedMode: Boolean = true,
    reuseContainer: Boolean = false,
    envs: MutableMap<String, String> = mutableMapOf(),
    spec: GoJudgeEnvSpec = GoJudgeEnvSpec()
): Resource<CoroutineContainer> {

    log.info { "building new docker client" }

    envs["DEBIAN_FRONTEND"] = "noninteractive"

    val container = GenericContainer(DockerImageName.parse(GO_JUDGE_DOCKER_NAME))
        .withSharedMemorySize((256 * 1024 * 1024).toLong()) // 256 MB
        .withPrivilegedMode(isPrivilegedMode)
        .withReuse(reuseContainer)
        .withCreateContainerCmdModifier { it.withName(name) }
        .withEnv(envs)

    applySpec(spec, container)
    return resource({
        CoroutineContainer(container).also {
            it.start()
        }
    }) { wrapper, _ ->
        wrapper.close()
    }
}

fun shouldNotHappen(): Nothing =
    throw IllegalStateException("it shouldn't be info please report me on Github:Llonvne/OnlineJudge")

fun applySpec(spec: GoJudgeEnvSpec, container: GenericContainer<*>) {

    val portBindings = mutableListOf<String>()
    val commands = mutableListOf<String>()

    fun withCommand(command: String) = commands.add(command)

    val logger = LoggerFactory.getLogger(GenericContainer::class.java)

//    withCommand("-http-addr=${spec.httpAddr}")

    logger.info("go-judge http endpoint on ${spec.httpAddr},port ${spec.httpAddr.port} is exposed")

    portBindings.add("5050:5050")

    if (spec.enableGrpc != GoJudgeEnvSpec.DEFAULT_ENABLE_GRPC) {
        withCommand("-enable-grpc=${spec.enableGrpc}")
        logger.info("go-judge grpc is active")
    }

    if (spec.grpcAddr != GoJudgeEnvSpec.DEFAULT_GRPC_ADDR) {

        if (spec.enableGrpc) {
            logger.error("you disable grpc service,and set a address,please check your setting")
        }
        withCommand("-grpc-addr=${spec.grpcAddr}")
        logger.info("go-judge grpc endpoint on ${spec.grpcAddr},port ${spec.grpcAddr.port} is exposed")

        portBindings.add("${spec.grpcAddr.port}:${spec.grpcAddr.port}")
    }

    if (!spec.logLevel.isDefault) {
        when (spec.logLevel) {
            GoJudgeEnvSpec.GoJudgeLogLevel.RELEASE -> withCommand("-release")
            GoJudgeEnvSpec.GoJudgeLogLevel.SILENT -> withCommand("-silent")
            GoJudgeEnvSpec.GoJudgeLogLevel.INFO -> shouldNotHappen()
        }
    }
    when (spec.authToken) {
        GoJudgeEnvSpec.GoJudgeAuthTokenSetting.Disabled -> Unit
        is GoJudgeEnvSpec.GoJudgeAuthTokenSetting.Enable -> {
            @Suppress("SMARTCAST_IMPOSSIBLE")
            container.withCommand("-auth-token=${spec.authToken.token}")
        }
    }
    if (spec.goDebugEndPoint != GoJudgeEnvSpec.DEFAULT_GO_DEBUG_ENDPOINT_ENABLE) {
        withCommand("-enable-debug")
    }

    if (spec.prometheusMetrics != GoJudgeEnvSpec.DEFAULT_PROMETHEUS_METRICS_ENDPOINT_ENABLE) {
        withCommand("-enable-metrics")
    }

    if (spec.goDebugAndPrometheusMetricsAddr != GoJudgeEnvSpec.DEFAULT_MONITOR_ADDR) {
        withCommand("-monitor-addr=${spec.goDebugAndPrometheusMetricsAddr}")

        logger.info("go-judge monitor endpoint on ${spec.goDebugAndPrometheusMetricsAddr},port ${spec.goDebugAndPrometheusMetricsAddr.port} is exposed")

        if (!spec.prometheusMetrics && !spec.goDebugEndPoint) {
            logger.error("go-judge: go debug and prometheus metrics is all disabled,but set a addr for it,please check you setting")
        }

        portBindings.add("${spec.goDebugAndPrometheusMetricsAddr.port}:${spec.goDebugAndPrometheusMetricsAddr.port}")
    }

    @Suppress("SMARTCAST_IMPOSSIBLE")
    when (spec.concurrencyNumber) {
        is GoJudgeEnvSpec.ConcurrencyNumberSetting.Customized -> withCommand("-parallelism=${spec.concurrencyNumber.number}")
        GoJudgeEnvSpec.ConcurrencyNumberSetting.EqualCpuCore -> Unit
    }

    when (spec.fileStore) {
        GoJudgeEnvSpec.FileStoreSetting.Dir -> withCommand("-dir")
        GoJudgeEnvSpec.FileStoreSetting.Memory -> Unit
    }

    when (spec.cGroupPrefix) {
        is GoJudgeEnvSpec.CGroupPrefixSetting.Customized -> withCommand("-cgroup-prefix=${spec.cGroupPrefix.prefix}")
        GoJudgeEnvSpec.CGroupPrefixSetting.Default -> Unit
    }

    if (spec.srcPrefix.isNotEmpty()) {
        withCommand("-src-prefix=${spec.srcPrefix.joinToString(",")}")
    }

    when (spec.timeLimitCheckerInterval) {
        GoJudgeEnvSpec.GoJudgeTimeInterval.Default -> Unit
        is GoJudgeEnvSpec.GoJudgeTimeInterval.Ms -> withCommand("-time-limit-checker-interval=${spec.timeLimitCheckerInterval.time}")
        is GoJudgeEnvSpec.GoJudgeTimeInterval.Second -> withCommand("-time-limit-checker-interval=${spec.timeLimitCheckerInterval.time}")
    }

    when (spec.outputLimit) {
        is GoJudgeEnvSpec.OutputLimitSetting.Customize -> withCommand("-output-limit=${spec.outputLimit.byte} b")
        GoJudgeEnvSpec.OutputLimitSetting.Default -> Unit
    }

    when (spec.extraMemoryLimit) {
        is GoJudgeEnvSpec.ExtraMemoryLimitSetting.Customized -> withCommand("-extra-memory-limit=${spec.extraMemoryLimit.byte} b")
        GoJudgeEnvSpec.ExtraMemoryLimitSetting.Default -> Unit
    }

    when (spec.copyOutLimit) {
        is GoJudgeEnvSpec.CopyOutLimitSetting.Customized -> withCommand("-copy-out-limit=${spec.copyOutLimit.byte} b")
        GoJudgeEnvSpec.CopyOutLimitSetting.Default -> Unit
    }

    when (spec.openFileLimit) {
        GoJudgeEnvSpec.OpenFileLimitSetting.Default -> Unit
        is GoJudgeEnvSpec.OpenFileLimitSetting.Customized -> withCommand("-open-file-limit=${spec.openFileLimit.limit}")
    }
    @Suppress("SMARTCAST_IMPOSSIBLE")
    when (spec.linuxOnlySpec) {
        is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec -> {
            if (!isLinux) {
                logger.error("You are not in a liunx platform but set some liunx setting,we will apply this setting,but may not valid")
            }

            val linuxSpec = spec.linuxOnlySpec as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec

            when (linuxSpec.cpuSets) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuSetting.Customized -> withCommand("-cpuset=${(linuxSpec.cpuSets as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuSetting.Customized).settings}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuSetting.Default -> Unit
            }

            when (linuxSpec.containerCredStart) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.ContainerCredStartSetting.Customized -> withCommand("-container-cred-start=${linuxSpec.containerCredStart.start}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.ContainerCredStartSetting.Default -> Unit
            }

            when (linuxSpec.enableCpuRate) {
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuRateSetting.Disable -> Unit
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.CpuRateSetting.Enable -> withCommand("-enable-cpu-rate=true")
            }

            when (linuxSpec.seccompConf) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.SeccompConfSetting.Customized -> withCommand("-seccomp-conf=${(linuxSpec.seccompConf as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.SeccompConfSetting.Customized).settings}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.SeccompConfSetting.Disable -> Unit
            }

            when (linuxSpec.tmpFsParam) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.TmsFsParamSetting.Customized -> withCommand("-tmp-fs-param=${(linuxSpec.tmpFsParam as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.TmsFsParamSetting.Customized).command}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.TmsFsParamSetting.Default -> Unit
            }

            when (linuxSpec.mountConf) {
                is GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.MountConfSetting.Customized -> withCommand("-mount-conf=${(linuxSpec.mountConf as GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.MountConfSetting.Customized).settings}")
                GoJudgeEnvSpec.LinuxOnlySpec.LinuxPlatformSpec.MountConfSetting.Default -> Unit
            }
        }

        GoJudgeEnvSpec.LinuxOnlySpec.NotLinuxPlatform -> {
            Unit
        }
    }

    when (spec.preFork) {
        is GoJudgeEnvSpec.PreForkSetting.Customized -> withCommand("-pre-fork=${(spec.preFork as GoJudgeEnvSpec.PreForkSetting.Customized).instance}")
        GoJudgeEnvSpec.PreForkSetting.Default -> Unit
    }

    when (spec.fileTimeout) {
        GoJudgeEnvSpec.FileTimeoutSetting.Disabled -> Unit
        is GoJudgeEnvSpec.FileTimeoutSetting.Timeout -> withCommand("-file-timeout=${(spec.fileTimeout as GoJudgeEnvSpec.FileTimeoutSetting.Timeout).seconds}")
    }

    container.withCommand(*commands.toTypedArray())
    container.portBindings = portBindings
}