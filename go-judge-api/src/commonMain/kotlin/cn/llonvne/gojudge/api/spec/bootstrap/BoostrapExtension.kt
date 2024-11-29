package cn.llonvne.gojudge.api.spec.bootstrap

/**
 * 检查端口是否合法
 * @param port 端口号
 */
fun isValidPort(port: Int) = port in 1..65535

/**
 * 表示输入的数字表示的是 Kib
 * 将自动转换为 bit
 */
val Long.Kib get() = this * 1024

/**
 * 表示输入的单位是 Mib
 * 将自动转换为 bit
 */
val Long.Mib get() = this.Kib * 1024
