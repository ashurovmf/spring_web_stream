package com.gft.backend.entities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by miav on 2016-09-08.
 */
public class FolderNameSearchTest {

    @Test
    public void tryToPrepareMessage() throws Exception {
        FolderNameSearch message = new FolderNameSearch();
        String testString = "testString";
        message.setFolderName(testString);
        assertEquals(testString,message.getFolderName());

    }
}
