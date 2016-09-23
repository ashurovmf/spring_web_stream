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
import java.util.concurrent.ConcurrentHashMap;
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

    private ConcurrentHashMap<String,File> directoryCache = new ConcurrentHashMap<>(12);

    private ConcurrentHashMap<Object, ReplaySubject<FileStateMessage>> streamCache = new ConcurrentHashMap<>(12);

    public FileSystemService(){

    }

    public Observable<FileStateMessage> getFileHierarchyBasedOnPath(Path basePath){
        ReplaySubject<FileStateMessage> watchStream = ReplaySubject.create();
        streamCache.put(watchStream,watchStream);
        logger.debug("Try to create iterator for files");
        FileHierarchyIterator iterator = new FileHierarchyIterator(basePath,
                watchService.getWatchService(),
                directoryCache);
        registerWatcher(basePath);
        directoryCache.put(basePath.toFile().getName(),basePath.toFile());
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
                            Path parDir = (Path) watchKey.watchable();
                            Path changed = parDir.resolve((Path) event.context());
                            logger.debug("Get watch event " + kind.name() + " for " + parDir);
                            FileStateMessage message = createFileStateMessage(changed.toFile(), parDir);
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
                            streamCache.values().stream().forEach( o -> o.onNext(message));
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
            FileStateMessage message = createFileStateMessage(file, null);
            message.setState(FileStateMessage.STATE_ADDED);
            watchStream.onNext(message);
            logger.debug("Message " + message.getFileName()+ " from iterator is prepared");
        }
        return watchStream;
    }

    public static FileStateMessage createFileStateMessage(File file, Path parentPath) {
        logger.debug("Try to create message for " + file.toString());
        FileStateMessage message = new FileStateMessage();
        message.setFileName(file.getName());
        String parentPathStr = file.getParent();
        if(parentPathStr == null){
            if(parentPath != null){
                parentPathStr = parentPath.toString();
            }
            else parentPathStr = ".";
        }
        logger.debug("Parent path " + parentPathStr);
        message.setParent(parentPathStr.split(Pattern.quote(File.separator)));
        message.setHashId(FileStateMessage.getMD5Hash(message.getFileName()));
        if(file.isDirectory()) message.setDirectory(true);
        //message.setHashId(Integer.toHexString(file.hashCode()));
        logger.debug("Message is prepared " + message.getFileName() + " and isDir" + message.isDirectory());
        return message;
    }

    public void addFile(String parent, String child, boolean isDir) {
        File parentFile = directoryCache.get(parent);
        if(parentFile == null){
            throw new IllegalArgumentException("parent should be exist");
        }
        Path newFileName = Paths.get(parentFile.getPath().toString()+File.separator+child);
        try {
            if(isDir) {
                Path newDirPath = Files.createDirectory(newFileName);
                registerWatcher(newDirPath);
                directoryCache.put(newDirPath.toFile().getName(),newDirPath.toFile());
            }
            else
                newFileName.toFile().createNewFile();

        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void deleteFile(String parent, String child) {
        File parentFile = directoryCache.get(parent);
        File childFile = directoryCache.get(child);
        if(parentFile == null){
            throw new IllegalArgumentException("parent should be exist");
        }
        try {
            if (childFile != null) {
                directoryCache.remove(child);
                Files.delete(childFile.toPath());
            } else {
                File[] listFiles = parentFile.listFiles();
                for (File cFile : listFiles) {
                    if (cFile.getName().equals(child)) {
                        cFile.delete();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void registerWatcher(Path path) {
        try {
            logger.debug("Register new watch key for " + path);
            WatchKey keyReg = path.register(watchService.getWatchService(),
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            logger.debug("Key have been registered for " + path + " and it is " + keyReg.isValid());
        } catch (IOException e) {
            logger.error("Fail with path register", e);
        }
    }

    class FileHierarchyIterator implements Iterator<File>{

        private WatchService service;
        private ConcurrentHashMap<String,File> cache;
        private Path basePath;
        private Stack<File> fileStack = new Stack<>();
        private HashSet<WatchKey> regKeySet = new HashSet<>();

        public FileHierarchyIterator(Path rootPath,
                                     WatchService watchService,
                                     ConcurrentHashMap<String,File> directoryCache) throws InvalidPathException {
            service = watchService;
            basePath = rootPath;
            cache = directoryCache;
            File currentDir = basePath.toFile();
            registerWatcherForPath(rootPath);
            if(!currentDir.isDirectory()) {
                throw new InvalidPathException(rootPath.toString(), "Root path is not a directory");
            }
            addChildsToStack(currentDir);
            logger.debug("File iterator is created");
        }

        private void addChildsToStack(File dir) {
            File[] listFiles = dir.listFiles();
            for (File cFile : listFiles)  fileStack.push(cFile);
            logger.debug("Files are added to a stack");
        }

        public void registerWatcherForPath(Path path) {
            try {
                logger.debug("Try to register watch key for " + path);
                WatchKey keyReg = path.register(service,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                regKeySet.add(keyReg);
                logger.debug("New key reg for " + path + " is " + keyReg.isValid());
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
                cache.put(nextFile.getName(),nextFile);
                addChildsToStack(nextFile);
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
