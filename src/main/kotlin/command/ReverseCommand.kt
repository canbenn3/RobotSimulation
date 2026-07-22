package command

class ReverseCommand(actuator: RobotActuator, velocity: Double = 120.0) :
        SetVelocityCommand(actuator, -velocity, -velocity)
