package org.rekotlinexample.github.asyntasks

import io.reactivex.Single
import org.rekotlinexample.github.actions.LoginResultAction
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.apirequests.GitHubApiService
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.middleware.GHLoginObservableType
import org.rekotlinexample.github.states.LoggedInState
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store
import java.text.DateFormat
import java.text.SimpleDateFormat


/**
 * Created by Mohanraj Karatadipalayam on 21/11/17.
 */


class GHLoginTask (val mEmail: String,
                   val mPassword: String,
                   var githubService: GitHubApi = GitHubApiService()) {

    var mGHLoginSingle = Single.fromCallable {
        Pair(githubService.createToken(mEmail, mPassword), mainStore as Store<StateType>)
    }.map { it  ->
        val loginResultAction = LoginResultAction(it.first)
        loginResultAction.createdAt = SimpleDateFormat("MMM dd, yyy").format(it.first.createdAt)
        val ghLoginObservable = Pair(loginResultAction,it.second)
        ghLoginObservable
    }


    fun getGHLoginObserver(): Single<GHLoginObservableType> {
        return mGHLoginSingle
    }
}