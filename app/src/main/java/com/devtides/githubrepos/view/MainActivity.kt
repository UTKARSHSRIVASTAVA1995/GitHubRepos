package com.devtides.githubrepos.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.devtides.githubrepos.R
import com.devtides.githubrepos.model.GitHubComments
import com.devtides.githubrepos.model.GitHubPr
import com.devtides.githubrepos.model.GithubRepo
import com.devtides.githubrepos.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        repositoriesSpinner.isEnabled = false
        repositoriesSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayListOf("No repositories available")
        )
        repositoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Load PullRequests

                if (parent?.selectedItem is GithubRepo) {
                    val currentRepo = parent.selectedItem as GithubRepo
                    token?.let {
                        viewModel.onLoadPrs(it, currentRepo.owner.login, currentRepo.name)
                    }
                }
            }
        }


        prsSpinner.isEnabled = false
        prsSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayListOf("Please select repository")
        )
        prsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Load comments

                if (parent?.selectedItem is GitHubPr) {
                    val githubPr = parent.selectedItem as GitHubPr
                    val currentRepo = repositoriesSpinner.selectedItem as GithubRepo
                    token?.let {
                        viewModel.onLoadComments(
                            it,
                            githubPr.user?.login,
                            currentRepo.name,
                            githubPr.number
                        )
                    }
                }
            }
        }


        commentsSpinner.isEnabled = false
        commentsSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            arrayListOf("Please select PR")
        )


        observeViewModel()
    }

    private fun observeViewModel() {

        viewModel.tokenLd.observe(this, Observer { token ->
            if (token.isNotEmpty()) {
                this.token = token
                loadReposButton.isEnabled = true
                Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.reposLd.observe(this, Observer { reposList ->
            if (reposList.isNullOrEmpty()) {
                val spinnerAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item, reposList
                )
                repositoriesSpinner.adapter = spinnerAdapter
                repositoriesSpinner.isEnabled = true
            } else {
                val spinnerAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item, arrayListOf("User has no Repos")
                )
                repositoriesSpinner.adapter = spinnerAdapter
                repositoriesSpinner.isEnabled = false
            }
        })

        viewModel.prsLd.observe(this, Observer { prList ->
            if (!prList.isNullOrEmpty()) {
                val spinnerAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item, prList
                )
                prsSpinner.adapter = spinnerAdapter
                prsSpinner.isEnabled = true
            } else {
                val spinnerAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    arrayListOf("Repository has no PRs")
                )
                prsSpinner.adapter = spinnerAdapter
                prsSpinner.isEnabled = false
            }
        })


        viewModel.commentsLd.observe(this, Observer { commentsList ->
            if (!commentsList.isNullOrEmpty()) {
                val spinnerAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, commentsList)
                commentsSpinner.adapter = spinnerAdapter
                commentsSpinner.isEnabled = true
                commentET.isEnabled = true
                postCommentButton.isEnabled = true
            } else {

                val spinnerAdapter = ArrayAdapter(
                    this, android.R.layout.simple_spinner_dropdown_item,
                    arrayListOf("PRs has no comment")
                )
                commentsSpinner.adapter = spinnerAdapter
                commentsSpinner.isEnabled = false
                commentET.isEnabled = false
                postCommentButton.isEnabled = false

            }
        })

        viewModel.postCommentLd.observe(this, Observer { success ->
            if (success) {
                commentET.setText("")
                Toast.makeText(this, "Comment Created", Toast.LENGTH_SHORT).show()
                token?.let {
                    val currentRepo = repositoriesSpinner.selectedItem as GithubRepo
                    val currentPr = prsSpinner.selectedItem as GitHubPr
                    viewModel.onLoadComments(
                        it,
                        currentPr?.user?.login,
                        currentRepo.name,
                        currentPr.number
                    )
                }
            } else {
                Toast.makeText(this, "Cannot  Create Comment", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.errorLd.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
    }

    fun onAuthenticate(view: View) {
        val oauthUrl = getString(R.string.oauthUrl)
        val clientId = getString(R.string.clientId)
        val callBackUrl = getString(R.string.callbackUrl)
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("$oauthUrl?client_id=$clientId&scope=repo&redirect_uri=$callBackUrl")
        )
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val uri = intent.data
        val callbackUrl = getString(R.string.callbackUrl)
        if (uri != null && uri.toString().startsWith(callbackUrl)) {
            val code = uri.getQueryParameter("code")

            code?.let {
                val clientId = getString(R.string.clientId)
                val clientSecret = getString(R.string.clientSecret)
                viewModel.getToken(clientId, clientSecret, code)
            }
        }
    }

    fun onLoadRepos(view: View) {

        token?.let {
            viewModel.onLoadRepositories(it)
        }
    }

    fun onPostComment(view: View) {

        val comments = commentET.text.toString()
        if (comments.isNotEmpty()) {
            val currentRepo = repositoriesSpinner.selectedItem as GithubRepo
            val currentPr = prsSpinner.selectedItem as GitHubPr
            token?.let {
                viewModel.onPostComment(
                    it, currentRepo, currentPr.number,
                    GitHubComments(comments, null.toString())
                )
            }

        } else {
            Toast.makeText(this, "Please Enter a Comment", Toast.LENGTH_SHORT).show()
        }

    }

}
