package org.rekotlinexample.github.reducers

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.states.AuthenticationState
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinexample.github.states.LoggedInState
import org.rekotlinexample.github.states.RepoListState
import tw.geothings.rekotlin.Action

class TestGitHubReducer{

    @Test // @DisplayName("test when loginStartedAction returns isFetching as true")
    fun test_authenticationReducer_for_loginStartedAction(){
        //Given
        val loginStartedAction = LoginStartedAction("")
        val state = AuthenticationState(LoggedInState.notLoggedIn,isFetching = false,userName = "")

        // When
        val newState = authenticationReducer(loginStartedAction,state)

        // Then
        assertThat(newState.isFetching).isTrue()
    }

    @Test // @DisplayName("test loginCompletedAction")
    fun test_authenticationReducer_for_LoginCompletedAction(){
        //Given
        val loginCompletedAction = LoginCompletedAction("")
        val state = AuthenticationState(LoggedInState.notLoggedIn,isFetching = true,userName = "")

        // When
        val newState = authenticationReducer(loginCompletedAction,state)

        // Then
        assertThat(newState.isFetching).isFalse()
        assertThat(newState.isCompleted).isFalse()
    }

    @Test // @DisplayName("test LoggedInDataSaveAction")
    fun test_authenticationReducer_for_loggedInDataSaveAction(){
        //Given
        val loggedInDataSaveAction = LoggedInDataSaveAction(userName = "",
                loginStatus = LoggedInState.loggedIn,
                token = "1A2B")
        val state = AuthenticationState(LoggedInState.notLoggedIn,isFetching = true,userName = "")

        // When
        val newState = authenticationReducer(loggedInDataSaveAction,state)

        // Then
        assertThat(newState.isFetching).isFalse()
        assertThat(newState.isCompleted).isTrue()
        assertThat(newState.loggedInState).isEqualTo(LoggedInState.loggedIn)
    }

    @Test // @DisplayName("test loginFailedAction")
    fun test_authenticationReducer_for_loginFailedAction(){
        //Given
        val loginFailedAction = LoginFailedAction(userName = "", message = "Failure blah blah")
        val state = AuthenticationState(LoggedInState.loggedIn,isFetching = true,userName = "")

        // When
        val newState = authenticationReducer(loginFailedAction,state)

        // Then
        assertThat(newState.isFetching).isFalse()
        //assertThat(newState.isCompleted).isTrue()
        assertThat(newState.loggedInState).isEqualTo(LoggedInState.notLoggedIn)
        assertThat(newState.errorMessage).isNotNull()
        assertThat(newState.errorMessage).isNotBlank()
    }

    @Test // @DisplayName("test repoListRetrivalStartedAction")
    fun test_repoListReducer_for_repoListRetrivalStartedAction(){
        //Given
        val repoListRetrivalStartedAction = RepoListRetrivalStartedAction()
        val state = RepoListState()

        // When
        val newState = repoListReducer(repoListRetrivalStartedAction,state)

        // Then
        assertThat(newState.isFetching).isTrue()
    }

    @Test // @DisplayName("test repoListCompletedAction")
    fun test_repoListReducer_for_repoListCompletedAction(){
        //Given
        val repoListCompletedAction = RepoListCompletedAction(repoList = arrayListOf(RepoViewModel(),RepoViewModel()))
        val state = RepoListState()

        // When
        val newState = repoListReducer(repoListCompletedAction,state)

        // Then
        assertThat(newState.isFetching).isFalse()
        assertThat(newState.repoList?.size).isEqualTo(2)
    }

    @Test // @DisplayName("test appReducer does the initialization")
    fun test_appReducer(){
        //Given
        data class SomeAction(val name: String?=null): Action

        // When
        val newState = appReducer(SomeAction(),null)

        // Then
        assertThat(newState.authenticationState).isNotNull()
        assertThat(newState.navigationState).isNotNull()
        assertThat(newState.repoListState).isNotNull()
    }


}
