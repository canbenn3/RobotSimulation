package command

class ForwardCommand(actuator: RobotActuator, velocity: Double = 120.0) :
        SetVelocityCommand(actuator, velocity, velocity)
