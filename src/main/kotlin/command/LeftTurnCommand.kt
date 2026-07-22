package command

class LeftTurnCommand(actuator: RobotActuator, velocity: Double = 90.0) :
        SetVelocityCommand(actuator, velocity, -velocity)
