package com.gft.backend.controllers;

import com.gft.backend.entities.FileStateMessage;
import com.gft.backend.entities.FolderNameSearch;
import com.gft.backend.utils.TreeFileSystemNodeRepresenter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import rx.functions.Action1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by miav on 2016-08-29.
 */
@Controller
@EnableScheduling
public class WebSocketController {

    private static final Logger logger = Logger.getLogger(WebSocketController.class);

    private volatile String folderName = null;

    @Autowired
    private FileSystemService fileService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/add")
    public void fetchFolder(FolderNameSearch folderNameSearch){
        logger.debug("$Receive message with content:" + folderNameSearch.getFolderName());
        if("#".equals(folderNameSearch.getFolderName())) {
            folderName = null;
        } else {
            folderName = new String(folderNameSearch.getFolderName());
            fileService.getFolderWatcherStream().subscribe(new Action1<FileStateMessage>() {
                @Override
                public void call(FileStateMessage message) {
                    simpMessagingTemplate.convertAndSend("/topic/show", message);
                    logger.debug("$Send content of " + message.getFileName());
                }
            });
            Path rootPath = Paths.get("c:/" + folderName);
            fileService.getFileHierarchyBasedOnPath(rootPath);
        }
    }

//    @Scheduled(fixedRate = 500)
//    @SendTo("/topic/show")
//    public void sendListOfFiles(){
//        if(folderName != null) {
//            try {
//                logger.debug("$Send content of " + folderName);
//                FolderList list = new FolderList();
//                String[] myList = listFilesViaStream(folderName);
//                list.setfList(myList);
//                simpMessagingTemplate.convertAndSend("/topic/show", list);
//            } catch (IOException e) {
//                logger.error(e);
//                folderName = null;
//            }
//        }
//    }
//    public String[] listFilesViaStream(String directoryName) throws IOException {
//        logger.debug("Try to get stream from " + directoryName);
//        String searchPath = "c:/";
//        Path rootPath = Paths.get(searchPath + directoryName);
//        String[] paths = Files.walk(rootPath)
//                .map(TreeFileSystemNodeRepresenter::representPathToString)
//                .toArray(size -> new String[size]);
//        String[] result = paths;
//        return result;
//    }
}
