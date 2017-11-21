package org.rekotlinexample.github.routes


import android.content.Context
import android.content.Intent
import android.util.Log
import org.rekotlinexample.github.controllers.RepoListActivity
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.controllers.WebViewActivity
import org.rekotlinexample.github.controllers.WelcomeActivity
import org.rekotlinexample.github.mainStore
import org.rekotlinrouter.Routable
import org.rekotlinrouter.RouteElementIdentifier
import org.rekotlinrouter.RoutingCompletionHandler

/**
* Created by Mohanraj Karatadipalayam on 14/10/17.
*/
val loginRoute: RouteElementIdentifier = "LoginActivity"
val repoListRoute: RouteElementIdentifier = "RepoListActivity"
val welcomeRoute: RouteElementIdentifier = "WelcomeActivity"
val repoDetailRoute: RouteElementIdentifier = "WebViewActivity"

// Routes helper methods

object RoutableHelper {

     fun createWelcomeRoutable(context: Context): WelcomeRoutable {
        val welcomeIntent = Intent(context, WelcomeActivity::class.java)
        welcomeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(welcomeIntent)
        return WelcomeRoutable(context)
    }

    fun createRepoListRoutable(context: Context): RepoListRoutable {
        val repoListIntent = Intent(context, RepoListActivity::class.java)
        repoListIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(repoListIntent)
        return RepoListRoutable(context)
    }

    fun createRepoDetailRoutable(context: Context): RepoDetailRoutable {
        val repoDetailIntent = Intent(context, WebViewActivity::class.java)
        val currentRoute = mainStore.state.navigationState.route
        val intentData = mainStore.state.navigationState.getRouteSpecificState<RepoViewModel>(currentRoute)
        repoDetailIntent.putExtra("repo",intentData)
        repoDetailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(repoDetailIntent)
        return RepoDetailRoutable(context)
    }
}


class RootRoutable(val context: Context): Routable {
    override fun popRouteSegment(routeElementIdentifier: RouteElementIdentifier,
                                 animated: Boolean,
                                 completionHandler: RoutingCompletionHandler) {
    }

    override fun pushRouteSegment(routeElementIdentifier: RouteElementIdentifier,
                                  animated: Boolean,
                                  completionHandler: RoutingCompletionHandler): Routable {
        if(routeElementIdentifier == loginRoute) {
            return LoginRoutable(context)
        } else if (routeElementIdentifier == welcomeRoute) {
            return RoutableHelper.createWelcomeRoutable(context)
        }

        return LoginRoutable(context)
    }

    override fun changeRouteSegment(from: RouteElementIdentifier,
                                    to: RouteElementIdentifier,
                                    animated: Boolean,
                                    completionHandler: RoutingCompletionHandler): Routable {
       TODO("not implemented")
    }

}

class LoginRoutable(val context: Context) : Routable {

    val TAG = "Router LoginRoutable"
    override fun changeRouteSegment(from: RouteElementIdentifier, to: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        if (from == repoListRoute && to == welcomeRoute) {
            return RoutableHelper.createWelcomeRoutable(context)
        } else{
            return this
        }

    }

    override fun popRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler) {
    }

    override fun pushRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        if(routeElementIdentifier == repoListRoute){
            return RoutableHelper.createRepoListRoutable(context)
        } else if (routeElementIdentifier == welcomeRoute) {
            return RoutableHelper.createWelcomeRoutable(context)
        } else {
            Log.d(TAG,"Fatal Errror --- start of arbitarty route")
            return RepoListRoutable(context)
        }

    }

}



class RepoListRoutable (val context: Context): Routable {
    val TAG = "Router RepoListRoutable"
    override fun changeRouteSegment(from: RouteElementIdentifier, to: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        TODO("not implemented")
    }

    override fun popRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler) {
        Log.d(TAG,"RepoListRoutable popRouteSegment")
    }

    override fun pushRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        if(routeElementIdentifier == repoDetailRoute){
           return RoutableHelper.createRepoDetailRoutable(context)
        }
        return RepoDetailRoutable(context)
    }

}

class RepoDetailRoutable (val context: Context): Routable {
    override fun changeRouteSegment(from: RouteElementIdentifier, to: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        TODO("not implemented")  
    }

    override fun popRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler) {
        TODO("not implemented")
    }

    override fun pushRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        TODO("not implemented")  
    }

}

class WelcomeRoutable (val context: Context): Routable {
    val TAG = "WelcomeRoutable"
    override fun changeRouteSegment(from: RouteElementIdentifier, to: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        TODO("not implemented")  
    }

    override fun popRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler) {
        Log.d(TAG, "DO Nothing --- Inside from popRouteSegment() routeElementIdentifier is ${routeElementIdentifier} ")
    }

    override fun pushRouteSegment(routeElementIdentifier: RouteElementIdentifier, animated: Boolean, completionHandler: RoutingCompletionHandler): Routable {
        if (routeElementIdentifier == repoListRoute) {
            return RoutableHelper.createRepoListRoutable(context)
        } else {
            TODO("not implemented")
            return RepoListRoutable(context)
        }
    }
}