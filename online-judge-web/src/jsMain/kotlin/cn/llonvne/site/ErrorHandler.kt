package cn.llonvne.site

import cn.llonvne.compoent.NotFoundAble
import cn.llonvne.compoent.notFound
import io.kvision.core.Container
import io.kvision.html.I

interface ErrorHandler<ID> {
    fun handleLanguageNotFound(root: Container, id: ID) {
        root.notFound(object : NotFoundAble {
            override val header: String
                get() = "评测语言未找到"
            override val notice: String
                get() = "请尝试再次提交，如果还是错误，请联系我们"
            override val errorCode: String
                get() = "ErrorCode-LanguageNotFound-$id"
        })
    }

    fun handleCodeNotFound(root: Container, id: ID) {
        root.notFound(object : NotFoundAble {
            override val header: String
                get() = "评测结果未找到"
            override val notice: String
                get() = "请尝试再次提交，如果还是错误，请联系我们"
            override val errorCode: String
                get() = "ErrorCode-PlaygroundOutputNotFound-CodeId-${id}"

        })
    }

    fun handleJudgeResultParseError(root: Container, id: ID) {
        root.notFound(object : NotFoundAble {
            override val header: String
                get() = "无法解析评测结果"
            override val notice: String
                get() = "请尝试再次提交，如果还是错误，请联系我们"
            override val errorCode: String
                get() = "JudgeResultParseError-CodeId-${id}"

        })
    }

    fun handleSubmissionNotFound(root: Container, id: ID) {
        root.notFound(object : NotFoundAble {
            override val header: String
                get() = "找到不到提交记录"
            override val notice: String
                get() = "请确认提交号正确"
            override val errorCode: String
                get() = "SubmissionNotFound-CodeId-${id}"
        })
    }
}

class JudgeResultDisplayErrorHandler private constructor() : ErrorHandler<Int> {
    companion object {

        private val errorHandler = JudgeResultDisplayErrorHandler()

        fun getHandler(): ErrorHandler<Int> = errorHandler
    }
}
