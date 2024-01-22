package cn.llonvne.site.share.visisbility

import cn.llonvne.AppScope
import cn.llonvne.dtos.CodeDto
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.decr
import cn.llonvne.entity.problem.share.limited
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.CodeModel
import io.kvision.core.Container
import io.kvision.html.ButtonStyle
import io.kvision.html.button
import io.kvision.html.p
import io.kvision.modal.Dialog
import io.kvision.toolbar.buttonGroup
import kotlinx.coroutines.launch

class CommentVisibilityChanger(override val codeDto: CodeDto) : VisibilityChanger {
    fun change() {
        if (!isCodeOwner()) {
            return
        }

        val types = CodeCommentType.entries.limited(AuthenticationModel.userToken.value ?: return)

        change("更改评论可见性",types){
            Messager.toastInfo(
                CodeModel.setCodeCommentType(
                    shareId = codeDto.codeId,
                    type = it
                ).toString()
            )
        }
    }
}
