package com.kdapps.offstore.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class OStTextView (context: Context, attrs: AttributeSet) :AppCompatTextView(context, attrs){
    init {
        applyFont()
    }

    private fun applyFont() {
        val typeface: Typeface = Typeface.createFromAsset(context.assets, "raleway.regular.ttf")
        setTypeface(typeface)
    }
}