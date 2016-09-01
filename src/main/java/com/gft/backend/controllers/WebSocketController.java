package com.gft.backend.controllers;

import com.gft.backend.entities.FolderList;
import com.gft.backend.entities.FolderNameSearch;
import com.gft.backend.entities.TreeFileSystemNode;
import com.gft.backend.utils.TreeFileSystemNodeRepresenter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Scheduled(fixedRate = 500)
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
        String searchPath = "c:/";
        if(directoryName != null && !directoryName.isEmpty()) {
            searchPath += directoryName;
        }

        TreeFileSystemNode<String> rootNode = new TreeFileSystemNode<>();
        rootNode.setDirectory(true);
        rootNode.setLevel(0);
        recursiveScanDirs(rootNode,searchPath);

        String[] result = TreeFileSystemNodeRepresenter.convertToStringArray(rootNode);
        return result;
    }

    public TreeFileSystemNode<String> recursiveScanDirs(TreeFileSystemNode<String> rootNode, String directoryName){
        File directory = new File(directoryName);
        rootNode.setData(directory.getName());

        File[] fList = directory.listFiles();
        for (File file : fList){
            TreeFileSystemNode<String> childNode = new TreeFileSystemNode<>(file.getName());

            if(file.isDirectory()){
                childNode.setDirectory(true);
                rootNode.addChild(childNode);
                recursiveScanDirs(childNode,directoryName+"/" +file.getName());
            }
            else {
                rootNode.addChild(childNode);
            }
        }
        return rootNode;
    }
}
