import api.DefaultRobotApi
import command.Command
import command.CommandInvoker
import command.SetVelocityCommand
import geometry.Pose
import model.Robot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DefaultRobotApiTest {

    private fun fixture(): Pair<Robot, DefaultRobotApi> {
        val robot = Robot(Pose(100.0, 100.0, 0.0))
        val api =
                DefaultRobotApi(
                        invoker = CommandInvoker(),
                        actuatorProvider = { robot },
                        sensorsProvider = { robot },
                )
        return robot to api
    }

    @Test
    fun `actuator and sensors providers return the live robot`() {
        val (robot, api) = fixture()
        assertSame(robot, api.actuator)
        assertSame(robot, api.sensors)
    }

    @Test
    fun `perform runs a single command through the invoker`() {
        val (robot, api) = fixture()
        api.perform(SetVelocityCommand(api.actuator, 40.0, 60.0))
        assertEquals(40.0, robot.leftTrackVelocity)
        assertEquals(60.0, robot.rightTrackVelocity)
    }

    @Test
    fun `perform runs a list of commands in order`() {
        val (robot, api) = fixture()
        val order = mutableListOf<String>()
        val a =
                object : Command {
                    override fun execute() {
                        order += "a"
                        robot.setTrackVelocities(1.0, 1.0)
                    }
                    override fun undo() {}
                }
        val b =
                object : Command {
                    override fun execute() {
                        order += "b"
                        robot.setTrackVelocities(2.0, 2.0)
                    }
                    override fun undo() {}
                }

        api.perform(listOf(a, b))

        assertEquals(listOf("a", "b"), order)
        assertEquals(2.0, robot.leftTrackVelocity)
    }

    @Test
    fun `undo and redo delegate to the invoker`() {
        val (robot, api) = fixture()
        api.perform(SetVelocityCommand(api.actuator, 80.0, 80.0))
        api.undo()
        assertEquals(0.0, robot.leftTrackVelocity)
        assertEquals(0.0, robot.rightTrackVelocity)

        api.redo()
        assertEquals(80.0, robot.leftTrackVelocity)
        assertEquals(80.0, robot.rightTrackVelocity)
    }
}
