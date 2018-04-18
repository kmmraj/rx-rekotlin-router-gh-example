package org.rekotlinexample.github.middleware

import android.content.Context
import android.content.SharedPreferences
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.middleware.LoginMiddleWare.getGHLoginSingleSubscriber
import org.rekotlinexample.github.states.LoggedInState
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tw.geothings.rekotlin.Action
import tw.geothings.rekotlin.DispatchFunction
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store
import java.util.concurrent.TimeUnit


@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TestGitHubMiddleware{

    internal data class TestState(var name:String? = null, var password:String? = null):StateType
    //internal class TestAction(var name:String? = null, var password:String? = null): Action
    internal class TestStateReducer {
        lateinit var mAction: Action
        fun handleAction(action: Action, state: TestState?): TestState {
            @Suppress("NAME_SHADOWING")
            val state = state ?: TestState()
            mAction = action
            return state
        }
    }

    private lateinit var dispatch: ((Action) -> Unit)
    private val testStateReducer = TestStateReducer()
    private lateinit var testStore: Store<TestState>

    @Before
    fun setUp(){

         testStore = Store(
                reducer = testStateReducer::handleAction,
                state = TestState(),
                middleware = arrayListOf()
        )
        dispatch = testStore::dispatch

    }


    @Test // @DisplayName("Verify executeGitHubLogin function dispatches StartLoginAction")
    fun test_executeGitHubLogin_dispatches_StartLoginAction() {
        //Given

        val mockSharedPrefs = Mockito.mock(SharedPreferences::class.java)
        val mockSharedPrefsEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        val mockContext = Mockito.mock(Context::class.java)
        testAppContext = mockContext
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPrefs)
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockSharedPrefsEditor)
        Mockito.`when`(mockSharedPrefsEditor.putString(anyString(), anyString())).thenReturn(mockSharedPrefsEditor)
        Mockito.`when`(mockSharedPrefsEditor.commit()).thenReturn(true)

        //When
        executeGitHubLogin(LoginAction("test","test"),dispatch = dispatch as DispatchFunction)


        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted { Runnable { assertThat(testStateReducer.mAction).isInstanceOf(LoginStartedAction::class.java) } }


    }

    @Test // @DisplayName("Verify loginTaskListenerMiddleware function dispatches LoggedInDataSaveAction")
    fun test_loginTaskListenerMiddleware_dispatches_LoggedInDataSaveAction() {
        //Given
        val loginCompletedAction = LoginCompletedAction(userName = "test",
                token = "181816",
                loginStatus = LoggedInState.loggedIn)
        val loginTaskListenerMiddleware = LoginTaskListenerMiddleware()

        //When
        loginTaskListenerMiddleware.onFinished(loginCompletedAction, store = testStore as Store<StateType>)

        //Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoggedInDataSaveAction::class.java)
    }

    @Test // @DisplayName("Verify loginTaskListenerMiddleware function dispatches LoginFailedAction")
    fun test_loginTaskListenerMiddleware_dispatches_LoginFailedAction() {
        //Given
        val loginCompletedAction = LoginCompletedAction(userName = "test",
                message = "Error Message",
                loginStatus = LoggedInState.notLoggedIn)
        val loginTaskListenerMiddleware = LoginTaskListenerMiddleware()

        //When
        loginTaskListenerMiddleware.onFinished(loginCompletedAction, store = testStore as Store<StateType>)

        //Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoginFailedAction::class.java)
    }

    @Test // @DisplayName("Verify executeGitHubRepoListRetrieval function dispatches RepoListRetrivalStartedAction")
    fun test_executeGitHubRepoListRetrieval_dispatches_RepoListRetrivalStartedAction() {
        //Given
        val repoDetailListAction = RepoDetailListAction(userName = "test", token = "1818186")


        //When
        val executionResult = executeGitHubRepoListRetrieval(action = repoDetailListAction, dispatch = dispatch as DispatchFunction)

        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted {
            Runnable {
                assertThat(testStateReducer.mAction).isInstanceOf(RepoListRetrivalStartedAction::class.java)
                assertThat(executionResult).isTrue()
            }
        }


    }

    @Test // @DisplayName("Verify executeGitHubRepoListRetrieval when token is null")
    fun test_executeGitHubRepoListRetrieval_when_token_is_null() {
        //Given
        val repoDetailListAction = RepoDetailListAction(userName = "test", token = null)

        //When
        val executionResult = executeGitHubRepoListRetrieval(action = repoDetailListAction, dispatch = dispatch as DispatchFunction)

        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted { Runnable { assertThat(executionResult).isFalse() } }

    }

    @Test // @DisplayName("Verify executeGitHubRepoListRetrieval when context is not null")
    fun test_executeGitHubRepoListRetrieval_when_context_is_not_null() {
        //Given
        val repoDetailListAction = RepoDetailListAction(userName = "test", token = null)
        val sharedPrefs = Mockito.mock(SharedPreferences::class.java)
        val context = Mockito.mock(Context::class.java)
        testAppContext = context
        Mockito.`when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        Mockito.`when`(sharedPrefs.getString(anyString(), ArgumentMatchers.isNull())).thenReturn("teststring")

        //When
        val executionResult = executeGitHubRepoListRetrieval(action = repoDetailListAction, dispatch = dispatch as DispatchFunction)

        //Then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted { Runnable { assertThat(executionResult).isTrue() } }

    }


    @Test //@DisplayName("Validate the Subscriber when the login status is not logged in")
    fun test_subscriber_when_login_status_is_notLoggedin(){
        // Given
        val loginCompletedAction = LoginResultAction(userName = "test",
                message = "Error Message",
                loginStatus = LoggedInState.notLoggedIn)
        val subscriber = getGHLoginSingleSubscriber()
        val pair = GHLoginObservableType(loginCompletedAction,testStore as Store<StateType>)

        // When
       subscriber.onSuccess(pair)

        // Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoginFailedAction::class.java)
    }

    @Test //@DisplayName("Validate the Subscriber when the login status is logged in")
    fun test_subscriber_when_login_status_is_Loggedin(){
        // Given
        val loginCompletedAction = LoginResultAction(userName = "test",
                token = "969696969696969696",
                loginStatus = LoggedInState.loggedIn)
        val subscriber = getGHLoginSingleSubscriber()
        val pair = GHLoginObservableType(loginCompletedAction,testStore as Store<StateType>)

        // When
        subscriber.onSuccess(pair)

        // Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoggedInDataSaveAction::class.java)
    }


    @Test  // @DisplayName("Verify Subscription when passed success dispatches LoggedInDataSaveAction")
    fun test_subscription_when_passed_success_dispatches_LoggedInDataSaveAction(){

        //Given
        val loginResultAction = LoginResultAction(userName = "test",
                loginStatus = LoggedInState.loggedIn,
                token = "161816181618")

        // When
        val ghLoginSubscriber = getGHLoginSingleSubscriber()
        ghLoginSubscriber.onSuccess(Pair(loginResultAction, testStore as Store<StateType>))

        // Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoggedInDataSaveAction::class.java)

    }

    @Test  // @DisplayName("Verify Subscription when passed success dispatches LoggedInDataSaveAction")
    fun test_subscription_when_passed_error_dispatches_LoggedInDataSaveAction(){

        //Given
        val loginResultAction = LoginResultAction(userName = "test",
                message = "Some Error Message",
                loginStatus = LoggedInState.notLoggedIn)

        // When
        val ghLoginSubscriber = getGHLoginSingleSubscriber()
        ghLoginSubscriber.onSuccess(Pair(loginResultAction, testStore as Store<StateType>))

        // Then
        assertThat(testStateReducer.mAction).isInstanceOf(LoginFailedAction::class.java)

    }
}