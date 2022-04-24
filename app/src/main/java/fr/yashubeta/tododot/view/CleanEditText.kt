package fr.yashubeta.tododot.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText

class CleanEditText(
    context: Context,
    attrs: AttributeSet?
) : AppCompatEditText(context, attrs) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        return super.onCreateInputConnection(outAttrs)
    }
}