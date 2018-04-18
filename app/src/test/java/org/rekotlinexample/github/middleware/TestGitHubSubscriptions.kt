package org.rekotlinexample.github.middleware

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.rekotlinexample.github.actions.LoggedInDataSaveAction
import org.rekotlinexample.github.actions.LoginFailedAction
import org.rekotlinexample.github.actions.LoginResultAction
import org.rekotlinexample.github.states.LoggedInState
import tw.geothings.rekotlin.Action
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store


class TestGitHubSubscriptions{

    internal data class TestState(var name:String? = null, var password:String? = null): StateType
    internal class TestStateReducer {
        lateinit var mAction: Action
        fun handleAction(action: Action, state: TestState?): TestState {
            val newState = state ?: TestState()
            mAction = action
            return newState
        }
    }

    private val testStateReducer = TestStateReducer()
    private lateinit var testStore: Store<TestState>

    @Before
    fun setUp(){

        testStore = Store(
                reducer = testStateReducer::handleAction,
                state = TestState(),
                middleware = arrayListOf()
        )
    }



    @Test  // @DisplayName("Verify Subscription when passed success dispatches LoggedInDataSaveAction")
    fun test_subscription_when_passed_success_dispatches_LoggedInDataSaveAction(){

        //Given
        val loginResultAction = LoginResultAction(userName = "test",
                loginStatus = LoggedInState.loggedIn,
                token = "161816181618")

        // When
        val ghLoginSubscriber = LoginMiddleWare.getGHLoginSingleSubscriber()
        ghLoginSubscriber.onSuccess(Pair(loginResultAction, testStore as Store<StateType>))

        // Then
        Assertions.assertThat(testStateReducer.mAction).isInstanceOf(LoggedInDataSaveAction::class.java)

    }

    @Test  // @DisplayName("Verify Subscription when passed success dispatches LoggedInDataSaveAction")
    fun test_subscription_when_passed_notLoggedIn_dispatches_LoggedInDataSaveAction(){

        //Given
        val loginResultAction = LoginResultAction(userName = "test",
                message = "Some Error Message",
                loginStatus = LoggedInState.notLoggedIn)

        // When
        val ghLoginSubscriber = LoginMiddleWare.getGHLoginSingleSubscriber()
        ghLoginSubscriber.onSuccess(Pair(loginResultAction, testStore as Store<StateType>))

        // Then
        Assertions.assertThat(testStateReducer.mAction).isInstanceOf(LoginFailedAction::class.java)

    }
}