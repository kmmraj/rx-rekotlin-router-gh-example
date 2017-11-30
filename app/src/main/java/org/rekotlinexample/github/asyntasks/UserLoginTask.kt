package org.rekotlinexample.github.asyntasks

import android.os.AsyncTask
import org.rekotlinexample.github.actions.LoginCompletedAction
import org.rekotlinexample.github.actions.LoginResultAction
import org.rekotlinexample.github.middleware.LoginTaskListenerInterface
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.apirequests.GitHubApiService
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.states.LoggedInState
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store

/**
 * Created by Mohanraj Karatadipalayam on 08/11/17.
 */
class UserLoginTask (val loginTaskListener: LoginTaskListenerInterface,
                     val mEmail: String,
                     val mPassword: String,
                     var githubService: GitHubApi = GitHubApiService()) : AsyncTask<Void, Void, Boolean>() {

    var mLoginResultAction = LoginResultAction(userName = mEmail,
            loginStatus = LoggedInState.notLoggedIn)


    override fun doInBackground(vararg params: Void): Boolean? {
        val loginDataModel = githubService.createToken(mEmail, mPassword)
        mLoginResultAction = LoginResultAction(loginDataModel)
        return true
    }

    override fun onPostExecute(success: Boolean?) {

        if (success!!) {
            val loginCompletedAction: LoginCompletedAction?
            if (mLoginResultAction.loginStatus == LoggedInState.notLoggedIn) {
                loginCompletedAction = LoginCompletedAction(userName = mEmail,
                        token = null,
                        loginStatus = LoggedInState.notLoggedIn,
                        message = mLoginResultAction.message)

            } else {
                loginCompletedAction = LoginCompletedAction(loginResultAction = mLoginResultAction)
            }
            loginTaskListener.onFinished(loginCompletedAction, mainStore as Store<StateType>)
        }
    }

    override fun onCancelled() {
        //   create a callback and set it there
        //mAuthTask = null
        // showProgress(false)
    }
}