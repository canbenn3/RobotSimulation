import api.DefaultRobotApi
import command.CommandInvoker
import environment.AbstractEnvironment
import environment.Obstacle
import geometry.Pose
import geometry.Rectangle
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.layout.HBox
import model.Robot
import testutil.JavaFxTestSupport
import ui.ControlPanel
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ControlPanelTest {

    private class TestEnv(override val name: String = "Alpha") : AbstractEnvironment() {
        override val bounds = Rectangle(0.0, 0.0, 400.0, 400.0)
        override val obstacles = emptyList<Obstacle>()
        override fun startPose() = Pose(100.0, 100.0, 0.0)
    }

    private class TestEnvBeta : AbstractEnvironment() {
        override val name = "Beta"
        override val bounds = Rectangle(0.0, 0.0, 400.0, 400.0)
        override val obstacles = emptyList<Obstacle>()
        override fun startPose() = Pose(50.0, 50.0, 0.0)
    }

    @BeforeTest
    fun startJavaFx() {
        JavaFxTestSupport.ensureStarted()
    }

    private fun fixture(
            onSelect: (environment.Environment) -> Unit = {},
            onReset: () -> Unit = {},
    ): Triple<Robot, DefaultRobotApi, ControlPanel> {
        val robot = Robot(Pose(100.0, 100.0, 0.0))
        val api =
                DefaultRobotApi(
                        invoker = CommandInvoker(),
                        actuatorProvider = { robot },
                        sensorsProvider = { robot },
                )
        val panel = ControlPanel(api, listOf(TestEnv(), TestEnvBeta()), onSelect, onReset)
        return Triple(robot, api, panel)
    }

    private fun driveButtons(panel: ControlPanel): List<Button> {
        val driveBox = panel.children[1] as HBox
        return driveBox.children.filterIsInstance<Button>()
    }

    private fun button(panel: ControlPanel, labelPart: String): Button =
            driveButtons(panel).single { it.text.contains(labelPart) }

    @Test
    fun `forward button sets equal positive track velocities`() {
        val (robot, _, panel) = fixture()
        button(panel, "Forward").fire()
        assertEquals(120.0, robot.leftTrackVelocity)
        assertEquals(120.0, robot.rightTrackVelocity)
    }

    @Test
    fun `back button sets equal negative track velocities`() {
        val (robot, _, panel) = fixture()
        button(panel, "Back").fire()
        assertEquals(-120.0, robot.leftTrackVelocity)
        assertEquals(-120.0, robot.rightTrackVelocity)
    }

    @Test
    fun `left and right buttons spin with opposite track signs`() {
        val (robot, _, panel) = fixture()

        button(panel, "Left").fire()
        assertEquals(90.0, robot.leftTrackVelocity)
        assertEquals(-90.0, robot.rightTrackVelocity)

        button(panel, "Right").fire()
        assertEquals(-90.0, robot.leftTrackVelocity)
        assertEquals(90.0, robot.rightTrackVelocity)
    }

    @Test
    fun `stop button zeroes track velocities`() {
        val (robot, _, panel) = fixture()
        button(panel, "Forward").fire()
        button(panel, "Stop").fire()
        assertEquals(0.0, robot.leftTrackVelocity)
        assertEquals(0.0, robot.rightTrackVelocity)
    }

    @Test
    fun `undo and redo restore previous drive command`() {
        val (robot, _, panel) = fixture()
        button(panel, "Forward").fire()
        button(panel, "Back").fire()
        assertEquals(-120.0, robot.leftTrackVelocity)

        button(panel, "Undo").fire()
        assertEquals(120.0, robot.leftTrackVelocity)

        button(panel, "Redo").fire()
        assertEquals(-120.0, robot.leftTrackVelocity)
    }

    @Test
    fun `reset button invokes the reset callback`() {
        var resets = 0
        val (_, _, panel) = fixture(onReset = { resets++ })
        button(panel, "Reset").fire()
        assertEquals(1, resets)
    }

    @Test
    fun `environment combo notifies on selection change`() {
        val selected = mutableListOf<String>()
        val (_, _, panel) = fixture(onSelect = { selected += it.name })
        val combo = environmentCombo(panel)

        combo.value = combo.items[1]

        assertTrue(selected.contains("Beta"), "selected=$selected")
    }

    @Test
    fun `environment combo ignores null selections`() {
        var calls = 0
        val (_, _, panel) = fixture(onSelect = { calls++ })
        val combo = environmentCombo(panel)

        // selectFirst already chose Alpha; clearing must not call onSelect
        val before = calls
        combo.value = null
        assertEquals(before, calls)
    }

    @Test
    fun `environment list cells render empty null and named items`() {
        val (_, _, panel) = fixture()
        val combo = environmentCombo(panel)
        val cell = combo.buttonCell
        val env = combo.items.first()

        JavaFxTestSupport.updateListCell(cell, null, empty = true)
        assertEquals(null, cell.text)

        JavaFxTestSupport.updateListCell(cell, null, empty = false)
        assertEquals(null, cell.text)

        JavaFxTestSupport.updateListCell(cell, env, empty = false)
        assertEquals(env.name, cell.text)

        // cellFactory path creates a fresh cell with the same branches
        val factoryCell = combo.cellFactory.call(null)
        JavaFxTestSupport.updateListCell(factoryCell, env, empty = false)
        assertEquals(env.name, factoryCell.text)
        JavaFxTestSupport.updateListCell(factoryCell, null, empty = true)
        assertEquals(null, factoryCell.text)
    }

    @Suppress("UNCHECKED_CAST")
    private fun environmentCombo(panel: ControlPanel): ComboBox<environment.Environment> {
        val envBox = panel.children[0] as HBox
        return envBox.children.filterIsInstance<ComboBox<*>>().single() as ComboBox<environment.Environment>
    }
}
