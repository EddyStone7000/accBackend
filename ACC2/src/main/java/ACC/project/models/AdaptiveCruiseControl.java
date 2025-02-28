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
    }

    public float calculateControlSignal() {
        float error = desiredDistance - currentDistance;
        return pidController.calculateControl(error);
    }

    // Getter und Setter
    public float getDesiredDistance() {

        return desiredDistance;
    }

    public void setDesiredDistance(float desiredDistance) {
        this.desiredDistance = desiredDistance;
    }
}
