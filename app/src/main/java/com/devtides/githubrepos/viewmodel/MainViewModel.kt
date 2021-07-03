package com.devtides.githubrepos.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devtides.githubrepos.model.*
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import javax.sql.StatementEvent

class MainViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val tokenLd = MutableLiveData<String>()
    val errorLd = MutableLiveData<String>()
    val reposLd = MutableLiveData<List<GithubRepo>>()
    val prsLd = MutableLiveData<List<GitHubPr>>()
    val commentsLd = MutableLiveData<List<GitHubComments>>()
    val postCommentLd = MutableLiveData<Boolean>()


    fun getToken(clientId: String, clientSecret: String, code: String) {

        compositeDisposable.add(
            GithubService.getUnauthorizedApi().getAuthToken(clientId, clientSecret, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<GithubToken>() {
                    override fun onSuccess(t: GithubToken) {
                        tokenLd.value = t.accessToken
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        errorLd.value = "Cannot load token"
                    }
                })
        )
    }

    fun onLoadRepositories(token: String) {
        compositeDisposable.add(
            GithubService.getAuthorizedApi(token).getAllRepos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<List<GithubRepo>>() {
                    override fun onSuccess(value: List<GithubRepo>) {
                        reposLd.value = value
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        errorLd.value = "Cannot load Repos"
                    }

                })

        )
    }

    fun onLoadPrs(token: String, owner: String?, repository: String?) {
        if (owner != null && repository != null) {
            compositeDisposable.add(
                GithubService.getAuthorizedApi(token).getPRs(owner, repository)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<GitHubPr>>() {
                        override fun onSuccess(value: List<GitHubPr>) {
                            prsLd.value = value
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            errorLd.value = "Cannot Load Prs"
                        }
                    })
            )
        }
    }


    fun onLoadComments(token: String, owner: String?, repo: String?, pullNumber: String?) {

        if (owner != null && repo != null && pullNumber != null) {
            compositeDisposable.add(
                GithubService.getAuthorizedApi(token).getComments(owner, repo, pullNumber)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<List<GitHubComments>>() {
                        override fun onSuccess(value: List<GitHubComments>) {
                            commentsLd.value = value
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            errorLd.value = "Cannot Load Comments"

                        }

                    })
            )
        }

    }

    fun onPostComment(
        token: String,
        repo: GithubRepo,
        pullNumber: String?,
        content: GitHubComments
    ) {
        if (repo.owner.login != null && repo.name != null && pullNumber != null) {
            compositeDisposable.add(
                GithubService.getAuthorizedApi(token)
                    .postComment(repo.owner.login, repo.name, pullNumber, content)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<ResponseBody>() {
                        override fun onSuccess(t: ResponseBody) {
                            postCommentLd.value = true
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            errorLd.value = "Cannot Create Comments"
                        }

                    })
            )
        }
    }

    override fun onCleared() {
        compositeDisposable.clear()
    }
}