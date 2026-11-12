package com.webtv

import android.app.Activity

object DeparaguayModule {
    const val CATEGORY = "DESDEPARAGUAY"

    fun getChannels(): List<Movie> {
        return listOf(
            Movie().apply {
                title = "Venusmedia"
                videoUrl = "https://www.desdeparaguay.com/external/?venusmedia"
                cardImageUrl = "res://drawable/ic_logo"
                category = CATEGORY
            }
        )
    }

    fun navigate(activity: Activity, movie: Movie) {
        val url = movie.videoUrl ?: return
        BrowserActivity.launch(activity, url)
    }
}
