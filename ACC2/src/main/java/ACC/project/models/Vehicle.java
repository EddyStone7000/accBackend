package ACC.project.models;

public class Vehicle {
    private float speed;

    public Vehicle() {
        this.speed = 0.0f;
    }

    public void accelerate(float amount) {
        this.speed += amount;
    }

    public void brake(float amount) {
        this.speed = Math.max(0, this.speed - amount);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}