package ACC.project.models;

public class PIDController {
    private float Kp = 0.5f; //wie stark der Regler auf den aktuellen Fehler. Schwingungsgefahr zB 1.0f reagiert
    private float Ki = 0.01f; //korrigiert langfristige Abweichungen
    private float Kd = 0.02f; //(z. B. Kd = 0.1f): Der Regler dämpft Schwingungen, zu hoher Wert kann die Reaktion verlangsamen
    private float setpoint;
    private float integral = 0.0f; //um langfristige Abweichungen zu korrigieren.
    private float previousError = 0.0f; //Dämpfung
    private float maxOutput = 50.0f; //stärkeres Bremsen oder Beschleunigen
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

    public float getKp() {
        return Kp;
    }
    public void setKp(float kp) {
        Kp = kp;
    }

    public void setKi(float v) {
    }

    public void setKd(float v) {
    }

    public void setOutputLimits(float v, float v1) {
    }

    public void setSetpoint(float targetDistance) {
    }
}