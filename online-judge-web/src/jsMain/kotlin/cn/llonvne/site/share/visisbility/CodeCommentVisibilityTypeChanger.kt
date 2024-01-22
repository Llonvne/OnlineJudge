package cn.llonvne.site.share.visisbility

import cn.llonvne.dtos.CodeDto

class CodeCommentVisibilityTypeChanger(override val codeDto: CodeDto) : VisibilityChanger {
    fun change() {
        if (!isCodeOwner()) {
            return
        }

        change("更改该评论的可见性")
    }
}