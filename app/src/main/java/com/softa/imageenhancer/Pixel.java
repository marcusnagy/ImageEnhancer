package com.softa.imageenhancer;

public class Pixel implements Comparable <Pixel> {

    private final int index;
    private float value;

    private Pixel(int index, float value){
        this.index = index;
        this.value = value;
    };

    public int getIndex() {
        return index;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public static PixelFactory createPixelFactory() {
        return new PixelFactory();
    }


    @Override
    public int compareTo(Pixel o) {
        return Float.compare(this.value, o.getValue());
    }

    public static class PixelFactory {
        private int index;
        private float intensity;

        private PixelFactory(){};


        public PixelFactory index(int i) {
            index = i;
            return this;
        };

        public PixelFactory value(float i) {
            intensity = i;
            return this;
        };

        public Pixel build() {
            return new Pixel(index, intensity);
        }

    }
}
