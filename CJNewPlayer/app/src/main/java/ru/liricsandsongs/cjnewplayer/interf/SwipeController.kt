package ru.liricsandsongs.cjnewplayer.interf

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import ru.liricsandsongs.cjnewplayer.interf.SwipeControllerActions


class SwipeController(buttonsActions: SwipeControllerActions?) :
    Callback() {
    private var swipeBack = false
    private var buttonShowedState = ButtonsState.GONE
    private var buttonInstance: RectF? = null
    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private var buttonsActions: SwipeControllerActions? = null
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT or RIGHT)
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = buttonShowedState !== ButtonsState.GONE
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        var dX = dX
        if (actionState == ACTION_STATE_SWIPE) {
            if (buttonShowedState !== ButtonsState.GONE) {
                if (buttonShowedState === ButtonsState.LEFT_VISIBLE) dX = Math.max(dX, leftbuttonWidth)
                if (buttonShowedState === ButtonsState.RIGHT_VISIBLE) dX =
                    Math.min(dX, -buttonWidth)
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            } else {
                setTouchListener(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        if (buttonShowedState === ButtonsState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
        currentItemViewHolder = viewHolder
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { v, event ->
            swipeBack =
                event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                if (dX < -buttonWidth) buttonShowedState =
                    ButtonsState.RIGHT_VISIBLE/* else if (dX > leftbuttonWidth) buttonShowedState =
                    ButtonsState.LEFT_VISIBLE*/
                if (buttonShowedState !== ButtonsState.GONE) {
                    setTouchDownListener(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    setItemsClickable(recyclerView, false)
                }
            }
            false
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchDownListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                setTouchUpListener(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchUpListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        recyclerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                super@SwipeController.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    0f,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                recyclerView.setOnTouchListener { v, event -> false }
                setItemsClickable(recyclerView, true)
                swipeBack = false
                if (buttonsActions != null && buttonInstance != null && buttonInstance!!.contains(
                        event.x,
                        event.y
                    )
                ) {
                    if (buttonShowedState === ButtonsState.LEFT_VISIBLE) {
                      //  buttonsActions!!.onLeftClicked(viewHolder.adapterPosition)
                    } else if (buttonShowedState === ButtonsState.RIGHT_VISIBLE) {
                        buttonsActions!!.onRightClicked(viewHolder.adapterPosition)
                    }
                }
                buttonShowedState = ButtonsState.GONE
                currentItemViewHolder = null
            }
            false
        }
    }

    private fun setItemsClickable(recyclerView: RecyclerView, isClickable: Boolean) {
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i).isClickable = isClickable
        }
    }
  fun drawNotCome(c:Canvas, viewHolder: RecyclerView.ViewHolder){
        val buttonWidthWithoutPadding = buttonWidth - 20
        val buttonWidthWithoutPaddingLeft = leftbuttonWidth - 20
        val corners = 24f
        val itemView: View = viewHolder.itemView
        val p = Paint()

      /*     val leftButton = RectF(
              itemView.getLeft().toFloat(),
              itemView.getTop().toFloat()-80f,
              itemView.getLeft() + buttonWidthWithoutPaddingLeft,
              itemView.getBottom().toFloat()
          )
       val leftButtons = RectF(
            itemView.getLeft().toFloat(),
            itemView.getTop().toFloat()+80f,
            itemView.getLeft() + buttonWidthWithoutPaddingLeft,
            itemView.getBottom().toFloat()
        )
          p.color = Color.parseColor("#BDBDBD")
        c.drawRoundRect(leftButton, corners, corners, p)
          drawText("Пришел / ", c, leftButton, p)
        drawText("Не пришел", c, leftButtons, p)
  */        val rightButton = RectF(
            itemView.getRight() - buttonWidthWithoutPadding,
            itemView.getTop().toFloat(),
            itemView.getRight().toFloat(),
            itemView.getBottom().toFloat()
        )
        p.color = Color.parseColor("#FF6600")
        c.drawRoundRect(rightButton, corners, corners, p)
        drawText("Текст", c, rightButton, p)
        buttonInstance = null
        if (buttonShowedState === ButtonsState.LEFT_VISIBLE) {
         //   buttonInstance = leftButton
        } else if (buttonShowedState === ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton
        }
    }
   fun drawButtons(c: Canvas, viewHolder: RecyclerView.ViewHolder) {
        val buttonWidthWithoutPadding = buttonWidth - 20
        val buttonWidthWithoutPaddingLeft = leftbuttonWidth - 20
        val corners = 16f
        val itemView: View = viewHolder.itemView
        val p = Paint()
        val leftButton = RectF(
            itemView.getLeft().toFloat(),
            itemView.getTop().toFloat(),
            itemView.getLeft() + buttonWidthWithoutPaddingLeft,
            itemView.getBottom().toFloat()
        )
        p.color = Color.parseColor("#388E3C")
        c.drawRoundRect(leftButton, corners, corners, p)
        drawText("Пришел", c, leftButton, p)
        val rightButton = RectF(
            itemView.getRight() - buttonWidthWithoutPadding,
            itemView.getTop().toFloat(),
            itemView.getRight().toFloat(),
            itemView.getBottom().toFloat()
        )
        p.color = Color.parseColor("#FF6600")
        c.drawRoundRect(rightButton, corners, corners, p)
        drawText("Анкета", c, rightButton, p)
        buttonInstance = null
        if (buttonShowedState === ButtonsState.LEFT_VISIBLE) {
            buttonInstance = leftButton
        } else if (buttonShowedState === ButtonsState.RIGHT_VISIBLE) {
            buttonInstance = rightButton
        }
    }
    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = 60f
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = textSize
        val textWidth = p.measureText(text)
        c.drawText(text, button.centerX() - textWidth / 2, button.centerY() + textSize / 2, p)
    }

    fun onDraw(c: Canvas) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder!!)
        }
    }
    fun onDrawNotCome(c:Canvas){
        if(currentItemViewHolder!=null){
            drawNotCome(c, currentItemViewHolder!!)
        }
    }

    companion object {
        private const val buttonWidth = 300f
        private const val leftbuttonWidth = 500f
    }

    init {
        this.buttonsActions = buttonsActions
    }

}
internal enum class ButtonsState {
    GONE, LEFT_VISIBLE, RIGHT_VISIBLE
}

