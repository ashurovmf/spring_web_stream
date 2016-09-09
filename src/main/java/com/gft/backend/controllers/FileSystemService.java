package com.gft.backend.controllers;

import com.gft.backend.entities.FileStateMessage;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Created by miav on 2016-09-06.
 */
@Service
public class FileSystemService {

    private static final Logger logger = Logger.getLogger(FileSystemService.class);

    private volatile WatchService watchService = null;

    private volatile PublishSubject<FileStateMessage> watchStream = PublishSubject.create();

    public FileSystemService(){
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        WatchKey watchKey;
                        try {
                            watchKey = watchService.take();
                            for (WatchEvent event : watchKey.pollEvents()) {
                                final WatchEvent.Kind kind = event.kind();
                                final Path changed = (Path) event.context();
                                FileStateMessage message = FileSystemService.this.createFileStateMessage(changed.toFile());
                                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                    message.setState(FileStateMessage.STATE_ADDED);
                                }
                                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    message.setState(FileStateMessage.STATE_MODIFIED);
                                }
                                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                    message.setState(FileStateMessage.STATE_DELETED);
                                }
                                watchStream.onNext(message);
                            }
                            boolean valid = watchKey.reset();
                            if (!valid) {
                                logger.error("Watch key has been unregistered");
                            }
                        } catch (InterruptedException e) {
                            logger.error(e);
                            return;
                        }
                    }
                }
            });
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public Observable<FileStateMessage> getFolderWatcherStream() {
        return watchStream;
    }

    public Observable<FileStateMessage> getFileHierarchyBasedOnPath(Path basePath){
        FileHierarchyIterator iterator = new FileHierarchyIterator(basePath, watchService);
        while (iterator.hasNext()){
            File file = iterator.next();
            FileStateMessage message = createFileStateMessage(file);
            message.setState(FileStateMessage.STATE_ADDED);
            watchStream.onNext(message);
        }
        return watchStream;
    }

    public FileStateMessage createFileStateMessage(File file) {
        FileStateMessage message = new FileStateMessage();
        message.setFileName(file.getName());
        logger.debug("### File sep:"+File.separator+" len:"+File.separator.length());
        message.setParent(file.getParent().split(Pattern.quote(File.separator)));
        if(file.isDirectory()) message.setDirectory(true);
        message.setHashId(FileStateMessage.getMD5Hash(
                message.getParent()+message.getFileName()));
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
            } catch (IOException e) {
                logger.error(e);
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
    }
}
