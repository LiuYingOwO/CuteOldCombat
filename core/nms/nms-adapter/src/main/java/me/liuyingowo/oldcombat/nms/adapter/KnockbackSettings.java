package me.liuyingowo.oldcombat.nms.adapter;

public final class KnockbackSettings {

    public static final double DEFAULT_HORIZONTAL = 1.0D;
    public static final double DEFAULT_VERTICAL = 0.4000000059604645D;
    public static final double DEFAULT_VERTICAL_LIMIT = 0.4000000059604645D;
    public static final double DEFAULT_FRICTION = 0.5D;
    public static final double DEFAULT_MIN_DIRECTION_LENGTH = 1.0E-5D;
    public static final boolean DEFAULT_APPLY_RESISTANCE = true;

    public static final Snapshot DEFAULT = new Snapshot(
            false,
            DEFAULT_HORIZONTAL,
            DEFAULT_VERTICAL,
            DEFAULT_VERTICAL_LIMIT,
            DEFAULT_FRICTION,
            DEFAULT_MIN_DIRECTION_LENGTH,
            DEFAULT_APPLY_RESISTANCE
    );

    private static volatile Snapshot current = DEFAULT;

    private KnockbackSettings() {
    }

    public static Snapshot current() {
        return current;
    }

    public static void update(Snapshot settings) {
        current = settings == null ? DEFAULT : settings;
    }

    public static final class Snapshot {
        private final boolean enabled;
        private final double horizontal;
        private final double vertical;
        private final double verticalLimit;
        private final double friction;
        private final double minDirectionLength;
        private final boolean applyResistance;

        public Snapshot(boolean enabled,
                        double horizontal,
                        double vertical,
                        double verticalLimit,
                        double friction,
                        double minDirectionLength,
                        boolean applyResistance) {
            this.enabled = enabled;
            this.horizontal = finiteOrDefault(horizontal, DEFAULT_HORIZONTAL);
            this.vertical = finiteOrDefault(vertical, DEFAULT_VERTICAL);
            this.verticalLimit = finiteOrDefault(verticalLimit, DEFAULT_VERTICAL_LIMIT);
            this.friction = finiteOrDefault(friction, DEFAULT_FRICTION);
            this.minDirectionLength = Math.max(0.0D, finiteOrDefault(minDirectionLength, DEFAULT_MIN_DIRECTION_LENGTH));
            this.applyResistance = applyResistance;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public double getHorizontal() {
            return horizontal;
        }

        public double getVertical() {
            return vertical;
        }

        public double getVerticalLimit() {
            return verticalLimit;
        }

        public double getFriction() {
            return friction;
        }

        public double getMinDirectionLength() {
            return minDirectionLength;
        }

        public boolean isApplyResistance() {
            return applyResistance;
        }

        @Override
        public String toString() {
            return "enabled=" + enabled
                    + ", horizontal=" + horizontal
                    + ", vertical=" + vertical
                    + ", verticalLimit=" + verticalLimit
                    + ", friction=" + friction
                    + ", minDirectionLength=" + minDirectionLength
                    + ", applyResistance=" + applyResistance;
        }

        private static double finiteOrDefault(double value, double defaultValue) {
            return Double.isFinite(value) ? value : defaultValue;
        }
    }
}
