package cn.llonvne.gojudge.api.spec.bootstrap

fun isValidPort(port: Int) = port in 1..65535
val Long.Kib get() = this * 1024
val Long.Mib get() = this.Kib * 1024