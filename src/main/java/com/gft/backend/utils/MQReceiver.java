package com.gft.backend.utils;

import com.gft.backend.controllers.FirstShowController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by miav on 2016-08-22.
 */
public class MQReceiver {
    private static final Logger logger =
            LoggerFactory.getLogger(MQReceiver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(byte[] message) {
        logger.debug("Received {} bytes", message.length);
        latch.countDown();
    }

    public void receiveMessage(String message) {
        logger.debug("Received <{}>", message);
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
