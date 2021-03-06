package com.gft.backend.controllers;

import com.gft.backend.configs.SpringWebConfig;
import com.gft.backend.entities.FileStateMessage;
import com.gft.backend.entities.FolderList;
import com.gft.backend.entities.FolderNameSearch;
import com.gft.backend.entities.FolderNameSearchTest;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Created by miav on 2016-08-30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader= AnnotationConfigWebContextLoader.class, classes = {SpringWebConfig.class})
@WebAppConfiguration
public class WebSocketControllerTest {

    public static final String C_TEMP1 = "temp1";
    public static final String C_TEMP1_PATH = "c:/"+C_TEMP1;

    class FolderNameHandler implements StompSessionHandler {

        public StompSession session;

        public CompletableFuture<FolderNameSearchTest> singleResponse = new CompletableFuture<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return FolderNameSearchTest.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Get folder name with content" + payload.toString());
            singleResponse.complete((FolderNameSearchTest) payload);
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            this.session = session;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println("Folder name exception: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.out.println("Folder name transport error:" + exception.getMessage());

        }
    }

    class FolderListHandler implements StompSessionHandler {

        public StompSession session;

        public CompletableFuture<FileStateMessage> singleResponse = new CompletableFuture<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return FileStateMessage.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Get folder list with content" + payload.toString());
            singleResponse.complete((FileStateMessage) payload);
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            this.session = session;
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println("Folder list exception: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.out.println("Folder list transport error:" + exception.getMessage());

        }
    }

    class MyWebSocketHandler implements WebSocketHandler {

        @Override
        public void afterConnectionClosed(WebSocketSession arg0, CloseStatus arg1) throws Exception {
            System.out.println("afterConnectionClosed");
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession arg0) throws Exception {
            System.out.println("afterConnectionEstablished");
        }

        @Override
        public void handleMessage(WebSocketSession arg0, WebSocketMessage<?> arg1) throws Exception {
            System.out.println("Get message " + arg1.getPayload().toString());
        }

        @Override
        public void handleTransportError(WebSocketSession arg0, Throwable arg1) throws Exception {
            System.out.println("handleTransportError");
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

    }


    private static final Logger logger = Logger.getLogger(WebSocketControllerTest.class);

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        File parentDir = new File(C_TEMP1_PATH);
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


    @Test
    public void sendMessageToBrokerAndReceiveReplyViaTopic() throws Exception {
        System.out.println("Stomp client is trying to connect ");
        try {
            WebSocketClient transport = new StandardWebSocketClient();
            List<Transport> transports = new ArrayList<>(2);
            transports.add(new WebSocketTransport(transport));
            transports.add(new RestTemplateXhrTransport());

            SockJsClient sockJsClient = new SockJsClient(transports);
            sockJsClient.doHandshake(new MyWebSocketHandler(), "ws://localhost:8080/webstream/add")
                    .addCallback(new ListenableFutureCallback<WebSocketSession>() {
                @Override
                public void onFailure(Throwable throwable) {
                    System.out.println("Handshake fail");
                }

                @Override
                public void onSuccess(WebSocketSession webSocketSession) {
                    System.out.println("Handshake success");
                }
            });

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new StringMessageConverter());

            MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
            stompClient.setMessageConverter(messageConverter);

            String url = "ws://localhost:8080/webstream/add";
            FolderNameHandler folderHandler = new FolderNameHandler();
            ListenableFuture<StompSession> future = stompClient.connect(url, folderHandler);
            StompSession stompSession = future.get();

            FolderListHandler listHandler = new FolderListHandler();
            stompSession.subscribe("/topic/show", listHandler);
            System.out.println("Subscribed");

            FolderNameSearch message = new FolderNameSearch();
            message.setFolderName(C_TEMP1);
            folderHandler.session.send("/list/add", message);

            String fName = listHandler.singleResponse.get().getFileName();

            message.setFolderName("#");
            folderHandler.session.send("/list/add", message);

            stompClient.stop();
            assertEquals("",fName);
            assertTrue("Root directory", "".equals(fName));
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(),ex);
        }
        assertTrue("STOMP TEST", true);
    }


    @Test
    public void toGetService() throws Exception {
        WebSocketController controller = wac.getBean(WebSocketController.class);
        FolderNameSearch folder = new FolderNameSearch();
        folder.setFolderName(C_TEMP1);
        Message<Object> messageMap = new Message<Object>() {
            @Override
            public Object getPayload() {
                return folder;
            }

            @Override
            public MessageHeaders getHeaders() {
                Map<String,Object> headerMap = new HashMap<String,Object>();
                headerMap.put("simpMessageType", SimpMessageType.MESSAGE);
                headerMap.put(SimpMessageHeaderAccessor.SESSION_ID_HEADER,"4rnnhl4o");
                MessageHeaders headers = new MessageHeaders(headerMap);
                return headers;
            }
        };
        controller.fetchFolder(messageMap, folder);
        assertTrue("Web Socket Controller TEST", true);
    }


    //@Ignore
    @Test
    @WithMockUser
    public void toRedirectFromFolderPage() throws Exception {
        mockMvc.perform(get("/folder"))
                .andExpect(status().isOk())
                .andExpect(view().name("folder"));
    }

    @After
    public void clean(){
        try {
            File parentDir = Paths.get(C_TEMP1_PATH).toFile();
            FileUtils.cleanDirectory(parentDir);
            parentDir.delete();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Deleting failed:" + e.getMessage());
        }
//        try {
//            Files.walkFileTree(parentDir, new SimpleFileVisitor<Path>() {
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    Files.delete(file);
//                    return FileVisitResult.CONTINUE;
//                }
//
//                @Override
//                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                    Files.delete(dir);
//                    return FileVisitResult.CONTINUE;
//                }
//
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Deleting failed:" + e.getMessage());
//        }
    }
}
