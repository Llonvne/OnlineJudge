package cn.llonvne.site

import cn.llonvne.AppScope
import cn.llonvne.message.Messager
import cn.llonvne.model.CodeModel
import io.kvision.core.Container
import io.kvision.html.div
import kotlinx.coroutines.launch

fun Container.share(shareId: Int) {
    AppScope.launch {
        val resp = CodeModel.getCode(shareId)
        Messager.toastInfo(resp.toString())
    }
}