package org.rekotlinexample.github.asynctasks

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.Test
import org.junit.runner.RunWith
import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.asyntasks.UserLoginTask
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.middleware.LoginTaskListenerInterface
import org.rekotlinexample.github.states.LoggedInState
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Mohanraj Karatadipalayam on 17/11/17.
 */

@Config(manifest= Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TestUserLoginTask{

    @Test // @DisplayName("Verify LoginTask when passed success returns LoggedInState_as_loggedIn")
    fun test_LoginTask_returns_LoggedInState_as_loggedIn() {

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

        class TestLoginTaskListenerMiddleware : LoginTaskListenerInterface {
            var mLoginCompletedAction: LoginCompletedAction? = null
            override fun onFinished(result: LoginCompletedAction, store: Store<StateType>) {
                mLoginCompletedAction = result

            }
        }

        val testLoginTaskListenerMiddleware = TestLoginTaskListenerMiddleware()
        val authTask = UserLoginTask(testLoginTaskListenerMiddleware, "test", "test")
        authTask.githubService = TestMockGitHubApiService()
        //When
        authTask.execute()
        //Then

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted { object : Runnable {
            override fun run() {
                assertThat(testLoginTaskListenerMiddleware.mLoginCompletedAction).isInstanceOf(LoginCompletedAction::class.java)
                assertThat(testLoginTaskListenerMiddleware.mLoginCompletedAction?.loginStatus).isEqualTo(LoggedInState.loggedIn)
            }
        }}
    }

    @Test // @DisplayName("Verify LoginTask when passed failure returns LoggedInState_as_notLoggedIn")
    fun test_LoginTask_returns_LoggedInState_as_notLoggedIn() {

        // Given
        class TestMockGitHubApiService : GitHubApi {
            override fun getRepoList(userName: String, token: String): List<RepoViewModel> {
                TODO()
            }

            override fun createToken(username: String, password: String): LoginDataModel {
                return LoginDataModel(userName = username,
                        loginStatus = LoggedInState.notLoggedIn,
                        message = "Error Message"
                )
            }

        }

        class TestLoginTaskListenerMiddleware : LoginTaskListenerInterface {
            var action: LoginCompletedAction? = null
            override fun onFinished(result: LoginCompletedAction, store: Store<StateType>) {
                action = result

            }
        }

        val testLoginTaskListenerMiddleware = TestLoginTaskListenerMiddleware()
        val authTask = UserLoginTask(testLoginTaskListenerMiddleware, "test", "test")
        authTask.githubService = TestMockGitHubApiService()
        //When
        authTask.execute()
        //Then

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted { object : Runnable {
            override fun run() {
                assertThat(testLoginTaskListenerMiddleware.action).isInstanceOf(LoginCompletedAction::class.java)
                assertThat(testLoginTaskListenerMiddleware.action?.loginStatus).isEqualTo(LoggedInState.notLoggedIn)
            }
        }}
    }


}