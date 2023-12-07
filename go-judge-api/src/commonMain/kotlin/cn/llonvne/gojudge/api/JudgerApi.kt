package cn.llonvne.gojudge.api

import de.jensklingenberg.ktorfit.http.GET

interface JudgerApi {
    @GET("/version")
    fun getJudgerVersion()
}

