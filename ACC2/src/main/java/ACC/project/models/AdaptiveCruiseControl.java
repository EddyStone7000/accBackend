package ACC.project.models;

public class AdaptiveCruiseControl {
    private float desiredDistance;
    private float currentSpeed;
    private float currentDistance;
    private PIDController pidController;

    public AdaptiveCruiseControl(float desiredDistance) {
        this.desiredDistance = desiredDistance;
        this.pidController = new PIDController(desiredDistance);
    }

    public void update(float speed, float distance) {
        this.currentSpeed = speed;
        this.currentDistance = distance;
        // Bei jedem Update sicherstellen, dass der PID-Regler sauber startet
        if (Math.abs(desiredDistance - distance) < 0.5f) { // Toleranz von 0.5m
            pidController.reset();
        }
    }

    public float calculateControlSignal() {
        float error = desiredDistance - currentDistance;
        return pidController.calculateControl(error);
    }

    public float getDesiredDistance() {
        return desiredDistance;
    }

    public void setDesiredDistance(float desiredDistance) {
        this.desiredDistance = desiredDistance;
    }
}