package com.example

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

// Class to make circled Backgrounds symmetric
class SquareFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var isHalfSize: Boolean = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newSpec = if (isHalfSize) MeasureSpec.makeMeasureSpec(
            MeasureSpec.getSize(widthMeasureSpec) / 2,
            MeasureSpec.EXACTLY
        ) else widthMeasureSpec
        super.onMeasure(newSpec, newSpec) // Use the newSpec measurement for both dimensions
    }
}
