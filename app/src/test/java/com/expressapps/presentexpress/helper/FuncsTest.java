package com.expressapps.presentexpress.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class FuncsTest {
    public static class TransformRangeTest {
        @Test
        public void transformRange_middleValue_correctTransformation() {
            // Transform 5 from range [0,10] to range [0,100]
            float result = Funcs.transformRange(5.0f, 0.0f, 10.0f, 0.0f, 100.0f);
            assertEquals(50.0f, result, 0.001f);
        }

        @Test
        public void transformRange_minimumValue_correctTransformation() {
            // Transform 0 from range [0,10] to range [20,30]
            float result = Funcs.transformRange(0.0f, 0.0f, 10.0f, 20.0f, 30.0f);
            assertEquals(20.0f, result, 0.001f);
        }

        @Test
        public void transformRange_maximumValue_correctTransformation() {
            // Transform 10 from range [0,10] to range [20,30]
            float result = Funcs.transformRange(10.0f, 0.0f, 10.0f, 20.0f, 30.0f);
            assertEquals(30.0f, result, 0.001f);
        }

        @Test
        public void transformRange_negativeRanges_correctTransformation() {
            // Transform -5 from range [-10,0] to range [100,200]
            float result = Funcs.transformRange(-5.0f, -10.0f, 0.0f, 100.0f, 200.0f);
            assertEquals(150.0f, result, 0.001f);
        }

        @Test
        public void transformRange_invertedTargetRange_correctTransformation() {
            // Transform 2 from range [0,4] to range [100,0] (inverted)
            float result = Funcs.transformRange(2.0f, 0.0f, 4.0f, 100.0f, 0.0f);
            assertEquals(50.0f, result, 0.001f);
        }

        @Test
        public void transformRange_valueOutsideRange_correctExtrapolation() {
            // Transform 12 from range [0,10] to range [0,100] (value is outside source range)
            float result = Funcs.transformRange(12.0f, 0.0f, 10.0f, 0.0f, 100.0f);
            assertEquals(120.0f, result, 0.001f);
        }

        @Test
        public void transformRange_decimalValues_correctTransformation() {
            // Transform 2.5 from range [1.0,4.0] to range [10.0,40.0]
            float result = Funcs.transformRange(2.5f, 1.0f, 4.0f, 10.0f, 40.0f);
            assertEquals(25.0f, result, 0.001f);
        }

        @Test
        public void transformRange_sameRanges_identityTransformation() {
            // Transform 5 from range [0,10] to range [0,10]
            float result = Funcs.transformRange(5.0f, 0.0f, 10.0f, 0.0f, 10.0f);
            assertEquals(5.0f, result, 0.001f);
        }

        @Test
        public void transformRange_unitToLargerRange_correctTransformation() {
            // Transform 0.3 from range [0,1] to range [0,255] (like color values)
            float result = Funcs.transformRange(0.3f, 0.0f, 1.0f, 0.0f, 255.0f);
            assertEquals(76.5f, result, 0.001f);
        }

        @Test
        public void transformRange_temperatureScales_correctTransformation() {
            // Transform 0 from range [0,100] to range [32,212] (like C to F)
            float result = Funcs.transformRange(0.0f, 0.0f, 100.0f, 32.0f, 212.0f);
            assertEquals(32.0f, result, 0.001f);

            // Transform 100 from range [0,100] to range [32,212]
            result = Funcs.transformRange(100.0f, 0.0f, 100.0f, 32.0f, 212.0f);
            assertEquals(212.0f, result, 0.001f);
        }

        @Test
        public void transformRange_smallRanges_correctTransformation() {
            // Transform 0.0005 from range [0.0001,0.001] to range [1,10]
            float result = Funcs.transformRange(0.0005f, 0.0001f, 0.001f, 1.0f, 10.0f);
            assertEquals(5.0f, result, 0.001f);
        }

        @Test
        public void transformRange_largeRanges_correctTransformation() {
            // Transform 500000 from range [0,1000000] to range [0,100]
            float result = Funcs.transformRange(500000.0f, 0.0f, 1000000.0f, 0.0f, 100.0f);
            assertEquals(50.0f, result, 0.001f);
        }

        @Test
        public void transformRange_percentageToAngle_correctTransformation() {
            // Transform 50% (0.5) from range [0,1] to range [0,360] degrees
            float result = Funcs.transformRange(0.5f, 0.0f, 1.0f, 0.0f, 360.0f);
            assertEquals(180.0f, result, 0.001f);
        }
    }
}