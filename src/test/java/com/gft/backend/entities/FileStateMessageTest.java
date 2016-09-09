package com.gft.backend.entities;

import com.gft.backend.controllers.WebSocketControllerTest;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by miav on 2016-09-08.
 */
public class FileStateMessageTest {
    @Test
    public void tryToMakeMessage() throws Exception {
        FileStateMessage message = new FileStateMessage();
        message.setDirectory(true);
        String fileName = "test.txt";
        message.setFileName(fileName);
        String[] parentPath = {"ru", "test", "message"};
        message.setParent(parentPath);
        message.setState(FileStateMessage.STATE_DELETED);
        assertEquals(true,message.isDirectory());
        assertEquals(fileName,message.getFileName());
        assertEquals(3,message.getParent().length);
        assertEquals("test",message.getParent()[1]);
        assertEquals(FileStateMessage.STATE_DELETED,message.getState());
    }

    @Test
    public void tryToGiveHashToMessage() throws Exception {
        FileStateMessage message = new FileStateMessage();
        message.setDirectory(true);
        String fileName = "test.txt";
        message.setFileName(fileName);
        String[] parentPath = {"ru", "test", "message"};
        message.setParent(parentPath);
        message.setHashId(FileStateMessage.getMD5Hash(
                message.getParent()[message.getParent().length-1]
                        + message.getFileName()));
        assertEquals("cb9129312069192a15d9fa41cb52bbb",message.getHashId());
    }
}
