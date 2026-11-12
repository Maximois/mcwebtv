package com.webtv

import android.app.Activity

object DesdepylabsModule {
    const val CATEGORY = "DESDEPYLABS"

    fun getChannels(): List<Movie> {
        return listOf(
            Movie().apply {
                title = "Telefuturo"
                videoUrl = "https://www.desdepylabs.com/External/tvaccion/telefuturo"
                cardImageUrl = "res://drawable/ic_logo"
                category = CATEGORY
            },
            Movie().apply {
                title = "La Tele"
                videoUrl = "https://www.desdepylabs.com/External/tvaccion/latele"
                cardImageUrl = "res://drawable/ic_logo"
                category = CATEGORY
            },
            Movie().apply {
                title = "Npy"
                videoUrl = "https://www.desdepylabs.com/External/tvaccion/npy"
                cardImageUrl = "res://drawable/ic_logo"
                category = CATEGORY
            }
        )
    }

    fun navigate(activity: Activity, movie: Movie) {
        BrowserActivity.launch(activity, movie.videoUrl ?: return)
    }
}
