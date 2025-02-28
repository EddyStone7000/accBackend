package ACC.project.models;

public class PIDController {
    private float Kp = 0.5f;
    private float Ki = 0.01f;
    private float Kd = 0.02f;
    private float setpoint;
    private float integral = 0.0f;
    private float previousError = 0.0f;
    private float maxOutput = 50.0f;
    private float minOutput = -50.0f;

    public PIDController(float setpoint) {
        this.setpoint = setpoint;
        reset(); // Beim Erstellen zurücksetzen
    }

    public float calculateControl(float error) {
        integral += error;
        float derivative = error - previousError;
        previousError = error;
        float output = Kp * error + Ki * integral + Kd * derivative;
        return Math.max(minOutput, Math.min(maxOutput, output));
    }

    public void reset() {
        integral = 0.0f; // Integral zurücksetzen
        previousError = 0.0f; // Vorheriger Fehler zurücksetzen
    }

    public float getKp() { return Kp; }
    public void setKp(float kp) { Kp = kp; }
}
