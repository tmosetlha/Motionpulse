package com.the5watermelons.motionpulse.util

import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
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
 * Shows a Snackbar styled with the app's brand gradient (success) or error color,
 * instead of the default plain grey Material Snackbar.
 */
object MessageUtils {

    fun showSuccess(anchor: View, message: String) {
        showBranded(anchor, message, R.drawable.snackbar_background_success)
    }

    fun showError(anchor: View, message: String) {
        showBranded(anchor, message, R.drawable.snackbar_background_error)
    }

    private fun showBranded(anchor: View, message: String, backgroundRes: Int) {
        val snackbar = Snackbar.make(anchor, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        snackbarView.background = ContextCompat.getDrawable(anchor.context, backgroundRes)

        val textView = snackbarView.findViewById<android.widget.TextView>(
            com.google.android.material.R.id.snackbar_text
        )
        textView.setTextColor(ContextCompat.getColor(anchor.context, R.color.mp_text_primary))
        textView.textSize = 15f

        snackbar.show()
    }
}