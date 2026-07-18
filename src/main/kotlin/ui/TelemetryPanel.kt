package ui

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import model.Robot

/**
 * A live readout of the sensor values — the *consumer* side of the Observer pattern.
 *
 * The layout (labels) is provided. Making it live is your job: in [bindTo] you subscribe an
 * observer to each sensor so the matching label updates when the sensor reports a reading.
 */
class TelemetryPanel : VBox(6.0) {

    private val title = StyledLabel("Telemetry", 15.0, bold = true)
    private val sonar = LabelObserver<Double>()
    private val temperature = LabelObserver<Double>()
    private val vision = LabelObserver<Color>()
    private val line = LabelObserver<Boolean>()
    private val collision = LabelObserver<Boolean>()

    init {
        padding = Insets(12.0)
        prefWidth = 210.0
        style = "-fx-background-color: #14171c;"
        children.addAll(
            title,
            captioned("Sonar (distance)", sonar),
            captioned("Temperature", temperature),
            captioned("Vision (color)", vision),
            captioned("Line L / C / R", line),
            captioned("Collision", collision),
        )
    }

    /**
     * Subscribe observers to the given robot's sensors so the labels update live. Called whenever
     * the robot is (re)created — on startup, environment change, and reset.
     *
     * TODO(student): subscribe an observer to each sensor and update the matching label, e.g.:
     * You can change the text of one of the Labels above by modifying the `text` property,
     * e.g: `vision.text = "The new text to display"`
     *
     * The labels (`sonar`, `temperature`, `vision`, `line`, `collision`) are ready to write to.
     * Until you do this, they stay "—". (This depends on your Observer pattern working — see
     * AbstractSubject.)
     */
    fun bindTo(robot: Robot) {
        robot.sonar.subscribe(sonar)
        robot.vision.subscribe(vision)
        robot.temperature.subscribe(temperature)
        robot.collision.subscribe(collision)
        robot.lineLeft.subscribe(line)
        robot.lineCenter.subscribe(line)
        robot.lineRight.subscribe(line)
    }

    private fun captioned(caption: String, value: Label): VBox =
        VBox(2.0, StyledLabel(caption, 11.0, color = "#8b949e"), value)
}
