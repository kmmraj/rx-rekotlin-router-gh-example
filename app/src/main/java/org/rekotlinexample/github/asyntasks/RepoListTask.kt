package org.rekotlinexample.github.asyntasks

import android.os.AsyncTask
import org.rekotlinexample.github.actions.RepoListCompletedAction
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.middleware.RepoListTaskListenerInterface
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.apirequests.GitHubApiService
import org.rekotlinexample.github.mainStore
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store

/**
 * Created by Mohanraj Karatadipalayam on 25/10/17.
 */

class RepoListTask(val repoListTaskListener: RepoListTaskListenerInterface,
                   val userName:String,
                   val token: String,
                   var githubService: GitHubApi = GitHubApiService()) : AsyncTask<Void, Void, Boolean>() {
    var mRepoList: List<RepoViewModel>? = null
    override fun doInBackground(vararg params: Void?): Boolean {
        mRepoList = githubService.getRepoList(userName = userName, token = token)
        return true
    }

    override fun onPostExecute(success: Boolean?) {
        if (success!!) {
            mRepoList?.let {
                repoListTaskListener.onFinished(RepoListCompletedAction(repoList = mRepoList as List<RepoViewModel>),
                        store = mainStore as Store<StateType>)
            }

        }
    }

}