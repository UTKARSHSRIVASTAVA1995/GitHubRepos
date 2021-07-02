package com.devtides.githubrepos.model

import io.reactivex.Single
import retrofit2.http.*

interface GithubApi {

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("https://github.com/login/oauth/access_token")
    fun getAuthToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String
    ): Single<GithubToken>

    @GET("user/repos")
    fun getAllRepos(): Single<List<GithubRepo>>
}