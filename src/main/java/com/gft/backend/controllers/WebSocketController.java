package com.gft.backend.controllers;

import com.gft.backend.entities.FileStateMessage;
import com.gft.backend.entities.FolderNameSearch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import rx.Subscription;
import rx.functions.Action1;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Iterator;

/**
 * Created by miav on 2016-08-29.
 */
@Controller
public class WebSocketController {

    private static final Logger logger = Logger.getLogger(WebSocketController.class);

    @Autowired
    private FileSystemService fileService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/add")
    public void fetchFolder(Message<Object> inMessage, @Payload FolderNameSearch folderNameSearch){
        logger.debug("$Receive message with content:" + folderNameSearch.getFolderName());
        final String authedSender = getSessionIdOrUser(inMessage);
        Path targetPath = getTargetPath(folderNameSearch);
        logger.debug("$Try to get hierarchy for " + folderNameSearch.getFolderName());
        Subscription subscribe = fileService.getFileHierarchyBasedOnPath(targetPath).subscribe(new Action1<FileStateMessage>() {
            @Override
            public void call(FileStateMessage message) {
                logger.debug("$Try to send " + message.getFileName() + " with st:" + message.getState());
                simpMessagingTemplate.convertAndSendToUser(authedSender, "/topic/show", message, createHeaders(authedSender));
                logger.debug("$Send content of " + message.getFileName() + " to session id " + authedSender);
            }
        });
        logger.debug("$Subscribed for " + folderNameSearch.getFolderName());
    }

    private Path getTargetPath(FolderNameSearch folderNameSearch) {
        Path rootPath;
        Iterator<Path> rootDirectories = FileSystems.getDefault().getRootDirectories().iterator();
        if(rootDirectories.hasNext()) { rootPath = rootDirectories.next(); } else { rootPath = Paths.get("c:/"); }
        Path targetPath = Paths.get(rootPath + folderNameSearch.getFolderName());
        File targetFile = targetPath.toFile();
        if(!targetFile.exists() || !targetFile.isDirectory()){
            targetPath = rootPath; //targetPath = rootPath; //Paths.get("/data");
        }
        return targetPath;
    }

    private String getSessionIdOrUser(Message<Object> inMessage) {
        String result = "";
        logger.debug("$Parse input message:" + inMessage.getHeaders());
        String session_id = inMessage.getHeaders().get(SimpMessageHeaderAccessor.SESSION_ID_HEADER, String.class);
        if(session_id != null) {
            result = session_id;
        }
//        Principal principal = inMessage.getHeaders().get(SimpMessageHeaderAccessor.USER_HEADER, Principal.class);
//        if(principal != null) {
//            result = principal.getName();
//        }
        return result;
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    @MessageExceptionHandler
    @SendToUser(destinations="/queue/errors", broadcast=false)
    public String handleException(Exception ex) {
        return ex.getMessage();
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
