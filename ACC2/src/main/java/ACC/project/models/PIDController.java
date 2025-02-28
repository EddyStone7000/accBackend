package ACC.project.models;

public class PIDController {
    private float Kp = 1.0f;
    private float Ki = 0.1f;
    private float Kd = 0.05f;
    private float setpoint;
    private float integral = 0.0f;
    private float previousError = 0.0f;

    public PIDController(float setpoint) {
        this.setpoint = setpoint;
    }

    public float calculateControl(float error) {
        integral += error;
        float derivative = error - previousError;
        previousError = error;
        return Kp * error + Ki * integral + Kd * derivative;
    }

    // Getter und Setter
    public float getKp() {
        return Kp;
    }

    public void setKp(float kp) {
        Kp = kp;
    }
}
