package org.rekotlinexample.github.controllers

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import org.rekotlinexample.github.R
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.routes.repoDetailRoute
import org.rekotlinrouter.Route
import org.rekotlinrouter.SetRouteAction

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val myWebView = findViewById(R.id.webview) as WebView
        val repo = intent.getParcelableExtra<RepoViewModel>("repo")
        myWebView.loadUrl(repo.htmlUrl)
    }

    override fun onBackPressed() {
        val currentRoute: Route = mainStore.state.navigationState.route.clone() as Route
        if(currentRoute.last() == repoDetailRoute) {
             currentRoute.remove(repoDetailRoute)
        }
        val action = SetRouteAction(route = currentRoute)
        mainStore.dispatch(action)
        super.onBackPressed()
    }

}
