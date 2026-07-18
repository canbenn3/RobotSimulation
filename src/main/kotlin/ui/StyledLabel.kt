package ui

import javafx.scene.control.Label

/**
 * A [Label] with consistent font size, weight, and color styling applied at construction.
 */
open class StyledLabel(
    text: String = "—",
    size: Double = 18.0,
    bold: Boolean = false,
    color: String = "#e6edf3",
) : Label(text) {
    init {
        style = "-fx-font-size: ${size}px; -fx-text-fill: $color;" +
            if (bold) " -fx-font-weight: bold;" else ""
    }
}
