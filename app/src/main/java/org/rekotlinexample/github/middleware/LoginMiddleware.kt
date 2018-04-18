package org.rekotlinexample.github.middleware

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.rekotlinexample.github.actions.*
import org.rekotlinexample.github.apirequests.MockGitHubApiService
import org.rekotlinexample.github.asyntasks.GHLoginTask
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.states.LoggedInState
import tw.geothings.rekotlin.DispatchFunction
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store

/**
 * Created by Mohanraj Karatadipalayam on 18/04/18.
 */
object LoginMiddleWare : LifecycleObserver {

    val TAG = "LoginMiddleWare"
//    lateinit var compositeDisposable: CompositeDisposable

    fun executeGitHubLoginTask(action: LoginAction, dispatch: DispatchFunction) {

    val ghLoginTask = GHLoginTask(action.userName, action.password)
        whenTestDebug { ghLoginTask.githubService = MockGitHubApiService() }
    ghLoginTask.getGHLoginObservable().subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeWith(getGHLoginSingleSubscriber())

    dispatch(LoginStartedAction(action.userName))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun init() {
        Log.d(TAG, "Inside init function")
//        compositeDisposable = CompositeDisposable()
        // TODO: - Create a disposable out of the trait and dispose it
//        compositeDisposable.add( )
//        getGHLoginSingleSubscriber().onSubscribe()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun destroy(){
        Log.d(TAG, "Inside destroy function")
//        compositeDisposable.dispose()
    }


    fun getGHLoginSingleSubscriber(): SingleObserver<GHLoginObservableType> {
    return object : SingleObserver<Pair<LoginResultAction, Store<StateType>>> {

        override fun onSubscribe(d: Disposable) {
//            compositeDisposable.add(d)
        }

        override fun onSuccess(value: GHLoginObservableType) {

            val (loginResultAction,store) = value
            if (loginResultAction.loginStatus == LoggedInState.notLoggedIn) {
                store.dispatch(LoginFailedAction(userName = loginResultAction.userName,
                        message = loginResultAction.message as String))

            } else {
                store.dispatch(LoginCompletedAction(loginResultAction))
                store.dispatch(LoggedInDataSaveAction(userName = loginResultAction.userName,
                        token = loginResultAction.token as String, loginStatus = LoggedInState.loggedIn))
            }

        }

        override fun onError(e: Throwable) {
            mainStore.dispatch(LoginFailedAction(userName = "",
                    message = "Internal Error"))
        }
    }
}

}