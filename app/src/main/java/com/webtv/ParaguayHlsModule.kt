package com.webtv

import android.app.Activity

object ParaguayHlsModule {
    const val CATEGORY = "Paraguay TV"

    fun getChannels(): List<Movie> {
        return listOf(
            Movie().apply {
                title = "Paraguay TV"
                videoUrl = "https://stream.paraguaytv.gov.py/memfs/bbe36fed-9b49-4d1d-adaa-4bd6d1b2e386.m3u8"
                cardImageUrl = "res://drawable/ic_logo"
                category = CATEGORY
            }
        )
    }

    fun navigate(activity: Activity, movie: Movie) {
        BrowserActivity.launch(activity, movie.videoUrl ?: return)
    }
}
