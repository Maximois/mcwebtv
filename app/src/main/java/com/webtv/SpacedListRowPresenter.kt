package com.webtv

import android.content.res.Resources
import android.view.ViewGroup
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter

class SpacedListRowPresenter(private val spacingDp: Int = 28) : ListRowPresenter() {

    override fun createRowViewHolder(parent: ViewGroup): RowPresenter.ViewHolder {
        val vh = super.createRowViewHolder(parent)
        if (vh is ListRowPresenter.ViewHolder) {
            val spacingPx = (spacingDp * Resources.getSystem().displayMetrics.density).toInt()
            vh.gridView.setItemSpacing(spacingPx)
        }
        return vh
    }
}
