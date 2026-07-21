import command.Command
import command.CommandInvoker
import command.RobotActuator
import command.SetVelocityCommand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommandInvokerTest {

    private class FakeActuator : RobotActuator {
        override var leftTrackVelocity: Double = 0.0
            private set
        override var rightTrackVelocity: Double = 0.0
            private set

        override fun setTrackVelocities(left: Double, right: Double) {
            leftTrackVelocity = left
            rightTrackVelocity = right
        }
    }

    private class CountingCommand(private val label: String) : Command {
        val log = mutableListOf<String>()
        override fun execute() {
            log += "execute:$label"
        }
        override fun undo() {
            log += "undo:$label"
        }
    }

    @Test
    fun `run executes and records undo history`() {
        val invoker = CommandInvoker()
        val cmd = CountingCommand("a")

        assertFalse(invoker.canUndo())
        invoker.run(cmd)

        assertEquals(listOf("execute:a"), cmd.log)
        assertTrue(invoker.canUndo())
        assertFalse(invoker.canRedo())
    }

    @Test
    fun `undo reverses the last command and enables redo`() {
        val invoker = CommandInvoker()
        val cmd = CountingCommand("a")
        invoker.run(cmd)

        invoker.undo()

        assertEquals(listOf("execute:a", "undo:a"), cmd.log)
        assertFalse(invoker.canUndo())
        assertTrue(invoker.canRedo())
    }

    @Test
    fun `redo re-executes an undone command`() {
        val invoker = CommandInvoker()
        val cmd = CountingCommand("a")
        invoker.run(cmd)
        invoker.undo()

        invoker.redo()

        assertEquals(listOf("execute:a", "undo:a", "execute:a"), cmd.log)
        assertTrue(invoker.canUndo())
        assertFalse(invoker.canRedo())
    }

    @Test
    fun `run clears the redo stack`() {
        val invoker = CommandInvoker()
        val first = CountingCommand("a")
        val second = CountingCommand("b")
        invoker.run(first)
        invoker.undo()
        assertTrue(invoker.canRedo())

        invoker.run(second)

        assertFalse(invoker.canRedo())
        invoker.undo()
        assertEquals(listOf("execute:b", "undo:b"), second.log)
        // first is no longer redoable after a new run
        invoker.redo()
        assertEquals(listOf("execute:b", "undo:b", "execute:b"), second.log)
    }

    @Test
    fun `undo and redo are no-ops on empty stacks`() {
        val invoker = CommandInvoker()
        invoker.undo()
        invoker.redo()
        assertFalse(invoker.canUndo())
        assertFalse(invoker.canRedo())
    }

    @Test
    fun `SetVelocityCommand execute and undo restore track speeds`() {
        val actuator = FakeActuator()
        actuator.setTrackVelocities(10.0, 20.0)
        val invoker = CommandInvoker()

        invoker.run(SetVelocityCommand(actuator, 100.0, -50.0))
        assertEquals(100.0, actuator.leftTrackVelocity)
        assertEquals(-50.0, actuator.rightTrackVelocity)

        invoker.undo()
        assertEquals(10.0, actuator.leftTrackVelocity)
        assertEquals(20.0, actuator.rightTrackVelocity)

        invoker.redo()
        assertEquals(100.0, actuator.leftTrackVelocity)
        assertEquals(-50.0, actuator.rightTrackVelocity)
    }
}
