package cn.llonvne

/**
 * 所有自定义异常的父类
 *
 * 所有业务错误不应该从这里继承
 */
open class OnlineJudgeException(msg: String) : Exception(msg) {
}

/**
 * 所有 CommonMain 自定义异常的父类
 */
open class CommonMainException(msg: String) : OnlineJudgeException(msg)

/**
 * 所有 JsMain 自定义异常的父类
 */
open class JsMainException(msg: String) : OnlineJudgeException(msg)

/**
 * 所有 JvmMain 自定义异常的父类
 */
open class JvmMainException(msg: String) : OnlineJudgeException(msg)


