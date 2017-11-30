package org.rekotlinexample.github.apirequests

import org.rekotlinexample.github.actions.LoginDataModel
import org.rekotlinexample.github.actions.LoginResultAction
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.states.LoggedInState
import java.util.*

/**
 * Created by Mohanraj Karatadipalayam on 28/10/17.
 */

class MockGitHubApiService : GitHubApi {
    override fun getRepoList(userName: String, token: String): List<RepoViewModel> {

        val repo1 = RepoViewModel(repoName = "cleanAndroid"
                ,htmlUrl = "https://github.com/kmmraj/android-clean-code",
                forks = 1,
                stargazersCount = 9,
                language = "java",
                description = "Clean Code")
        val repo2 = RepoViewModel(repoName = "reKotlinRouter",
                htmlUrl = "https://github.com/kmmraj/ReKotlin",
                forks = 1,
                stargazersCount = 6,
                language = "kotlin",
                description = "Kotlin Code")
        return arrayListOf<RepoViewModel>(repo1,repo2)
    }

    override fun createToken(username: String, password: String): LoginDataModel {
        return LoginDataModel(userName = username,
                token = "181818181818181818181818181818",
                loginStatus = LoggedInState.loggedIn,
                fullName = "Mohanraj Karats",
                location = "Bengaluru",
                avatarUrl = "https://avatars2.githubusercontent.com/u/6253321",
                createdAt = Date()
        )
    }

}