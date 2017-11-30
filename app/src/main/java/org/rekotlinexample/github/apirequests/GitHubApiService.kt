package org.rekotlinexample.github.apirequests

/**
 * Created by Mohanraj Karatadipalayam on 03/10/17.
 */


import org.json.JSONObject
import org.kohsuke.github.*
import org.kohsuke.github.GHAuthorization.*
import org.rekotlinexample.github.actions.LoginDataModel
import org.rekotlinexample.github.actions.LoginResultAction
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.states.LoggedInState
import java.io.IOException
import java.lang.Exception


val GITHUB_URL = "https://api.github.com"
val GH_REQUIRED_SCOPE: List<String> = arrayListOf(REPO, REPO_STATUS,USER)



interface GitHubApi{
    fun createToken(username: String,
                    password: String): LoginDataModel
    fun getRepoList( userName:String, token:String): List<RepoViewModel>
}

class GitHubApiService : GitHubApi {



    override fun getRepoList( userName:String, token:String): List<RepoViewModel> {

        val github = GitHub.connect(userName,token)
        val repositories: PagedIterable<GHRepository> = github.myself.listRepositories(10)

        val repoList: List<GHRepository> = repositories.asList()

        var repoViewModelList = arrayListOf<RepoViewModel>()
        for( repo in repoList){
            val repoUrl = repo.htmlUrl.protocol.plus("://").plus(repo.htmlUrl.host).plus(repo.htmlUrl.path)
            val repoVM = RepoViewModel(repoName = repo.name,
                    watchers = repo.watchers,
                    stargazersCount = repo.stargazersCount,
                    language = repo.language ?: "",
                    forks = repo.forks,
                    description = repo.description ?: "",
                    htmlUrl = repoUrl)
            // TODO repo.pushedAt

            repoViewModelList.add(repoVM)

        }

        return repoViewModelList

    }






    @Throws(IOException::class)
    override fun createToken(username: String,
                             password: String): LoginDataModel {
        val gitHub = GitHubBuilder()
                .withEndpoint(GITHUB_URL)
                .withPassword(username, password)
                .build()

        // TODO - fix it with clientID and client Secret

        val dateTimeString: String = System.currentTimeMillis().toString()
        val note = "test-android-github-2A".plus(dateTimeString)

        var loginDataModel = LoginDataModel(userName = username)

        try {
            val ghAuthorization = gitHub.createToken(GH_REQUIRED_SCOPE, note, null)
            loginDataModel.token = ghAuthorization.getToken()
            loginDataModel.loginStatus = LoggedInState.loggedIn
            loginDataModel.fullName = gitHub.myself.name
            loginDataModel.createdAt = gitHub.myself.createdAt
            loginDataModel.location = gitHub.myself.location
        } catch (ex: GHFileNotFoundException){
            ex.printStackTrace()
            //{"message":"Bad credentials","documentation_url":"https://developer.github.com/v3"}
            loginDataModel.message = JSONObject(ex.message).get("message").toString()
        } catch (ex: Exception){
            ex.printStackTrace()
            loginDataModel.message = JSONObject(ex.message).get("message").toString()
        }

        return loginDataModel
    }

}



