import api.DefaultProgramRegistry
import api.DefaultRobotApi
import api.FollowLineProgram
import api.StudentPrograms
import command.CommandInvoker
import environment.AbstractEnvironment
import environment.LineSegment
import environment.Obstacle
import geometry.Pose
import geometry.Rectangle
import geometry.Vector2
import model.Robot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FollowLineProgramTest {

    private class LineWorld : AbstractEnvironment() {
        override val name = "line-program-test"
        override val bounds = Rectangle(0.0, 0.0, 2000.0, 2000.0)
        override val obstacles = emptyList<Obstacle>()
        override val lines = listOf(LineSegment(Vector2(0.0, 100.0), Vector2(400.0, 100.0)))
        override val lineWidth = 16.0
        override fun startPose() = Pose(100.0, 100.0, 0.0)
    }

    private fun fixture(): Triple<Robot, DefaultRobotApi, FollowLineProgram> {
        val robot = Robot(LineWorld().startPose(), radius = 16.0)
        val invoker = CommandInvoker()
        val api =
                DefaultRobotApi(
                        invoker = invoker,
                        actuatorProvider = { robot },
                        sensorsProvider = { robot },
                )
        return Triple(robot, api, FollowLineProgram())
    }

    @Test
    fun `left sensor on arcs right — outer left track, inner right track`() {
        val (robot, api, program) = fixture()
        program.startProgram(api)

        robot.lineLeft.notifyObservers(true)

        assertEquals(90.0, robot.leftTrackVelocity)
        assertEquals(20.0, robot.rightTrackVelocity)
    }

    @Test
    fun `left sensor off arcs left — inner left track, outer right track`() {
        val (robot, api, program) = fixture()
        program.startProgram(api)

        robot.lineLeft.notifyObservers(false)

        assertEquals(20.0, robot.leftTrackVelocity)
        assertEquals(90.0, robot.rightTrackVelocity)
    }

    @Test
    fun `program weaves when the left sensor toggles`() {
        val (robot, api, program) = fixture()
        program.startProgram(api)

        robot.lineLeft.notifyObservers(true)
        assertTrue(robot.leftTrackVelocity > robot.rightTrackVelocity)

        robot.lineLeft.notifyObservers(false)
        assertTrue(robot.leftTrackVelocity < robot.rightTrackVelocity)

        robot.lineLeft.notifyObservers(true)
        assertTrue(robot.leftTrackVelocity > robot.rightTrackVelocity)
    }

    @Test
    fun `stopProgram unsubscribes and stops the robot`() {
        val (robot, api, program) = fixture()
        program.startProgram(api)
        robot.lineLeft.notifyObservers(true)
        assertTrue(robot.leftTrackVelocity != 0.0)

        program.stopProgram(api)

        assertEquals(0.0, robot.leftTrackVelocity)
        assertEquals(0.0, robot.rightTrackVelocity)

        // Further notifications must not drive the robot
        robot.lineLeft.notifyObservers(true)
        assertEquals(0.0, robot.leftTrackVelocity)
        assertEquals(0.0, robot.rightTrackVelocity)
    }

    @Test
    fun `stopProgram without start still stops the robot safely`() {
        val (robot, api, program) = fixture()
        robot.setTrackVelocities(50.0, 50.0)

        // leftObserver is null — exercises the safe-call miss branch
        program.stopProgram(api)

        assertEquals(0.0, robot.leftTrackVelocity)
        assertEquals(0.0, robot.rightTrackVelocity)
    }

    @Test
    fun `observer ignores updates when the program has no robot`() {
        val (robot, api, program) = fixture()
        program.startProgram(api)
        robot.lineLeft.notifyObservers(true)
        assertEquals(90.0, robot.leftTrackVelocity)

        // Clear the stored api while leaving the subscription active
        val robotField = FollowLineProgram::class.java.getDeclaredField("robot")
        robotField.isAccessible = true
        robotField.set(program, null)

        robot.lineLeft.notifyObservers(false)
        assertEquals(90.0, robot.leftTrackVelocity)
        assertEquals(20.0, robot.rightTrackVelocity)
    }

    @Test
    fun `StudentPrograms registers the follow-line program`() {
        val registry = DefaultProgramRegistry()
        StudentPrograms.registerAll(registry)

        assertEquals(1, registry.programs().size)
        assertEquals("Left-Hand Line Maze", registry.programs().single().name)
    }
}
