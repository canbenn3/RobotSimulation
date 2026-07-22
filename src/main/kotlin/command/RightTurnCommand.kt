package command

class RightTurnCommand(actuator: RobotActuator, velocity: Double = 90.0) :
        SetVelocityCommand(actuator, -velocity, velocity)
