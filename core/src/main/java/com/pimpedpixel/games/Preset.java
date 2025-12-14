package com.pimpedpixel.games;

public enum Preset {
        SD_640x400(640, 400),
        HD_1280x800(1280, 800);

        private final int width;
        private final int height;

        Preset(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
