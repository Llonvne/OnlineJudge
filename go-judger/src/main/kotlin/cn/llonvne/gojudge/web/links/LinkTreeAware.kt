package cn.llonvne.gojudge.web.links

interface LinkTreeAware {
    val linkTreeUri: String
}

private data class LinkTreeAwareImpl(
    override val linkTreeUri: String,
) : LinkTreeAware

/**
 * 预先定义 LinkTree 的地址
 * 可以使用 [linkTr] 函数来获取内部的定义的 uri
 */
fun linkTrUri(
    uri: String,
    context: context(LinkTreeAware)
    () -> Unit,
) {
    val impl = LinkTreeAwareImpl(uri)
    context(impl)
}
