package com.kdapps.offstore.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton

class OSRadioButton(context: Context, attrs : AttributeSet): AppCompatRadioButton(context, attrs) {

    init {
        applyFont()
    }

    private fun applyFont() {
        val typeface: Typeface = Typeface.createFromAsset(context.assets, "raleway.bold.ttf")
        setTypeface(typeface)
    }
}