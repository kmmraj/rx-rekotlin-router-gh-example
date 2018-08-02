package org.rekotlinexample.github

import android.app.Application
import android.support.multidex.MultiDexApplication
import com.squareup.leakcanary.LeakCanary
import org.rekotlinexample.github.actions.LoggedInDataSaveAction
import org.rekotlinexample.github.middleware.gitHubMiddleware
import org.rekotlinexample.github.reducers.appReducer
import org.rekotlinexample.github.routes.RootRoutable

import org.rekotlinexample.github.apirequests.PreferenceApiService
import org.rekotlinexample.github.states.AuthenticationState
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import org.rekotlinexample.github.states.RepoListState
import org.rekotlinrouter.NavigationState
import org.rekotlinrouter.Router
import tw.geothings.rekotlin.Store

/**
 * Created by Mohanraj Karatadipalayam on 15/10/17.
 */

var mainStore = Store(state = null,
        reducer = ::appReducer,
        middleware = arrayListOf(gitHubMiddleware))
        //middleware = emptyList())

var router: Router<GitHubAppState>? = null

class AppController : MultiDexApplication() {


    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)


        mInstance = this
        val loginState = getLogedInState()

        val authenticationState = AuthenticationState(loggedInState = loginState.loginStatus,
                userName = loginState.userName)
        val state = GitHubAppState(navigationState = NavigationState(),
                authenticationState = authenticationState,
                repoListState = RepoListState())
        mainStore = Store(state = state,
                reducer = ::appReducer,
                middleware = arrayListOf(gitHubMiddleware),
                automaticallySkipRepeats = true)
        router = Router(store = mainStore,
                rootRoutable = RootRoutable(context = applicationContext),
                stateTransform = { subscription ->
                    subscription.select { stateType ->
                        stateType.navigationState
                    }
                })

    }


    fun getLogedInState(): LoggedInDataSaveAction {
        val token = PreferenceApiService.getPreference(applicationContext, PreferenceApiService.GITHUB_PREFS_KEY_TOKEN)

        val userName: String? = PreferenceApiService.getPreference(applicationContext, PreferenceApiService.GITHUB_PREFS_KEY_USERNAME)

        val loginStateString = PreferenceApiService.getPreference(applicationContext, PreferenceApiService.GITHUB_PREFS_KEY_LOGINSTATUS)

        var loginState = LoggedInState.notLoggedIn
        if (loginStateString === LoggedInState.loggedIn.name){
            loginState = LoggedInState.loggedIn
        }
        val savedLoginDataAction = LoggedInDataSaveAction(userName = userName?:"",
                token = token?:"",
                loginStatus = loginState)
        return savedLoginDataAction
    }

    companion object {

        @get:Synchronized var mInstance: AppController? = null
            private set

    }


}
