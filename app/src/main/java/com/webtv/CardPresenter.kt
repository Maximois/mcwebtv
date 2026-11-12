package com.webtv

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide

class CardPresenter : Presenter() {
    private var defaultImage: Drawable? = null

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        defaultImage = ContextCompat.getDrawable(parent.context, R.drawable.ic_logo)

        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                val v = this
                if (selected) {
                    animate().cancel()
                    ObjectAnimator.ofPropertyValuesHolder(
                        v,
                        PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.15f),
                        PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.15f),
                        PropertyValuesHolder.ofFloat("elevation", 0f, 18f),
                        PropertyValuesHolder.ofFloat("translationZ", 0f, 18f)
                    ).setDuration(250).start()
                } else {
                    animate().cancel()
                    ObjectAnimator.ofPropertyValuesHolder(
                        v,
                        PropertyValuesHolder.ofFloat("scaleX", 1.15f, 1.0f),
                        PropertyValuesHolder.ofFloat("scaleY", 1.15f, 1.0f),
                        PropertyValuesHolder.ofFloat("elevation", 18f, 0f),
                        PropertyValuesHolder.ofFloat("translationZ", 18f, 0f)
                    ).setDuration(250).start()
                }
                super.setSelected(selected)
            }
        }

        cardView.cardType = ImageCardView.CARD_TYPE_INFO_UNDER
        cardView.infoAreaBackground = ContextCompat.getDrawable(parent.context, R.drawable.card_info_bg)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        cardView.setBackgroundColor(Color.TRANSPARENT)

        val width = parent.context.resources.getDimensionPixelSize(R.dimen.card_main_image_width)
        val height = parent.context.resources.getDimensionPixelSize(R.dimen.card_main_image_height)
        cardView.setMainImageDimensions(width, height)

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val movie = item as Movie
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = movie.title
        // Buscar recursivamente el TextView del título y aplicar estilo y centrado
        fun findTitleText(v: View): TextView? {
            if (v is TextView) {
                val id = v.id
                if (id == androidx.leanback.R.id.title || id == androidx.leanback.R.id.title_text) return v
            }
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) {
                    val found = findTitleText(v.getChildAt(i))
                    if (found != null) return found
                }
            }
            return null
        }

        val titleTv = findTitleText(cardView) ?: cardView.findViewById(androidx.leanback.R.id.title) ?: cardView.findViewById(androidx.leanback.R.id.title_text)
        titleTv?.let { t ->
            t.layoutParams = t.layoutParams?.apply { width = ViewGroup.LayoutParams.MATCH_PARENT }
            t.setPadding(2, 0, 2, 0)
            t.gravity = Gravity.CENTER
            t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)
            t.maxLines = 1
            t.ellipsize = TextUtils.TruncateAt.END
            t.isSingleLine = true
            t.setIncludeFontPadding(false)
            t.setShadowLayer(3f, 0f, 0f, Color.parseColor("#00D4FF"))
            t.setTextColor(Color.parseColor("#FFFFFF"))
            t.typeface = Typeface.DEFAULT_BOLD
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            t.letterSpacing = 0.03f
        }

        // Ocultar el texto de categoría (sobra para el usuario final)
        cardView.findViewById<TextView>(androidx.leanback.R.id.content_text)?.visibility = android.view.View.GONE

        val imageSource: Any? = when {
            movie.cardImageUrl?.startsWith("res://") == true -> logoUrlForTitle(movie.title) ?: defaultImage
            movie.cardImageUrl.isNullOrBlank() -> defaultImage
            else -> movie.cardImageUrl
        }

        Glide.with(viewHolder.view.context)
            .load(imageSource)
            .centerInside()
            .placeholder(defaultImage)
            .error(defaultImage)
            .into(cardView.mainImageView!!)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
    }

    companion object {
        private val channelDomains = mapOf(
            "Venusmedia"  to "desdeparaguay.com",
            "Telefuturo"  to "telefuturo.com.py",
            "La Tele"     to "latele.com.py",
            "Npy"         to "npy.com.py",
            "SNT HLS"     to "snt.com.py",
            "Gen TV"      to "gen.com.py",
            "Paraguay TV" to "paraguaytv.gov.py",
            "C9N"         to "c9n.com.py",
            "Ñandejara Ñee Tv" to "nandejaraneetv.com"
        )

        private fun logoUrlForTitle(title: String?): String? {
            val domain = channelDomains[title] ?: return null
            return "https://t3.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://$domain&size=128"
        }
    }
}
