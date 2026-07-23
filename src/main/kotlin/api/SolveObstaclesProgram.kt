package api

import command.ForwardCommand
import command.RightTurnCommand
import command.SetVelocityCommand
import javafx.scene.paint.Color
import observer.Observer

/** Drive forward; spin on bumps; chase the red ball if we see it. */
class SolveObstaclesProgram : RobotProgram {
    private var robot: RobotApi? = null
    private var onCollision: Observer<Boolean>? = null
    private var onVision: Observer<Color>? = null
    private var onSonar: Observer<Double>? = null

    private var seesBall = false
    private var sonar = 999.0

    private val speed = 100.0
    private val turn = 90.0

    override val name = "Solve Obstacles — find the ball"

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        seesBall = false
        sonar = 999.0

        onVision = Observer { color ->
            seesBall = color.red > 0.7 && color.green < 0.45 && color.blue < 0.45
        }
        onSonar = Observer { dist -> sonar = dist }
        onCollision = Observer { hit ->
            val r = this.robot ?: return@Observer

            // bump, or wall close ahead while not chasing the ball → spin away
            if (hit || (!seesBall && sonar < 70)) {
                r.perform(RightTurnCommand(r.actuator, turn))
            } else {
                r.perform(ForwardCommand(r.actuator, speed))
            }
        }

        robot.sensors.vision.subscribe(onVision!!)
        robot.sensors.sonar.subscribe(onSonar!!)
        robot.sensors.collision.subscribe(onCollision!!)
        robot.perform(ForwardCommand(robot.actuator, speed))
    }

    override fun stopProgram(robot: RobotApi) {
        onCollision?.let { robot.sensors.collision.unsubscribe(it) }
        onVision?.let { robot.sensors.vision.unsubscribe(it) }
        onSonar?.let { robot.sensors.sonar.unsubscribe(it) }
        onCollision = null
        onVision = null
        onSonar = null
        robot.perform(SetVelocityCommand(robot.actuator, 0.0, 0.0))
        this.robot = null
    }
}
