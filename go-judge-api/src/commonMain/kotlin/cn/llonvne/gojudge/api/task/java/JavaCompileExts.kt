package cn.llonvne.gojudge.api.task.java

import cn.llonvne.gojudge.api.task.AbstractTask

internal fun useJavacArgs(filenames: AbstractTask.Filenames) =
    listOf("javac", filenames.source.asString())