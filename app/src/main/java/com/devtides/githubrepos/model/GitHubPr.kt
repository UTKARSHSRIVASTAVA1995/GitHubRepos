package com.devtides.githubrepos.model

import com.google.gson.annotations.SerializedName

data class GitHubPr(

    val id: String,
    val title: String,
    val number: String,

    @SerializedName("comments_url")
    val commentsUrl: String,
    val user: GithubUser?
) {
    override fun toString() = "$title -$id"
}