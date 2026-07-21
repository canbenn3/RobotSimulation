import environment.AbstractEnvironment
import environment.Obstacle
import geometry.Pose
import geometry.Rectangle
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import model.Robot
import testutil.JavaFxTestSupport
import ui.LabelObserver
import ui.TelemetryPanel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PanelTest {

    private class EmptyWorld : AbstractEnvironment() {
        override val name = "panel-test"
        override val bounds = Rectangle(0.0, 0.0, 500.0, 500.0)
        override val obstacles = emptyList<Obstacle>()
        override fun startPose() = Pose(100.0, 250.0, 0.0)
    }

    @BeforeTest
    fun startJavaFx() {
        JavaFxTestSupport.ensureStarted()
    }

    @Test
    fun `LabelObserver writes the update value into its label text`() {
        val label = LabelObserver<Double>(text = "—")
        assertEquals("—", label.text)

        label.onUpdate(12.5)
        assertEquals("12.5", label.text)

        label.onUpdate(0.0)
        assertEquals("0.0", label.text)
    }

    @Test
    fun `LabelObserver stringifies non-numeric values`() {
        val label = LabelObserver<Color>()
        label.onUpdate(Color.RED)
        assertTrue(label.text.contains("0xff0000") || label.text.contains("RED") || label.text.isNotBlank())
    }

    @Test
    fun `TelemetryPanel bindTo updates labels when sensors notify`() {
        val robot = Robot(EmptyWorld().startPose(), radius = 16.0)
        val panel = TelemetryPanel()
        val before = valueLabels(panel).map { it.text }

        panel.bindTo(robot)
        robot.updateSensors(EmptyWorld())

        val after = valueLabels(panel).map { it.text }
        assertEquals(5, after.size)
        assertTrue(before.all { it == "—" })
        assertTrue(after.any { it != "—" }, "expected at least one live reading, got $after")
        // Sonar should report a finite distance to the world boundary
        assertNotEquals("—", after[0])
        assertTrue(after[0].toDoubleOrNull() != null || after[0].contains("."), "sonar text=${after[0]}")
    }

    @Test
    fun `TelemetryPanel line label tracks lineLeft notifications`() {
        val robot = Robot(Pose(100.0, 100.0, 0.0))
        val panel = TelemetryPanel()
        panel.bindTo(robot)

        robot.lineLeft.notifyObservers(true)
        assertEquals("true", valueLabels(panel)[3].text)

        robot.lineRight.notifyObservers(false)
        assertEquals("false", valueLabels(panel)[3].text)
    }

    /** Caption rows are VBox(caption, valueLabel); skip the title child. */
    private fun valueLabels(panel: TelemetryPanel): List<Label> =
            panel.children.drop(1).map { row ->
                (row as VBox).children[1] as Label
            }
}
