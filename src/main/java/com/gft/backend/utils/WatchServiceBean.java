package com.gft.backend.utils;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

/**
 * Created by miav on 2016-09-14.
 */
public class WatchServiceBean {

    private static final Logger logger = Logger.getLogger(WatchServiceBean.class);

    WatchService watchService = null;

    public WatchServiceBean() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            logger.error("Fail with watch service creation:",e);
        }
    }

    public WatchService getWatchService(){
        return watchService;
    }

    public void close(){
        try {
            watchService.close();
        } catch (IOException e) {
            logger.error("Fail with watch service closing:",e);
        }
    }
}
