package org.wit.inventorymanager.helpers

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.wit.inventorymanager.R


abstract class StockSwipeToDeleteCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_swipe_delete)
    private val intrinsicWidth = deleteIcon?.intrinsicWidth
    private val intrinsicHeight = deleteIcon?.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#FFAB00")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    /**
     * > This function is called when the user drags an item from one position to another
     *
     * @param recyclerView The RecyclerView to which the ViewHolder belongs.
     * @param viewHolder The view holder that is being dragged.
     * @param target The target view holder you are switching the original one with.
     * @return Boolean
     */
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }


    /**
     * If the user is not swiping, clear the canvas. If the user is swiping, draw the orange background
     * and the delete icon
     *
     * @param c Canvas - The canvas which RecyclerView is drawing its children
     * @param recyclerView The RecyclerView to which the ItemTouchHelper is attached to.
     * @param viewHolder The ViewHolder that is being swiped.
     * @param dX The amount of horizontal displacement caused by user's action
     * @param dY The vertical distance the user has moved the item.
     * @param actionState The current state of the item. Is it swiped? Is it dragged? Is it idle?
     * @param isCurrentlyActive This is a boolean value that tells us whether the user is currently
     * dragging the item.
     * @return The return type is a Boolean.
     */
    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the orange delete background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth!! * 2
        val deleteIconRight = itemView.right - deleteIconMargin * 2.75
        val deleteIconBottom = deleteIconTop + intrinsicHeight


        // Draw the delete icon
        deleteIcon?.setBounds(deleteIconLeft, deleteIconTop,
            deleteIconRight.toInt(), deleteIconBottom)
        deleteIcon?.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    /**
     * Clear the canvas by drawing a rectangle with the clearPaint.
     *
     * @param c Canvas? - The canvas to draw on.
     * @param left The left coordinate of the rectangle to clear.
     * @param top The top of the rectangle to be cleared.
     * @param right The right side of the rectangle to clear.
     * @param bottom The bottom position of the rectangle to be cleared.
     */
    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}