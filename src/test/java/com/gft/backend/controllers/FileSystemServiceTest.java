package com.gft.backend.controllers;

import com.gft.backend.configs.SpringWebConfig;
import com.gft.backend.entities.FileStateMessage;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import rx.Observable;
import rx.functions.Action1;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by miav on 2016-09-08.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader= AnnotationConfigWebContextLoader.class, classes = {SpringWebConfig.class})
@WebAppConfiguration
public class FileSystemServiceTest {

    private static final Logger logger = Logger.getLogger(WebSocketControllerTest.class);
    public static final String NAME_TEMP2 = "temp2";
    public static final String DIR_TEMP2 = "." + File.separator + NAME_TEMP2;

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        File parentDir = Paths.get(DIR_TEMP2).toFile();
        boolean created = false;
        if(!parentDir.exists()){
            created = parentDir.mkdir();
        }
        else
            created = true;
        if(created){
            String path = parentDir.getPath();
            System.out.println("Created dir with path " + path);
            try {
                File file = new File(path + "/1.txt");
                boolean newFile = file.createNewFile();
                if(newFile){
                    file = new File(path + "/second.txt");
                    file.createNewFile();
                    file = new File(path + "/second_level");
                    newFile = file.mkdir();
                    path = file.getPath();
                    if(newFile){
                        file = new File(path + "/photo.bmp");
                        file.createNewFile();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Creating failed:" + e.getMessage());
            }
        }

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @After
    public void clean(){
        Path parentDir = Paths.get(DIR_TEMP2);
        try {
            Files.walkFileTree(parentDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Deleting failed:" + e.getMessage());
        }
    }

    @Test
    public void tryToInit() throws Exception {
        FileSystemService fileSystemService = (FileSystemService) wac.getBean(FileSystemService.class);
        Observable<FileStateMessage> watcherStream = fileSystemService.getFolderWatcherStream();
        watcherStream.subscribe(new Action1<FileStateMessage>() {
            @Override
            public void call(FileStateMessage message) {
                assertTrue("Message from File system service", !message.getFileName().isEmpty());
            }
        });
        fileSystemService.getFileHierarchyBasedOnPath(Paths.get(DIR_TEMP2));
    }

    @Test
    public void tryToCreateMesage() throws Exception {
        FileSystemService fileSystemService = (FileSystemService) wac.getBean(FileSystemService.class);
        FileStateMessage message = fileSystemService.createFileStateMessage(Paths.get(DIR_TEMP2).toFile());
        assertTrue("Message from File is created", !message.getFileName().isEmpty());
        assertEquals(NAME_TEMP2,message.getFileName());
    }
}
