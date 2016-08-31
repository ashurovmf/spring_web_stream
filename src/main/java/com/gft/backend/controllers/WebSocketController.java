package com.gft.backend.controllers;

import com.gft.backend.entities.FolderList;
import com.gft.backend.entities.FolderNameSearch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.*;

/**
 * Created by miav on 2016-08-29.
 */
@Controller
@EnableScheduling
public class WebSocketController {

    private static final Logger logger = Logger.getLogger(WebSocketController.class);

    private volatile String folderName = null;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/add")
    public void fetchFolder(FolderNameSearch folderNameSearch){
        logger.debug("$Receive message with content" + folderNameSearch.getFolderName());
        if("#".equals(folderNameSearch.getFolderName())) {
            folderName = null;
        } else {
            folderName = new String(folderNameSearch.getFolderName());
        }
    }

    @Scheduled(fixedRate = 100)
    @SendTo("/topic/show")
    public void sendListOfFiles(){
        logger.debug("$Scheduler is triggered");
        if(folderName != null) {
            logger.debug("$Send content of " + folderName);
            FolderList list = new FolderList();
            String[] myList = listFilesAndFolders(folderName);
            list.setfList(myList);
            simpMessagingTemplate.convertAndSend("/topic/show", list);
        }

    }

    public String[] listFilesAndFolders(String directoryName){
        logger.debug("Try to get file from " + directoryName);
        File directory = new File("c:/");
        if(directoryName != null && !directoryName.isEmpty()) {
            directory = new File("c:/" + directoryName);
        }
        //get all the files from a directory
        File[] fList = directory.listFiles();
        List<String> buff = new ArrayList<>(fList.length);
        for (File file : fList){
            buff.add(file.getName());
        }
        String[] result = new String[buff.size()];
        result = buff.toArray(result);
        return result;
    }
}
