package api

import command.ForwardCommand
import command.LeftTurnCommand
import command.ReverseCommand
import command.SetVelocityCommand
import observer.Observer

/**
 * Gradient-ascent toward the hot spot:
 *
 * - Remember the previous hottest temperature seen.
 * - On each temperature update, compare the new reading against that previous hottest.
 * - If hotter → update the previous hottest and keep driving forward.
 * - If cooler → reverse until that hottest temperature is recovered, then rotate and try again.
 */
class FindHottestTempProgram : RobotProgram {
    private var robot: RobotApi? = null
    private var tempObserver: TempObserver? = null

    /** Hottest temperature observed so far — compared against each new sensor reading. */
    private var previousHottestTemp = Double.NEGATIVE_INFINITY

    private var phase = Phase.FORWARD
    private var turnTicksRemaining = 0

    private val driveSpeed = 80.0
    private val turnSpeed = 70.0
    private val turnTicks = 18

    override val name = "Find Hottest Temperature"

    private enum class Phase {
        FORWARD,
        REVERSE,
        TURN,
    }

    private inner class TempObserver : Observer<Double> {
        override fun onUpdate(value: Double) {
            val r = robot ?: return

            when (phase) {
                Phase.TURN -> handleTurn(r, value)
                Phase.REVERSE -> handleReverse(r, value)
                Phase.FORWARD -> handleForward(r, value)
            }
        }

        private fun handleForward(r: RobotApi, value: Double) {
            if (value > previousHottestTemp) {
                // Hotter than before — keep going this way and remember the new peak.
                previousHottestTemp = value
                r.perform(ForwardCommand(r.actuator, driveSpeed))
            } else if (value < previousHottestTemp) {
                // Cooler than the previous hottest — backtrack toward it.
                phase = Phase.REVERSE
                r.perform(ReverseCommand(r.actuator, driveSpeed))
            } else {
                // Same reading as the previous hottest (common with 0.1° rounding) — keep moving.
                r.perform(ForwardCommand(r.actuator, driveSpeed))
            }
        }

        private fun handleReverse(r: RobotApi, value: Double) {
            if (value > previousHottestTemp) {
                // Found something even hotter while reversing — take it and go forward.
                previousHottestTemp = value
                phase = Phase.FORWARD
                r.perform(ForwardCommand(r.actuator, driveSpeed))
            } else if (value >= previousHottestTemp) {
                // Back at the previous hottest temperature — rotate a little, then try again.
                phase = Phase.TURN
                turnTicksRemaining = turnTicks
                r.perform(LeftTurnCommand(r.actuator, turnSpeed))
            } else {
                r.perform(ReverseCommand(r.actuator, driveSpeed))
            }
        }

        private fun handleTurn(r: RobotApi, value: Double) {
            if (value > previousHottestTemp) {
                // Hotter while turning — commit to this heading.
                previousHottestTemp = value
                phase = Phase.FORWARD
                turnTicksRemaining = 0
                r.perform(ForwardCommand(r.actuator, driveSpeed))
                return
            }
            turnTicksRemaining--
            if (turnTicksRemaining <= 0) {
                phase = Phase.FORWARD
                r.perform(ForwardCommand(r.actuator, driveSpeed))
            }
        }
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        previousHottestTemp = Double.NEGATIVE_INFINITY
        phase = Phase.FORWARD
        turnTicksRemaining = 0
        val observer = TempObserver()
        tempObserver = observer
        robot.sensors.temperature.subscribe(observer)
        robot.perform(ForwardCommand(robot.actuator, driveSpeed))
    }

    override fun stopProgram(robot: RobotApi) {
        tempObserver?.let { robot.sensors.temperature.unsubscribe(it) }
        tempObserver = null
        robot.perform(SetVelocityCommand(robot.actuator, 0.0, 0.0))
        this.robot = null
        previousHottestTemp = Double.NEGATIVE_INFINITY
        phase = Phase.FORWARD
        turnTicksRemaining = 0
    }
}
