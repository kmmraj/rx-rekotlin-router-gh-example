package org.rekotlinexample.github.asyntasks

import ListOfReposQuery
import android.net.ParseException
import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.response.CustomTypeAdapter
import com.apollographql.apollo.response.CustomTypeValue
import com.apollographql.apollo.rx2.Rx2Apollo
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import okhttp3.OkHttpClient
import org.rekotlinexample.github.actions.RepoListCompletedAction
import org.rekotlinexample.github.apirequests.GitHubApi
import org.rekotlinexample.github.apirequests.GitHubApiService
import org.rekotlinexample.github.controllers.RepoViewModel
import org.rekotlinexample.github.mainStore
import org.rekotlinexample.github.middleware.GHRepoListObservableType
import org.rekotlinexample.github.middleware.RepoListTaskListenerInterface
import tw.geothings.rekotlin.StateType
import tw.geothings.rekotlin.Store
import type.CustomType
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Mohanraj Karatadipalayam on 31/07/18.
 */



class RepoListGQLTask (val userName:String,
                       val token: String,
                       var githubService: GitHubApi = GitHubApiService(),
                       val repoListTaskListener: RepoListTaskListenerInterface) {

    private val BASE_URL = "https://api.github.com/graphql"
    private lateinit var apolloClient: ApolloClient
    private val TAG = "RepoListGQLTask"

    fun getRepos() {
        apolloClient = setupApollo(token)
        apolloClient.query(ListOfReposQuery
                .builder()
                .login(userName)
                .first(5)
                .build())
                .enqueue(object : ApolloCall.Callback<ListOfReposQuery.Data>() {

                    override fun onFailure(e: ApolloException) {
                        Log.d(TAG,e.message.toString())
                    }

                    override fun onResponse(response: Response<ListOfReposQuery.Data>) {
                        //TODO: Start here
                        val repositories: ListOfReposQuery.Repositories? = response.data()?.user()?.repositories()
                        val repoViewModelList = arrayListOf<RepoViewModel>()

                        repositories?.nodes()?.size
                        repositories?.let {
                            it.nodes()?.let {
                                for(repo in it){
                                    repo.createdAt()

                                    val repoUrl = repo.url()
                                    val repoVM = RepoViewModel(repoName = repo.name(),
                                            watchers = repo.watchers().totalCount(),
                                            stargazersCount = repo.stargazers().totalCount(),
                                            language = repo.primaryLanguage()?.toString() ?: "",
                                            forks = repo.forkCount(),
                                            description = repo.description() ?: "",
                                            htmlUrl = repoUrl)
                                    // TODO repo.pushedAt

                                    repoViewModelList.add(repoVM)
                                }
                            }
                        }



                    }
                })
    }


     fun getGHRepoObservable(): GHRepoListObservableType {

         apolloClient = setupApollo(token)
         val query = ListOfReposQuery.builder().login(userName)
                .first(100)
                .build()
         val apolloWatcher = apolloClient.query(query).watcher()
         val repoListObservable = Rx2Apollo.from(apolloWatcher)
         return Pair(repoListObservable, mainStore as Store<StateType>)
    }

     fun getRepoListSubscriber(): SingleObserver<GHRepoListObservableType> {
        return object : SingleObserver<GHRepoListObservableType> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onError(exception:  Throwable) {

                Log.d(TAG,"Error is ${exception.localizedMessage}")
            }

            override fun onSuccess(value: GHRepoListObservableType) {

                val response = value.first.blockingFirst()
                val repositories: ListOfReposQuery.Repositories? = response.data()?.user()?.repositories()
                val repoViewModelList = arrayListOf<RepoViewModel>()

                repositories?.let {
                    it.nodes()?.let {
                        for(repo in it){
                            val repoUrl = repo.url()
                            val repoVM = RepoViewModel(repoName = repo.name(),
                                    watchers = repo.watchers().totalCount(),
                                    stargazersCount = repo.stargazers().totalCount(),
                                    language = repo.primaryLanguage()?.name() ?: "",
                                    forks = repo.forkCount(),
                                    description = repo.description() ?: "",
                                    htmlUrl = repoUrl)
                                // TODO repo.pushedAt
                             repoViewModelList.add(repoVM)
                            }
                        }
                    }

                repoViewModelList.let {
                    repoListTaskListener.onFinished(
                            RepoListCompletedAction(repoList = repoViewModelList as List<RepoViewModel>),
                            store = mainStore as Store<StateType>)
                }


            }

        }

    }


    private fun setupApollo(authToken: String): ApolloClient {

        val dateTimeCustomTypeAdapter = object : CustomTypeAdapter<Date> {
            val ISO8601_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            override fun decode(value: CustomTypeValue<*>): Date? {
                try {
                    return ISO8601_DATE_FORMAT.parse(value.value.toString())
                } catch (ex: ParseException) {
                    throw RuntimeException(ex)
                }
            }

            override fun encode(value: Date): CustomTypeValue<*> {
                return CustomTypeValue.GraphQLString(ISO8601_DATE_FORMAT.format(value))
            }
        }


        val okHttp = OkHttpClient.Builder().addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder().method(original.method(),
                            original.body())
                    builder.addHeader("Authorization"
                            , "Bearer $authToken")
                    chain.proceed(builder.build())
                }
                .build()

        return ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttp)
                .addCustomTypeAdapter(CustomType.DATETIME, dateTimeCustomTypeAdapter)
                .build()
    }
}