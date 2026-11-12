package com.webtv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.OnItemViewClickedListener
import java.util.concurrent.Executors

class MainFragment : BrowseSupportFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        title = "MC-WTV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val config = RemoteConfigRepository.load(requireContext())
            val allChannels = mutableListOf<Movie>()
            allChannels.addAll(DeparaguayModule.getChannels())
            allChannels.addAll(DesdepylabsModule.getChannels())
            allChannels.addAll(SntHlsModule.getChannels())
            allChannels.addAll(GentvModule.getChannels())
            allChannels.addAll(ParaguayHlsModule.getChannels())
            allChannels.add(Movie().apply {
                title = "C9N"
                videoUrl = "https://d174axghh54kh0.cloudfront.net/ts:abr.m3u8"
                cardImageUrl = "res://drawable/ic_logo"
                category = "C9N"
            })
            allChannels.add(Movie().apply {
                title = "ABCTV"
                videoUrl = "https://geo.dailymotion.com/player/x1b1gw.html?video=kQRS6ZAjGuMkByE4Mtc&autoplay=1&mute=1"
                cardImageUrl = "res://drawable/ic_logo"
                category = "PARAGUAY_EXTRA"
                referer = "https://www.abc.com.py/tv/"
            })
            allChannels.add(Movie().apply {
                title = "Trece"
                videoUrl = "https://geo.dailymotion.com/player/x1at3a.html?video=k1bgQZHbBKPqXOHczd4&autoplay=1&mute=1"
                cardImageUrl = "res://drawable/ic_logo"
                category = "PARAGUAY_EXTRA"
                referer = "https://trece.com.py/"
            })
            allChannels.add(Movie().apply {
                title = "Unicanal"
                videoUrl = "https://geo.dailymotion.com/player/x1apn6.html?video=k41tDnJts45CTVHcyD4&autoplay=1&mute=1"
                cardImageUrl = "res://drawable/ic_logo"
                category = "PARAGUAY_EXTRA"
                referer = "https://unicanal.com.py/"
            })
            // Cargar canales remotos.
            val remoteChannels = config.channels
                .map {
                    Movie().apply {
                        title = it.title
                        videoUrl = it.videoUrl
                        cardImageUrl = it.cardImageUrl ?: "res://drawable/ic_logo"
                        category = it.category ?: "PARAGUAY_EXTRA"
                        referer = it.referer
                        userAgent = it.userAgent
                    }
                }
            allChannels.addAll(0, remoteChannels)

            val rowsAdapter = ArrayObjectAdapter(SpacedListRowPresenter())

            // Filas agrupadas de a 4
            allChannels.chunked(4).forEachIndexed { index, chunk ->
                val rowAdapter = ArrayObjectAdapter(CardPresenter())
                chunk.forEach { rowAdapter.add(it) }
                val headerTitle = if (index == 0) "Paraguay" else ""
                rowsAdapter.add(ListRow(HeaderItem(headerTitle), rowAdapter))
            }

            val events = config.events.map {
                Movie().apply {
                    title = it.title
                    videoUrl = it.videoUrl
                    cardImageUrl = it.cardImageUrl ?: "res://drawable/ic_logo"
                    category = "EVENTOS"
                    referer = it.referer
                    userAgent = it.userAgent
                }
            }

            if (events.isNotEmpty()) {
                val eventRowAdapter = ArrayObjectAdapter(CardPresenter())
                events.forEach { eventRowAdapter.add(it) }
                rowsAdapter.add(ListRow(HeaderItem("Eventos"), eventRowAdapter))
            } else {
                val emptyEventRowAdapter = ArrayObjectAdapter(CardPresenter())
                repeat(4) {
                    emptyEventRowAdapter.add(Movie().apply {
                        title = "Evento ${it + 1}"
                        cardImageUrl = "res://drawable/ic_logo"
                        category = "EVENTOS"
                    })
                }
                rowsAdapter.add(ListRow(HeaderItem("Eventos"), emptyEventRowAdapter))
            }

            requireActivity().runOnUiThread {
                adapter = rowsAdapter
            }
        }

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            val movie = item as? Movie ?: return@OnItemViewClickedListener
            val activity = requireActivity()
            when (movie.category) {
                DeparaguayModule.CATEGORY -> DeparaguayModule.navigate(activity, movie)
                DesdepylabsModule.CATEGORY -> DesdepylabsModule.navigate(activity, movie)
                SntHlsModule.CATEGORY -> SntHlsModule.navigate(activity, movie)
                GentvModule.CATEGORY -> GentvModule.navigate(activity, movie)
                ParaguayHlsModule.CATEGORY -> ParaguayHlsModule.navigate(activity, movie)
                "EVENTOS" -> {
                    val url = movie.videoUrl ?: return@OnItemViewClickedListener
                    BrowserActivity.launch(activity, url, movie.referer, movie.userAgent, isEvent = true)
                }
                else -> {
                    val url = movie.videoUrl ?: return@OnItemViewClickedListener
                    BrowserActivity.launch(activity, url, movie.referer, movie.userAgent)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headers = view.findViewById<View?>(androidx.leanback.R.id.browse_headers_dock)
        headers?.setBackgroundResource(R.drawable.bg_gradient)
    }
}
