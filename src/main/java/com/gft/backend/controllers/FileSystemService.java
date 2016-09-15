package com.gft.backend.controllers;

import com.gft.backend.entities.FileStateMessage;
import com.gft.backend.utils.WatchServiceBean;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.subjects.ReplaySubject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by miav on 2016-09-06.
 */
@Service
public class FileSystemService {

    private static final Logger logger = Logger.getLogger(FileSystemService.class);

    @Autowired
    private WatchServiceBean watchService;

    public FileSystemService(){

    }

    public Observable<FileStateMessage> getFileHierarchyBasedOnPath(Path basePath){
        ReplaySubject<FileStateMessage> watchStream = ReplaySubject.create();
        logger.debug("Try to create iterator for files");
        FileHierarchyIterator iterator = new FileHierarchyIterator(basePath, watchService.getWatchService());
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    WatchKey watchKey;
                    try {
                        logger.info("Watch key try to be taken");
                        watchKey = watchService.getWatchService().take();
                        for (WatchEvent event : watchKey.pollEvents()) {
                            logger.debug("Parse watch event " + event.count());
                            WatchEvent.Kind kind = event.kind();
                            Path changed = (Path) event.context();
                            Path root = ((Path) event.context()).getRoot();
                            logger.debug("Get watch event " + kind.name() + " for " + root);
                            FileStateMessage message = createFileStateMessage(changed.toFile());
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                message.setState(FileStateMessage.STATE_ADDED);
                            }
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                message.setState(FileStateMessage.STATE_MODIFIED);
                            }
                            if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                message.setState(FileStateMessage.STATE_DELETED);
                            }
                            logger.debug("Message is prepared before sent:" + message.getFileName());
                            watchStream.onNext(message);
                        }
                        logger.debug("Try to reset key");
                        boolean valid = watchKey.reset();
                        if (!valid) {
                            logger.error("Watch key has been unregistered");
                        }
                    } catch (Exception e) {
                        logger.error("Watch loop is interrupted",e);
                        return;
                    }
                }
            }
        });
        logger.debug("Organize a loop for scan");
        while (iterator.hasNext()){
            File file = iterator.next();
            FileStateMessage message = createFileStateMessage(file);
            message.setState(FileStateMessage.STATE_ADDED);
            watchStream.onNext(message);
            logger.debug("Message " + message.getFileName()+ " from iterator is sent");
        }
        return watchStream;
    }

    public static FileStateMessage createFileStateMessage(File file) {
        logger.debug("Try to create message for " + file.toString());
        FileStateMessage message = new FileStateMessage();
        message.setFileName(file.getName());
        String parentPathStr = file.getParent();
        logger.debug("Parent path " + parentPathStr);
        if(parentPathStr == null){
            parentPathStr = ".";
        }
        message.setParent(parentPathStr.split(Pattern.quote(File.separator)));
//        message.setHashId(FileStateMessage.getMD5Hash(lastParent+message.getFileName()));
        if(file.isDirectory()) message.setDirectory(true);
        message.setHashId(Integer.toHexString(file.hashCode()));
        logger.debug("Message is prepared " + message.getFileName());
        return message;
    }

    class FileHierarchyIterator implements Iterator<File>{

        private WatchService service;
        private Path basePath;
        private Stack<File> fileStack = new Stack<>();
        private HashSet<WatchKey> regKeySet = new HashSet<>();

        public FileHierarchyIterator(Path rootPath, WatchService watchService) throws InvalidPathException {
            service = watchService;
            basePath = rootPath;
            File currentDir = basePath.toFile();
            registerWatcherForPath(rootPath);
            if(!currentDir.isDirectory()) {
                throw new InvalidPathException(rootPath.toString(), "Root path is not a directory");
            }
            addToStack(currentDir);
        }

        private void addToStack(File dir) {
            File[] listFiles = dir.listFiles();
            for (File cFile : listFiles)  fileStack.push(cFile);
        }

        public void registerWatcherForPath(Path path) {
            try {
                WatchKey keyReg = path.register(service,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                regKeySet.add(keyReg);
                logger.debug("New key reg is " + keyReg.isValid());
            } catch (IOException e) {
                logger.error("Fail with path register", e);
            }
        }

        @Override
        public boolean hasNext() {
            return !fileStack.empty();
        }

        @Override
        public File next() {
            File nextFile = fileStack.pop();
            if(nextFile.isDirectory()) {
                addToStack(nextFile);
                registerWatcherForPath(nextFile.toPath());
            }
            return nextFile;
        }

        public void close(){
            for(WatchKey key : regKeySet){
                key.cancel();
            }
        }
    }
}
