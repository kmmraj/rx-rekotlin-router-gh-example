package org.rekotlinexample.github.asynctasks

import org.assertj.core.api.Assertions
import org.awaitility.Awaitility
import org.junit.Test
import org.junit.runner.RunWith
import org.rekotlinexample.github.actions.LoginCompletedAction
import org.rekotlinexample.github.actions.LoginResultAction
import org.rekotlinexample.github.actions.RepoListCompletedAction
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.asyntasks.RepoListTask
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.middleware.RepoListTaskListenerInterface
import org.rekotlinexample.github.states.LoggedInState
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store
import java.util.concurrent.TimeUnit

/**
 * Created by Mohanraj Karatadipalayam on 17/11/17.
 */

@Config(manifest= Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TestRepoListTask {
    @Test // @DisplayName("Verify RepoListTask dispatches RepoListCompletedAction(")
    fun test_RepoListTask_dispatches_RepoListCompletedAction() {

        // Given
        class TestMockGitHubApiService : GitHubApi {
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

            override fun createToken(username: String, password: String): LoginResultAction {
                TODO()
            }

        }

        class TestRepoListTaskListenerMiddleware : RepoListTaskListenerInterface {
            var action: RepoListCompletedAction? = null

            override fun onFinished(result: RepoListCompletedAction,store: Store<StateType>) {
                action = result
            }

        }

        val testRepoListTaskListenerMiddleware = TestRepoListTaskListenerMiddleware()
        val repoListTask = RepoListTask(testRepoListTaskListenerMiddleware, "test", "test")
        repoListTask.githubService = TestMockGitHubApiService()
        //When
        repoListTask.execute()
        //Then
        Assertions.assertThat(testRepoListTaskListenerMiddleware.action).isInstanceOf(RepoListCompletedAction::class.java)
    }

}