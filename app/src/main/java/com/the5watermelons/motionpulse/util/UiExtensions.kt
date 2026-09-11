package com.the5watermelons.motionpulse.util

import android.app.Activity
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.the5watermelons.motionpulse.R

/**
 * Adds a tappable "show/hide password" eye icon to the end of a password EditText.
 * Preserves cursor position when toggling. Safe to call once per EditText in onViewCreated.
 */
fun EditText.enablePasswordToggle() {
    var isVisible = false
    updatePasswordToggleIcon(isVisible)

    setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val drawableEnd = compoundDrawables[2]
            if (drawableEnd != null &&
                event.rawX >= (right - drawableEnd.bounds.width() - paddingEnd)
            ) {
                isVisible = !isVisible
                val cursorPosition = selectionEnd
                inputType = if (isVisible) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                updatePasswordToggleIcon(isVisible)
                if (cursorPosition in 0..text.length) setSelection(cursorPosition)
                return@setOnTouchListener true
            }
        }
        false
    }
}

private fun EditText.updatePasswordToggleIcon(visible: Boolean) {
    val icon = if (visible) R.drawable.ic_eye_off else R.drawable.ic_eye
    setCompoundDrawablesWithIntrinsicBounds(0, 0, icon, 0)
}

/**
 * Shows a small branded message pinned to the bottom of the screen, built entirely
 * by hand instead of using Material's Snackbar -- Snackbar re-applies its own
 * elevation-tinted background during its show animation no matter what we set
 * beforehand, which is why it kept rendering as a washed cream color instead of
 * our exact brand colors. This bypasses that system completely.
 */
object MessageUtils {

    fun showSuccess(activity: Activity, message: String) {
        showBranded(activity, message, R.drawable.snackbar_background_success)
    }

    fun showError(activity: Activity, message: String) {
        showBranded(activity, message, R.drawable.snackbar_background_error)
    }

    private fun showBranded(activity: Activity, message: String, backgroundRes: Int) {
        val root = activity.findViewById<ViewGroup>(android.R.id.content)
        val density = activity.resources.displayMetrics.density

        val messageView = TextView(activity).apply {
            text = message
            setTextColor(ContextCompat.getColor(activity, R.color.mp_text_primary))
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(activity, backgroundRes)
            elevation = 12f * density
            val paddingH = (18 * density).toInt()
            val paddingV = (14 * density).toInt()
            setPadding(paddingH, paddingV, paddingH, paddingV)
            alpha = 0f
        }

        val sideMarginPx = (24 * density).toInt()
        val bottomMarginPx = (90 * density).toInt()
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            leftMargin = sideMarginPx
            rightMargin = sideMarginPx
            bottomMargin = bottomMarginPx
        }

        root.addView(messageView, params)

        messageView.animate().alpha(1f).setDuration(200).withEndAction {
            messageView.postDelayed({
                messageView.animate().alpha(0f).setDuration(200).withEndAction {
                    root.removeView(messageView)
                }.start()
            }, 1800L)
        }.start()
    }
}