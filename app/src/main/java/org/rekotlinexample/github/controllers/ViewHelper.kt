package org.rekotlinexample.github.controllers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.res.Resources
import android.os.Build
import android.view.View

/**
* Created by Mohanraj Karatadipalayam on 28/10/17.
*/

// TODO : fix the background edit - https://stackoverflow.com/questions/24541411/disabling-all-touches-when-loading-indicator-is-shown
object ViewHelper {
    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     fun showProgress(show: Boolean, view: View, progressView: View,
                     // progressViewContainer: View,
                      resources: Resources) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progressView spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

            view.visibility = if (show) View.GONE else View.VISIBLE
            view.isClickable = show
            view.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = if (show) View.GONE else View.VISIBLE
                }
            })


            progressView.visibility = if (show) View.VISIBLE else View.GONE
            progressView.animate().setDuration(shortAnimTime.toLong()).alpha(
                    (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progressView.visibility = if (show) View.VISIBLE else View.GONE
                }
            })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.visibility = if (show) View.VISIBLE else View.GONE
            view.visibility = if (show) View.GONE else View.VISIBLE
            view.isClickable = show
        }
    }
}