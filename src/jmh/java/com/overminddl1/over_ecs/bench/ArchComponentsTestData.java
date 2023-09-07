package com.overminddl1.over_ecs.bench;

import com.overminddl1.over_ecs.Component;

public class ArchComponentsTestData {
    public static class Transform implements Component {
        public int x, y;

        public Transform(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Component componentClone() {
            return new Transform(this.x, this.y);
        }

        @Override
        public void componentReset() {
            this.x = 0;
            this.y = 0;
        }
    }

    public static class Velocity implements Component {
        public int x, y;

        public Velocity(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Component componentClone() {
            return new Velocity(this.x, this.y);
        }

        @Override
        public void componentReset() {
            this.x = 0;
            this.y = 0;
        }
    }
}
