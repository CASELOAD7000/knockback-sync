package me.caseload.knockbacksync.util.data;

import java.util.Objects;

public final class Vec2d {

    public final double x;
    public final double z;

    public Vec2d(double x, double z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (!(object instanceof Vec2d))
            return false;

        Vec2d v = (Vec2d) object;
        return Double.compare(v.x, x) == 0 && Double.compare(v.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "Vec2d(" + x + ", " + z + ")";
    }
}
