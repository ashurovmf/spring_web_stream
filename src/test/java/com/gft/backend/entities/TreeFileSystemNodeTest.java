package com.gft.backend.entities;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by miav on 2016-09-01.
 */
public class TreeFileSystemNodeTest {
    //@Ignore
    @Test
    public void creatingNode() throws Exception {
        TreeFileSystemNode<String> node = new TreeFileSystemNode<String>();
        node.setData("Root");
        node.setDirectory(true);
        node.setLevel(3);

        assertEquals("Root",node.getData());
        assertTrue("Node is directory",node.isDirectory());
        assertEquals(3,node.getLevel());
    }

    @Test
    public void hierarchyWithNodes() throws Exception {
        TreeFileSystemNode<String> root = new TreeFileSystemNode<String>("Root");
        root.setDirectory(true);
        root.setLevel(3);

        for(int i = 0; i < 3; ++i) {
            TreeFileSystemNode<String> node = new TreeFileSystemNode<String>("Child"+i);
            root.addChild(node);
        }

        for ( TreeFileSystemNode<String> node : root) {
            assertTrue("Node is file", !node.isDirectory());
            assertEquals(4,node.getLevel());
        }
        assertEquals(3,root.childCount());
    }
}
