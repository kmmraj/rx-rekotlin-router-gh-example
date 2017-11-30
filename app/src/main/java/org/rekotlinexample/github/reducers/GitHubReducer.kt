package org.rekotlinexample.github.reducers

import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.states.AuthenticationState
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import org.rekotlinexample.github.states.RepoListState
import org.rekotlinrouter.NavigationReducer
import tw.geothings.rekotlin.Action


fun appReducer(action: Action, oldState: GitHubAppState?) : GitHubAppState {

    // if no state has been provided, create the default state
    val state = oldState ?: GitHubAppState(
            navigationState = NavigationReducer.handleAction(action = action, state = oldState?.navigationState),
            authenticationState = AuthenticationState(loggedInState = LoggedInState.loggedIn,
                    userName = ""),
            repoListState = RepoListState())

    return state.copy(
            navigationState = NavigationReducer.reduce(action = action, oldState = state.navigationState),
            authenticationState = (::authenticationReducer)(action, state.authenticationState),
            repoListState = (::repoListReducer)(action, state.repoListState))
}

fun authenticationReducer(action: Action, state: AuthenticationState?): AuthenticationState {

    val newState =  state ?: AuthenticationState(LoggedInState.notLoggedIn,userName = "")
    when (action) {

        is LoginStartedAction -> {
            return newState.copy(isFetching = true)
        }
        is LoginCompletedAction -> {
            return newState.copy(isFetching = false,
                    loggedInState = LoggedInState.loggedIn,
                    fullName = action.fullName,
                    createdAt = action.createdAt,
                    avatarUrl = action.avatarUrl,
                    location = action.location)
        }
        is LoginFailedAction -> {
            return newState.copy(isFetching = false,
                    loggedInState = LoggedInState.notLoggedIn,
                    errorMessage = action.message,
                    userName = action.userName)
        }
        is LoggedInDataSaveAction -> {
            return newState.copy(isCompleted = true,
                    isFetching = false,
                    loggedInState = LoggedInState.loggedIn)
        }
    }
    return newState
}

fun repoListReducer(action: Action, state: RepoListState?): RepoListState {
    val newState =  state ?: RepoListState()
    when (action) {
        is RepoListRetrivalStartedAction -> {
            return newState.copy(isFetching = true)
        }
        is RepoListCompletedAction -> {
            return newState.copy(isFetching = false,
                    isCompleted = true,
                    repoList = action.repoList)
        }
    }
    return newState
}

