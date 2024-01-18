package cn.llonvne.security

fun <R> AuthenticationToken.invoke(block: context(AuthenticationToken) () -> R) {
    block(this)
}

