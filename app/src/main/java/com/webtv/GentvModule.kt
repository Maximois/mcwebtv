package com.webtv

import android.app.Activity

object GentvModule {
    const val CATEGORY = "GEN TV"

    fun getChannels(): List<Movie> {
        return listOf(
            Movie().apply {
                title = "Gen TV"
                videoUrl = "https://gentv.desdepylabs.com/External/gentv"
                cardImageUrl = "res://drawable/ic_logo"
                category = CATEGORY
            }
        )
    }

    fun navigate(activity: Activity, movie: Movie) {
        BrowserActivity.launch(activity, movie.videoUrl ?: return, "https://www.gen.com.py/")
    }
}
