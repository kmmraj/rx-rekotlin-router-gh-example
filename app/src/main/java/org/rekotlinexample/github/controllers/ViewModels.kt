package org.rekotlinexample.github.controllers

import android.os.Parcel
import android.os.Parcelable

/**
* Created by Mohanraj Karatadipalayam on 12/10/17.
*/


// Inline function to create Parcel Creator
inline fun <reified T : Parcelable> createParcel(crossinline createFromParcel: (Parcel) -> T?): Parcelable.Creator<T> =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel): T? = createFromParcel(source)
            override fun newArray(size: Int): Array<out T?> = arrayOfNulls(size)
        }

// custom readParcelable to avoid reflection
fun <T : Parcelable> Parcel.readParcelable(creator: Parcelable.Creator<T>): T? {
    if (readString() != null) return creator.createFromParcel(this) else return null
}

data class RepoViewModel ( var repoName: String? = null,
                      var watchers: Int = 0,
                      var stargazersCount: Int = 0,
                      var language: String = "",
                     // var pushedAt: Date,
                      var forks: Int = 0,
                      var description: String = "",
                      var htmlUrl: String = "") : Parcelable {


    constructor(parcelIn: Parcel) : this(repoName = parcelIn.readString(),
            watchers = parcelIn.readInt(),
            stargazersCount = parcelIn.readInt(),
            language = parcelIn.readString(),
            forks = parcelIn.readInt(),
            description = parcelIn.readString(),
            htmlUrl =  parcelIn.readString()
            )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(repoName)
        dest.writeInt(watchers)
        dest.writeInt(stargazersCount)
        dest.writeString(language)
        dest.writeInt(forks)
        dest.writeString(description)
        dest.writeString(htmlUrl)
    }

    companion object {
        @JvmField @Suppress("unused")
        val CREATOR = createParcel { RepoViewModel(it) }
    }
}

