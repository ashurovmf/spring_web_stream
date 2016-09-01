package com.gft.backend.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by miav on 2016-09-01.
 */
public class MQReceiverTest {

    @Test
    public void processMessages() throws Exception {
        MQReceiver receiver = new MQReceiver();
        String message = "test_message";
        receiver.receiveMessage(message);
        message = "west_ost";
        receiver.receiveMessage(message.getBytes());
        assertEquals(0,receiver.getLatch().getCount());
    }
}
