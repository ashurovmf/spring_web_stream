package com.gft.backend.entities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by miav on 2016-09-08.
 */
public class FolderListTest {

    @Test
    public void tryUseList() throws Exception {
        FolderList list = new FolderList();
        String[] sList = {"a", "b", "c"};
        list.setfList(sList);
        assertEquals(3,list.getfList().length);
        assertEquals("b",list.getfList()[1]);
    }
}
