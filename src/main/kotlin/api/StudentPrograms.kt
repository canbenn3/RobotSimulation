package api

/**
 * The one place programs are registered with the system. Each program you register shows up in the
 * "Program" dropdown and can be launched with "Run Program".
 *
 * Until you register a program, the dropdown shows "(no programs registered)".
 */
object StudentPrograms {
    fun registerAll(registry: ProgramRegistry) {
        registry.register(FollowLineProgram())
        registry.register(FindHottestTempProgram())
        registry.register(SolveObstaclesProgram())
    }
}
