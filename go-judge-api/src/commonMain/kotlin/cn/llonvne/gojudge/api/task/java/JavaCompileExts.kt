package cn.llonvne.gojudge.api.task.java

import cn.llonvne.gojudge.api.task.AbstractTask

fun useJavacArgs(filenames: AbstractTask.Filenames) =
    listOf("javac", filenames.source.asString(), "output", filenames.compiled.asString())