package ACC.project.models;

public class Vehicle {
    private float speed;
    private float acceleration;

    public void accelerate(float amount) {
        this.speed += amount;
    }

    public void brake(float amount) {
        this.speed = Math.max(0, this.speed - amount);
    }

    // Getter und Setter
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
