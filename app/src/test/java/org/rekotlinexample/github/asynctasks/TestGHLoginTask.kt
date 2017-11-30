package org.rekotlinexample.github.asynctasks

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.rekotlinexample.github.actions.LoginDataModel
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.asyntasks.GHLoginTask
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.states.LoggedInState
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.*


/**
* Created by Mohanraj Karatadipalayam on 28/11/17.
*/

@Config(manifest= Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TestGHLoginTask {

    @Test
    fun testGHLoginObservable(){

        // Given
        class TestMockGitHubApiService : GitHubApi {
            override fun getRepoList(userName: String, token: String): List<RepoViewModel> {
                TODO()
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

        val ghLoginTask = GHLoginTask("test","test")
        ghLoginTask.githubService = TestMockGitHubApiService()

        // When
        val ghLoginObserver = ghLoginTask.getGHLoginObserver().test()

        // Then
        val values = ghLoginObserver.values()
        assertThat(values.first().first.createdAt).isEqualTo(SimpleDateFormat("MMM dd, yyy").format(Date()))

    }

}