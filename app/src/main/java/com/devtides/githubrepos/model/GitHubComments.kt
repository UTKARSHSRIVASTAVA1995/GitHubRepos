package com.devtides.githubrepos.model

data class GitHubComments(
    val body: String,
    val id: String
) {
    override fun toString() = "$body - $id"
}
