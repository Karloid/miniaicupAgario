public class LibGdxObj extends Unit {
    public boolean isMy;

    public LibGdxObj(Unit other, boolean isMy) {
        this.radius = (float) other.radius;
        this.id = other.id;
        this.x = (float) other.x;
        this.y = (float) other.y;
        this.type = other.type;
        this.speedX = other.speedX;
        this.speedY = other.speedY;
        this.timeToFuse = other.timeToFuse;
        this.mass = other.mass;
        this.isMy = isMy;
    }
}
