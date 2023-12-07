package cn.llonvne.gojudge

import de.jensklingenberg.ktorfit.http.GET

interface JudgerApi {
    @GET("/version")
    fun getJudgerVersion()
}

