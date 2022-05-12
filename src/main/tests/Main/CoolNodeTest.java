package Main;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoolNodeTest {

    CoolNode<Integer>[] nodes;
    @BeforeEach
    void setUp(){
        nodes = new CoolNode[10];
        for(int i =0;i<nodes.length;++i){
            nodes[i] = new CoolNode<>(i*10);
        }
    }
    @AfterEach
    void tearDown(){
        nodes = null;
    }
    @Test
    void connectToNodeDirected() {
        assertEquals(0,nodes[0].getAttachedNodes().size());
        assertFalse(nodes[0].getAttachedNodes().contains(nodes[2]));
        nodes[0].connectToNodeDirected(nodes[2]);
        assertEquals(1,nodes[0].getAttachedNodes().size());
        assertTrue(nodes[0].getAttachedNodes().contains(nodes[2]));
        assertNotEquals(1,nodes[2].getAttachedNodes().size());
        assertFalse(nodes[2].getAttachedNodes().contains(nodes[0]));
    }

    @Test
    void connectToNodeUndirected() {
        assertEquals(0,nodes[0].getAttachedNodes().size());
        assertFalse(nodes[0].getAttachedNodes().contains(nodes[2]));
        assertEquals(0,nodes[2].getAttachedNodes().size());
        assertFalse(nodes[2].getAttachedNodes().contains(nodes[0]));
        nodes[0].connectToNodeUndirected(nodes[2]);
        assertEquals(1,nodes[0].getAttachedNodes().size());
        assertTrue(nodes[0].getAttachedNodes().contains(nodes[2]));
        assertEquals(1,nodes[2].getAttachedNodes().size());
        assertTrue(nodes[2].getAttachedNodes().contains(nodes[0]));
    }

    @Test
    void disconnectNodeDirected() {
        nodes[0].connectToNodeUndirected(nodes[2]);
        assertEquals(1,nodes[0].getAttachedNodes().size());
        assertTrue(nodes[0].getAttachedNodes().contains(nodes[2]));
        assertEquals(1,nodes[2].getAttachedNodes().size());
        assertTrue(nodes[2].getAttachedNodes().contains(nodes[0]));
        nodes[0].disconnectNodeDirected(nodes[2]);
        assertEquals(0,nodes[0].getAttachedNodes().size());
        assertFalse(nodes[0].getAttachedNodes().contains(nodes[2]));
        assertEquals(1,nodes[2].getAttachedNodes().size());
        assertTrue(nodes[2].getAttachedNodes().contains(nodes[0]));
    }

    @Test
    void disconnectNodeUnDirected() {
        nodes[0].connectToNodeUndirected(nodes[2]);
        assertEquals(1,nodes[0].getAttachedNodes().size());
        assertTrue(nodes[0].getAttachedNodes().contains(nodes[2]));
        assertEquals(1,nodes[2].getAttachedNodes().size());
        assertTrue(nodes[2].getAttachedNodes().contains(nodes[0]));
        nodes[0].disconnectNodeUnDirected(nodes[2]);
        assertEquals(0,nodes[0].getAttachedNodes().size());
        assertFalse(nodes[0].getAttachedNodes().contains(nodes[2]));
        assertEquals(0,nodes[2].getAttachedNodes().size());
        assertFalse(nodes[2].getAttachedNodes().contains(nodes[0]));
    }
}