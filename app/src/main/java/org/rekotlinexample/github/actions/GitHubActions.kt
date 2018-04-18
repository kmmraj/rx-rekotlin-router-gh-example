package org.rekotlinexample.github.actions

import org.kohsuke.github.GHRepository
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import tw.geothings.rekotlin.Action
import tw.geothings.rekotlin.Store
import java.util.*

/**
 * Created by Mohanraj Karatadipalayam on 14/10/17.
 */

data class LoginAction(val userName: String, val password: String) : Action
data class LoginStartedAction(val userName: String) : Action
data class LoginCompletedAction(val userName: String,
                                var token: String? = null,
                                var loginStatus: LoggedInState = LoggedInState.loggedIn,
                                var message:String? = null,
                                var fullName:String? = null,
                                var location:String? = null,
                                var avatarUrl:String? = null,
                                var createdAt:String? = null) : Action {
    constructor(loginResultAction: LoginResultAction): this(userName = loginResultAction.userName){
        this.fullName = loginResultAction.fullName
        this.avatarUrl = loginResultAction.avatarUrl
        this.createdAt = loginResultAction.createdAt
        this.location = loginResultAction.location
        this.loginStatus = loginResultAction.loginStatus
        this.token = loginResultAction.token
    }
}
data class LoggedInDataSaveAction(val userName: String,
                                  val token: String,
                                  val loginStatus: LoggedInState,
                                  var fullName:String? = null,
                                  var location:String? = null,
                                  var avatarUrl:String? = null,
                                  var createdAt:Date? = null) : Action
data class LoginFailedAction(val userName: String,val message: String): Action
data class LoginResultAction(val userName: String,
                             var token:String? = null,
                             var loginStatus: LoggedInState = LoggedInState.notLoggedIn,
                             var message:String? = null,
                             var fullName:String? = null,
                             var location:String? = null,
                             var avatarUrl:String? = null,
                             var createdAt:String? = null) : Action {
    constructor(loginDataModel: LoginDataModel): this (userName = loginDataModel.userName){
        this.fullName = loginDataModel.fullName
        this.avatarUrl = loginDataModel.avatarUrl
        this.createdAt = loginDataModel.createdAt.toString()
        this.location = loginDataModel.location
        this.loginStatus = loginDataModel.loginStatus
        this.token = loginDataModel.token
    }
}
class RepoDetailListAction (val userName: String? = null, var token:String? = null) : Action
class RepoListRetrivalStartedAction : Action
data class RepoListCompletedAction(val repoList: List<RepoViewModel>): Action

class LoginDataModel(val userName: String,
                     var token:String? = null,
                     var loginStatus: LoggedInState = LoggedInState.notLoggedIn,
                     var message:String? = null,
                     var fullName:String? = null,
                     var location:String? = null,
                     var avatarUrl:String? = null,
                     var createdAt:Date? = Date())

