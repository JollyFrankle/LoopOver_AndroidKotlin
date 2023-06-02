package com.example.poolover_jinston

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

class TileView: MaterialTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        gravity = android.view.Gravity.CENTER
//        setBackgroundColor(android.graphics.Color.rgb((128..255).random(), (128..255).random(), (128..255).random()))
        textSize = 24f
        setTextColor(android.graphics.Color.BLACK)
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}