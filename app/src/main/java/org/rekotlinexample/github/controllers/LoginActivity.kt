package org.rekotlinexample.github.controllers


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.rekotlinexample.github.R
import org.rekotlinexample.github.actions.LoginAction
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.routes.loginRoute
import org.rekotlinexample.github.routes.repoListRoute
import org.rekotlinexample.github.states.AuthenticationState
import org.rekotlinexample.github.states.LoggedInState
import org.rekotlinrouter.SetRouteAction
import tw.geothings.rekotlin.StoreSubscriber


interface NextActivityHandler{
    fun startNextActivity()
}

class LoginActivity : Activity(), StoreSubscriber<AuthenticationState>, NextActivityHandler {


    // UI references.
    private val mETEmail: EditText by lazy {
        this.findViewById(R.id.email) as EditText
    }
    private val mETPassword: EditText by lazy {
       this.findViewById(R.id.password) as EditText
    }
    private val mViewProgress: View by lazy {
        this.findViewById(R.id.login_progress)
    }

//    private val mViewProgressContainer: View by lazy {
//        this.findViewById(R.id.progressBarContainer)
//    }
    private val mViewForm: View by lazy {
        this.findViewById(R.id.login_progress)
    }

    private val mEmailSignInButton: Button by lazy {
        this.findViewById(R.id.email_sign_in_button) as Button
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mEmailSignInButton.setOnClickListener {
            mainStore.dispatch(LoginAction(userName = mETEmail.text.toString(),
                    password = mETPassword.text.toString()))
        }

        mainStore.subscribe(this){
            it.select {
                it.authenticationState
            }.skipRepeats { oldState,newState ->
                oldState == newState
            }
        }


        initRoute()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainStore.unsubscribe(this)
    }

    private fun initRoute() {
        val routes = arrayListOf(loginRoute)
        val action = SetRouteAction(route = routes)
        mainStore.dispatch(action)
    }


    override fun newState(state: AuthenticationState) {
        if (state.isFetching) {
            ViewHelper.showProgress(show = true,
                        view = mViewForm,
                        progressView = mViewProgress,
                        resources = resources)
            } else {
                ViewHelper.showProgress(show = false,
                        view = mViewForm,
                        progressView = mViewProgress,
                        resources = resources)
            }

            if (isLoginSuccess(state)) {
                startRepoListActivity()
            } else {
                showLoginErrorToast(state)
            }
        }

    private fun showLoginErrorToast(state: AuthenticationState) {
        state.errorMessage?.let {
            val errorMessageToBeShown = state.userName
                    .plus(" : ")
                    .plus(state.errorMessage)
            Toast.makeText(applicationContext, errorMessageToBeShown, Toast.LENGTH_LONG).show()
        }
    }

    private fun isLoginSuccess(state: AuthenticationState): Boolean {
        if (state.loggedInState == LoggedInState.loggedIn &&
                state.isCompleted &&  !state.isFetching){
            return true
        } else if (state.loggedInState == LoggedInState.notLoggedIn
                && state.isCompleted &&  !state.isFetching){
            return false
        }
        return false
    }

    fun startRepoListActivity(){
        val routes = arrayListOf(loginRoute, repoListRoute)
        val action = SetRouteAction(route = routes)
        mainStore.dispatch(action)
    }


    override fun startNextActivity(){
         val repoListIntent = Intent(this, RepoListActivity::class.java)
         this.startActivity(repoListIntent)
    }

    }





