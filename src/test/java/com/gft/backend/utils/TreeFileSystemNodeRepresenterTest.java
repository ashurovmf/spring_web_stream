package com.gft.backend.utils;

import com.gft.backend.entities.TreeFileSystemNode;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by miav on 2016-09-01.
 */
public class TreeFileSystemNodeRepresenterTest {

    TreeFileSystemNode<String> root;

    @Before
    public void setup() {
        root = new TreeFileSystemNode<String>("Root");
        root.setDirectory(true);
        root.setLevel(3);

        for(int i = 0; i < 3; ++i) {
            TreeFileSystemNode<String> node = new TreeFileSystemNode<String>("Child"+i);
            root.addChild(node);
        }
    }

    @Test
    public void convertTreeToStringArray() throws Exception {
        String[] result = TreeFileSystemNodeRepresenter.convertToStringArray(root);
        assertEquals(4,result.length);
        assertTrue("For root node","_________|->[Root]".equals(result[0]));
        assertTrue("For first child node","____________|->Child0".equals(result[1]));
    }

    @Test
    public void convertPathToString() throws Exception {
        Path testPath = Paths.get(".");
        String result = TreeFileSystemNodeRepresenter.representPathToString(testPath);
        assertTrue("String not empty",!result.isEmpty());
        assertTrue("String contains file name",result.contains(testPath.getFileName().toString()));
    }
}
