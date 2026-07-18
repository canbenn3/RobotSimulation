package ui

import observer.Observer

/**
 * A [StyledLabel] that also implements [Observer] — usable anywhere a [Label] is needed, and
 * updates its own text whenever [onUpdate] is called.
 */
class LabelObserver<T>(
    text: String = "—",
    size: Double = 18.0,
    bold: Boolean = true,
    color: String = "#e6edf3",
) : StyledLabel(text, size, bold, color), Observer<T> {

    override fun onUpdate(value: T) {
        this.text = value.toString()
    }
}
