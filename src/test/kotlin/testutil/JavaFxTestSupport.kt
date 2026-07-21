package testutil

import javafx.application.Platform
import javafx.scene.control.ListCell
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Starts the JavaFX toolkit once so tests can construct Labels / panels headlessly. */
object JavaFxTestSupport {
    private val lock = Any()
    @Volatile private var started = false

    fun ensureStarted() {
        if (started) return
        synchronized(lock) {
            if (started) return
            try {
                val latch = CountDownLatch(1)
                Platform.startup { latch.countDown() }
                check(latch.await(5, TimeUnit.SECONDS)) { "JavaFX toolkit failed to start" }
            } catch (_: IllegalStateException) {
                // Toolkit already running (e.g. another test class started it).
            }
            started = true
        }
    }

    /** Invokes protected [ListCell.updateItem] so cell rendering branches can be covered. */
    fun <T> updateListCell(cell: ListCell<T>, item: T?, empty: Boolean) {
        var type: Class<*>? = cell.javaClass
        var method: java.lang.reflect.Method? = null
        while (type != null && method == null) {
            method =
                    type.declaredMethods.firstOrNull {
                        it.name == "updateItem" && it.parameterCount == 2
                    }
            type = type.superclass
        }
        checkNotNull(method) { "updateItem not found on ${cell.javaClass}" }
        method.isAccessible = true
        method.invoke(cell, item, empty)
    }
}
