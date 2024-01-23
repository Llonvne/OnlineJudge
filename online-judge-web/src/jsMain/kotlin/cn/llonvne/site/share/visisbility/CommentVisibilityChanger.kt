package cn.llonvne.site.share.visisbility

import cn.llonvne.dtos.CodeDto
import cn.llonvne.entity.problem.share.CodeCommentType
import cn.llonvne.entity.problem.share.limited
import cn.llonvne.message.Messager
import cn.llonvne.model.AuthenticationModel
import cn.llonvne.model.CodeModel

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
