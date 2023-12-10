package cn.llonvne.gojudge.docker

object GoJudgeInitializer {

    data class Command(val command: String, val decr: String)

    val commands = listOf<Command>(
//        Command("apt update", "apt update")
    )
}