package Main;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilitiesTest {

    @Test
    void distance() {
        assertEquals(23.32,Utilities.distance(0,0,12,20),.01);
        assertEquals(22.02,Utilities.distance(1,3,-21,4),.01);
        assertEquals(31.4,Utilities.distance(5,-7,-26,-12),.01);
        assertEquals(28.44,Utilities.distance(-5,-17,23,-12),.01);
    }
}