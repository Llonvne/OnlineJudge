package cn.llonvne.gojudge.docker

object GoJudgeInitializer {

    data class Command(private val command: String, val decr: String, val noInteractive: Boolean = true) {




        val build: String
            get() {
                return if (noInteractive) {
                    "$command -y"
                } else {
                    command
                }
            }
    }

    internal val commands = listOf<Command>(
    )
}