package org.rekotlinexample.github.controllers

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.rekotlinexample.github.R
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.middleware.LoginMiddleWare
import org.rekotlinexample.github.routes.loginRoute
import org.rekotlinexample.github.routes.repoListRoute
import org.rekotlinexample.github.routes.welcomeRoute
import org.rekotlinexample.github.states.GitHubAppState
import org.rekotlinrouter.SetRouteAction
import tw.geothings.rekotlin.StoreSubscriber


class WelcomeActivity : AppCompatActivity(), StoreSubscriber<GitHubAppState> {

    val TAG = "WelcomeActivity"

    private val mTvWelcomeUser: TextView by lazy {
        this.findViewById<TextView>(R.id.tv_user_name) as TextView
    }

    private val mTvRepoCountDescription: TextView by lazy {
        this.findViewById<TextView>(R.id.tv_repo_count_description) as TextView
    }

    private val mTvUserCreatedAt: TextView by lazy {
        this.findViewById<TextView>(R.id.tv_createdAt) as TextView
    }

    private val mBtnViewRepo: Button by lazy {
        this.findViewById<Button>(R.id.btn_viewRepo) as Button
    }
    private val mTvLocation: TextView by lazy {
        this.findViewById<TextView>(R.id.tv_location) as TextView
    }

    override fun newState(state: GitHubAppState) {
        mTvWelcomeUser.text = "Welcome ".plus(state.authenticationState.fullName)
        mTvRepoCountDescription.text = "You have ${state.repoListState.repoList?.size.toString()} repositories"
        mTvUserCreatedAt.text = "User created on ${state.authenticationState.createdAt.toString()}"
        mTvLocation.text = state.authenticationState.location
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        mainStore.subscribe(this){ it.skipRepeats()}

        mTvRepoCountDescription.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startRepoListActivity()
                return@OnTouchListener true
            }
            false
        })

        mBtnViewRepo.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startRepoListActivity()
                return@OnTouchListener true
            }
            false
        })

       // LoginMiddleWare.init()
    }

    fun startRepoListActivity(){
        //Log.d(TAG,"startRepoListActivity")
        val routes = arrayListOf(loginRoute, welcomeRoute, repoListRoute)
        val action = SetRouteAction(route = routes)
        mainStore.dispatch(action)
    }
    override fun onDestroy() {
        mainStore.unsubscribe(this)
        super.onDestroy()
    }

    // Hack - Null override
    override fun onBackPressed() = Unit
}
