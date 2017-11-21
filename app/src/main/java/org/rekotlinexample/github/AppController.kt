package org.rekotlinexample.github

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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

private var mInstance: AppController? = null
var router: Router<GitHubAppState>? = null

class AppController : Application() {

    //Creating sharedpreferences object
    //We will store the user data in sharedpreferences
    private val sharedPreference: SharedPreferences by lazy {
        PreferenceApiService.getSharedPreferenceByName(context = applicationContext,
                sharedPreferenceKey = PreferenceApiService.GITHUB_PREFS_NAME)
    }



    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        mInstance = this
        instance = this
        val loginState = getLogedInState()

        val authenticationState = AuthenticationState(loggedInState = loginState.loginStatus,
                userName = loginState.userName)
        val state = GitHubAppState(navigationState = NavigationState(),
                authenticationState = authenticationState,
                repoListState = RepoListState())
        mainStore = Store(state = state,
//                reducer = ::loginReducer,
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



    fun persistUserDetails(email: String,token:String) {
        var editor = sharedPreference.edit()
        editor.putString(PreferenceApiService.GITHUB_PREFS_NAME, email)
        editor.putString(PreferenceApiService.GITHUB_PREFS_KEY_TOKEN, token)
        editor.putBoolean(PreferenceApiService.GITHUB_PREFS_KEY_LOGINSTATUS, true)
        editor.apply()
    }

    //This method will clear the sharedpreference
    //It will be called on logout
    fun clearUserDetails() {
        val editor = sharedPreference.edit()
        editor.clear()
        editor.apply()
    }

    fun getLogedInState(): LoggedInDataSaveAction {
        val token = PreferenceApiService.getPreference(applicationContext, PreferenceApiService.GITHUB_PREFS_KEY_TOKEN)
//        var loginState: LoggedInState = LoggedInState.notLoggedIn
//        if (mToken != null){
//            loginState = LoggedInState.loggedIn
//        }
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

        //Getting tag it will be used for displaying log and it is optional
        val TAG = AppController::class.java.simpleName


        //Creating class object
        //Public static method to get the instance of this class
        @get:Synchronized var instance: AppController? = null
            private set

        fun getAppController(context: Context): AppController {
            if (instance == null) {
                //Create instance
                instance = AppController()
            }

            return instance as AppController
        }
    }


}
