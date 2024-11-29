package cn.llonvne.entity.problem

enum class SubmissionStatus {
    Received,
    Finished,
    ;

    val readable: String
        get() =
            when (this) {
                Received -> "收到评测请求"
                Finished -> "评测完毕"
            }
}
