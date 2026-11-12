package com.webtv

import android.app.Activity

object SntHlsModule {
    const val CATEGORY = "SNT HLS"

    fun getChannels(): List<Movie> {
        return listOf(
            Movie().apply {
                title = "SNT HLS"
                videoUrl = "https://d3epa8orr2qy3m.cloudfront.net/ts:abr.m3u8"
                cardImageUrl = "res://drawable/ic_logo"
                category = CATEGORY
            }
        )
    }

    fun navigate(activity: Activity, movie: Movie) {
        BrowserActivity.launch(activity, movie.videoUrl ?: return)
    }
}
