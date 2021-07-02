package com.devtides.githubrepos.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.devtides.githubrepos.model.GithubRepo
import com.devtides.githubrepos.model.GithubService
import com.devtides.githubrepos.model.GithubToken
import com.devtides.githubrepos.model.GithubUser
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class MainViewModel : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    val tokenLd = MutableLiveData<String>()
    val errorLd = MutableLiveData<String>()
    val reposLd = MutableLiveData<List<GithubRepo>>()


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

    override fun onCleared() {
        compositeDisposable.clear()
    }
}