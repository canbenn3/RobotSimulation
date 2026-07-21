package api

import command.SetVelocityCommand
import observer.Observer

/**
 * Bang-bang left-edge follower: never drives straight, only arcs off the left sensor.
 *
 * - Left sensor on the line → arc right until it falls off
 * - Left sensor off the line → arc left until it finds the line again
 *
 * Both tracks stay mostly forward so the robot creeps along while weaving.
 */
class FollowLineProgram : RobotProgram {
    private var robot: RobotApi? = null
    private var leftObserver: LeftObserver? = null

    private val outerSpeed = 90.0
    private val innerSpeed = 20.0

    override val name = "Left-Hand Line Maze"

    private inner class LeftObserver : Observer<Boolean> {
        override fun onUpdate(value: Boolean) {
            val r = robot ?: return
            val command =
                    if (value) {
                        // On the line — arc right until the sensor clears
                        SetVelocityCommand(r.actuator, outerSpeed, innerSpeed)
                    } else {
                        // Off the line — arc left until contact returns
                        SetVelocityCommand(r.actuator, innerSpeed, outerSpeed)
                    }
            r.perform(command)
        }
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        val left = LeftObserver()
        leftObserver = left
        robot.sensors.lineLeft.subscribe(left)
    }

    override fun stopProgram(robot: RobotApi) {
        leftObserver?.let { robot.sensors.lineLeft.unsubscribe(it) }
        leftObserver = null
        robot.perform(SetVelocityCommand(robot.actuator, 0.0, 0.0))
        this.robot = null
    }
}
