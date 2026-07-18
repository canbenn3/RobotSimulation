package command

class SetVelocityCommand(
        private val actuator: RobotActuator,
        private val leftDrive: Double,
        private val rightDrive: Double
) : Command {
    private val prevLeft = actuator.leftTrackVelocity
    private val prevRight = actuator.rightTrackVelocity

    override fun execute() {
        actuator.setTrackVelocities(leftDrive, rightDrive)
    }

    override fun undo() {
        actuator.setTrackVelocities(prevLeft, prevRight)
    }
}
