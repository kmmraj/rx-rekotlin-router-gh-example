package org.rekotlinexample.github.middleware

import org.rekotlinexample.github.AppController
import org.rekotlinexample.github.BuildConfig
import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.apirequests.MockGitHubApiService
import org.rekotlinexample.github.asyntasks.RepoListTask
import org.rekotlinexample.github.asyntasks.UserLoginTask
import org.rekotlinexample.github.apirequests.PreferenceApiService
import org.rekotlinexample.github.apirequests.PreferenceApiService.GITHUB_PREFS_KEY_LOGINSTATUS
import org.rekotlinexample.github.apirequests.PreferenceApiService.GITHUB_PREFS_KEY_TOKEN
import org.rekotlinexample.github.apirequests.PreferenceApiService.GITHUB_PREFS_KEY_USERNAME
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import tw.geothings.rekotlin.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.rekotlinexample.github.asyntasks.GHLoginTask
import io.reactivex.SingleObserver
import org.rekotlinexample.github.mainStore


/**
 * Created by Mohanraj Karatadipalayam on 17/10/17.
 */


interface LoginTaskListenerInterface {
    fun onFinished(result: LoginCompletedAction,store: Store<StateType>)
}

class LoginTaskListenerMiddleware : LoginTaskListenerInterface {
    override fun onFinished(result: LoginCompletedAction, store: Store<StateType>) {

        if (result.loginStatus == LoggedInState.loggedIn ) {
            result.token?.let {
                store.dispatch(result)
                store.dispatch(LoggedInDataSaveAction(userName = result.userName,
                        token = result.token as String, loginStatus = LoggedInState.loggedIn))
            }
        } else {
            result.message?.let{
                store.dispatch(LoginFailedAction(userName = result.userName,
                        message = result.message as String))
            }

        }

    }
}

interface RepoListTaskListenerInterface {
    fun onFinished(result: RepoListCompletedAction,store: Store<StateType>)
}

class RepoListTaskListenerMiddleware: RepoListTaskListenerInterface {
    override fun onFinished(result: RepoListCompletedAction,store: Store<StateType>) {
        store.dispatch(result)
    }

}

// App context for UT, must be never set in app run
var testAppContext = AppController.instance?.applicationContext

internal val gitHubMiddleware: Middleware<GitHubAppState> = { dispatch, getState ->
    { next ->
        { action ->
            when (action) {
                is LoginAction -> {
                    // executeGitHubLogin(action, dispatch)
                    executeGitHubLoginTask(action,dispatch)
                }
                is LoggedInDataSaveAction -> {
                    executeSaveLoginData(action)
                }
                is RepoDetailListAction -> {
                    executeGitHubRepoListRetrieval(action,dispatch)
                }
            }

            next(action)

        }
    }
}

fun executeGitHubRepoListRetrieval(action: RepoDetailListAction,dispatch: DispatchFunction) : Boolean {

    var userName: String? = action.userName
    var token: String? = action.token
    val context = testAppContext ?: AppController.instance?.applicationContext
    context?.let {
        userName = PreferenceApiService.getPreference(context, GITHUB_PREFS_KEY_USERNAME)
        token = PreferenceApiService.getPreference(context, GITHUB_PREFS_KEY_TOKEN)
    }

    userName?.let {
        token?.let {
            val repoListTaskListenerMiddleware = RepoListTaskListenerMiddleware()
            val repoTask = RepoListTask(repoListTaskListenerMiddleware,
                    userName as String,
                    token as String)

            whenTestDebug {repoTask.githubService = MockGitHubApiService()}
            repoTask.execute()
            dispatch(RepoListRetrivalStartedAction())
            return true
        }
        return false
    }
    return false
}

private fun executeSaveLoginData(action: LoggedInDataSaveAction) {
    val context = testAppContext ?: AppController.instance?.applicationContext
    context?.let {
        PreferenceApiService.savePreference(context,
                GITHUB_PREFS_KEY_TOKEN, action.token)
        PreferenceApiService.savePreference(context,
                GITHUB_PREFS_KEY_USERNAME, action.userName)
        PreferenceApiService.savePreference(context,
                GITHUB_PREFS_KEY_LOGINSTATUS, LoggedInState.loggedIn.name)
    }
}

fun executeGitHubLogin(action: LoginAction, dispatch: DispatchFunction) {
    val loginTaskListenerMiddleware = LoginTaskListenerMiddleware()


    val authTask = UserLoginTask(loginTaskListenerMiddleware,
            action.userName,
            action.password )

    whenTestDebug { authTask.githubService = MockGitHubApiService() }

    authTask.execute()
    dispatch(LoginStartedAction(action.userName))
}

fun executeGitHubLoginTask(action: LoginAction, dispatch: DispatchFunction) {

    val ghLoginTask = GHLoginTask(action.userName,action.password)
    whenTestDebug { ghLoginTask.githubService = MockGitHubApiService() }

    ghLoginTask.getGHLoginObserver().subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(getGHLoginSingleSubscriber())

    dispatch(LoginStartedAction(action.userName))
}


typealias GHLoginObservableType = Pair<LoginResultAction,Store<StateType>>
private fun getGHLoginSingleSubscriber(): SingleObserver<GHLoginObservableType> {
    return object : SingleObserver<Pair<LoginResultAction,Store<StateType>>> {

        override fun onSubscribe(d: Disposable) {

        }

        override fun onSuccess(value: GHLoginObservableType) {

            val (loginResultAction,store) = value
            if (loginResultAction.loginStatus == LoggedInState.notLoggedIn) {
                store.dispatch(LoginFailedAction(userName = loginResultAction.userName,
                        message = loginResultAction.message as String))

            } else {
                store.dispatch(LoginCompletedAction(loginResultAction))
                store.dispatch(LoggedInDataSaveAction(userName = loginResultAction.userName,
                        token = loginResultAction.token as String, loginStatus = LoggedInState.loggedIn))
            }

        }

        override fun onError(e: Throwable) {
            mainStore.dispatch(LoginFailedAction(userName = "",
                    message = "Internal Error"))
        }
    }
}

fun whenTestDebug(body:(() -> Unit)){
    if(BuildConfig.ENABLE_MOCKS){
        body()
    }
}


