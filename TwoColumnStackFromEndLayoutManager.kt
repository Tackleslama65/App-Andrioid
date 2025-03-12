package com.example.myapplication


import android.R.attr.height
import androidx.recyclerview.widget.RecyclerView

class TwoColumnStackFromEndLayoutManager : RecyclerView.LayoutManager() {

    private var verticalOffset = 0 // Текущее смещение по вертикали
    private var totalHeight = 0 // Общая высота всех элементов

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }


    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        detachAndScrapAttachedViews(recycler)

        val parentWidth = width
        val columnWidth = parentWidth / 2

        val positions = IntArray(itemCount) { it } // Исходный порядок индексов

        if (itemCount > 8) {
            val reversed = positions.copyOf()
            for (i in positions.indices) {
                positions[i] = reversed[reversed.size - 1 - i]
            }
        }

        var top = 0
        if (itemCount <= 8) {
            //val totalHeightEstimate = (height / 4) * ((itemCount + 1) / 2) // Примерное расположение
            //top = height / 2 - totalHeightEstimate / 2
           // if (top < 0) top = 0
        }
        val rowHeights = mutableListOf<Int>()
        var lastLeftHeight = 0

        for (i in positions.indices) {
            val position = positions[i]
            val view = recycler.getViewForPosition(position)
            addView(view)

            val column = if (i % 2 == 0) 0 else 1
            val left = column * columnWidth
            val right = left + columnWidth

            measureChildWithMargins(view, 0, 0)
            val viewHeight = getDecoratedMeasuredHeight(view)

            if (column == 0) {
                rowHeights.add(viewHeight)
                lastLeftHeight = viewHeight
            } else {
                val rowIndex = rowHeights.size - 1
                rowHeights[rowIndex] = maxOf(rowHeights[rowIndex], viewHeight)
            }

            val bottom = top + rowHeights.last()
            layoutDecorated(view, left, top, right, bottom)

            if (column == 1 || i == positions.size - 1) {
                top += maxOf(rowHeights.last(), lastLeftHeight)
            }
        }

        totalHeight = top
        verticalOffset = 0 // Всегда размещаем элементы сверху
       // if (totalHeight < height) {
           // offsetChildrenVertical(verticalOffset)
        //}
    }


    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        val parentHeight = height
        val totalScrollRange = totalHeight - parentHeight

        if (totalScrollRange <= 0) {
            return 0
        }

        var delta = dy
        if (verticalOffset + dy < 0) {
            delta = -verticalOffset
        } else if (verticalOffset + dy > totalScrollRange) {
            delta = totalScrollRange - verticalOffset
        }

        offsetChildrenVertical(-delta)
        verticalOffset += delta

        return delta
    }

    override fun getPaddingTop(): Int {
        return verticalOffset
    }
}
