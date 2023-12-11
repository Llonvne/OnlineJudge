package cn.llonvne.gojudge.docker

import arrow.core.raise.either
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource
import cn.llonvne.gojudge.api.GoJudgeEnvSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.Container
import org.testcontainers.containers.ExecInContainerPattern
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.math.BigInteger
import java.security.SecureRandom


private const val GO_JUDGE_DOCKER_NAME = "criyle/go-judge"

val isLinux by lazy {
    val dockerHost = System.getenv("DOCKER_HOST")
    dockerHost != null && dockerHost.startsWith("unix://")
}

fun generateSecureKey(length: Int): String {
    val secureRandom = SecureRandom()
    val randomBytes = ByteArray(length)
    secureRandom.nextBytes(randomBytes)
    return BigInteger(1, randomBytes).toString(16)
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
val dockerCoroutinesContext = newSingleThreadContext("DockerThread")

class ContainerWrapper(private val container: GenericContainer<*>) {

    private val dockerClient = DockerClientFactory.lazyClient()
    private val containerId = container.containerId
    private val log = KotlinLogging.logger { }

    private suspend fun on(block: suspend (container: GenericContainer<*>) -> Unit) {
        withContext(dockerCoroutinesContext) {
            block(container)
        }
    }

    suspend fun start() = either {
        on {
            container.start()

            GoJudgeInitializer.commands.forEach { command ->
                if (exec(command.command).exitCode == 0) {
                    log.info { "${command.decr} successful" }
                } else {
                    raise("${command.decr} failed!")
                }
            }
        }
    }

    suspend fun close() {
        on { container.close() }
    }

    private suspend fun exec(command: String): Container.ExecResult {
        return withContext(dockerCoroutinesContext) {
            ExecInContainerPattern
                .execInContainer(dockerClient, container.containerInfo, "/bin/sh", "-c", command)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    prettyPrint = true
    encodeDefaults = true
    explicitNulls = true
}

private val log = KotlinLogging.logger {}

suspend fun persistInFile(spec: GoJudgeEnvSpec, id: String) = withContext(Dispatchers.IO) {
    log.info { "Trying to persist file" }
    val file = File("client.json")
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(json.encodeToString(GoJudgeResolver.CachedJsonClient(spec, id)))
}


fun configureGoJudgeContainer(
    name: String = "go-judge",
    isPrivilegedMode: Boolean = true,
    reuseContainer: Boolean = false,
    envs: MutableMap<String, String> = mutableMapOf(),
    spec: GoJudgeEnvSpec = GoJudgeEnvSpec()
): Resource<ContainerWrapper> {

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
        ContainerWrapper(container).also {
            it.start()
            persistInFile(spec, container.getContainerId())
        }
    }) { wrapper, _ ->
        coroutineScope {
            launch {
                wrapper.close()
            }
        }
    }
}

fun shouldHappen(): Nothing =
    throw IllegalStateException("it shouldn't be info please report me on Github:Llonvne/OnlineJudge")

fun applySpec(spec: GoJudgeEnvSpec, container: GenericContainer<*>) {

    val portBindings = mutableListOf<String>()
    val commands = mutableListOf<String>()

    fun withCommand(command: String) = commands.add(command)

    val logger = LoggerFactory.getLogger(GenericContainer::class.java)

    withCommand("-http-addr=${spec.httpAddr}")

    logger.info("go-judge http endpoint on ${spec.httpAddr},port ${spec.httpAddr.port} is exposed")

    portBindings.add("${spec.httpAddr.port}:${spec.httpAddr.port}")

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
            GoJudgeEnvSpec.GoJudgeLogLevel.INFO -> shouldHappen()
        }
    }

    if (!spec.authToken.isDefault) {
        when (spec.authToken) {
            GoJudgeEnvSpec.GoJudgeAuthTokenSetting.Disabled -> shouldHappen()
            is GoJudgeEnvSpec.GoJudgeAuthTokenSetting.Enable -> {
                @Suppress("SMARTCAST_IMPOSSIBLE")
                container.withCommand("-auth-token=${spec.authToken.token}")
            }
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

    container.portBindings = portBindings
    container.withCommand(*commands.toTypedArray())
}