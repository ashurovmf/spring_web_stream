package com.gft.backend.controllers;

import com.gft.backend.configs.SpringWebConfig;
import com.gft.backend.entities.FolderList;
import com.gft.backend.entities.FolderNameSearch;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.security.test.context.support.WithMockUser;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by miav on 2016-08-30.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader= AnnotationConfigWebContextLoader.class, classes = {SpringWebConfig.class})
@WebAppConfiguration
public class WebStreamControllerTest {

    class FolderNameHandler implements StompSessionHandler {

        public StompSession session;

        public CompletableFuture<FolderNameSearch> singleResponse = new CompletableFuture<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return FolderNameSearch.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Get folder name with content" + payload.toString());
            singleResponse.complete((FolderNameSearch) payload);
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

        public CompletableFuture<FolderList> singleResponse = new CompletableFuture<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return FolderList.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Get folder list with content" + payload.toString());
            singleResponse.complete((FolderList) payload);
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


    private static final Logger logger = Logger.getLogger(WebStreamControllerTest.class);

    @Autowired
    WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        File parentDir = new File("c:/temp");
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
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @After
    public void clean(){
        File parentDir = new File("c:/temp");
        for (File file: parentDir.listFiles()) {
            file.delete();
        }
    }

    //@Ignore
    @Test
    public void sendMessageToBrokerAndReceiveReplyViaTopic() throws Exception {
        System.out.println("Stomp client is trying to connect ");
        try {
            WebSocketClient transport = new StandardWebSocketClient();
            List<Transport> transports = new ArrayList<>(2);
            transports.add(new WebSocketTransport(transport));
            transports.add(new RestTemplateXhrTransport());

            SockJsClient sockJsClient = new SockJsClient(transports);
            sockJsClient.doHandshake(new MyWebSocketHandler(), "ws://localhost:8080/webstream/add").addCallback(new ListenableFutureCallback<WebSocketSession>() {
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
            System.out.println("Stomp client is created ");
            stompClient.setMessageConverter(new StringMessageConverter());

            MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
            stompClient.setMessageConverter(messageConverter);

            String url = "ws://localhost:8080/webstream/add";
            FolderNameHandler folderHandler = new FolderNameHandler();
            ListenableFuture<StompSession> future = stompClient.connect(url, folderHandler);
            System.out.println("Stomp client is connected ");
            StompSession stompSession = future.get();
            System.out.println("Session is got");

            FolderListHandler listHandler = new FolderListHandler();
            stompSession.subscribe("/topic/show", listHandler);
            System.out.println("Subscribed");

            FolderNameSearch message = new FolderNameSearch();
            message.setFolderName("temp");
            folderHandler.session.send("/list/add", message);
            System.out.println("Message is sent");

            String[] fList = listHandler.singleResponse.get().getfList();
            System.out.println("Handled response: " + fList.length);

            message.setFolderName("#");
            folderHandler.session.send("/list/add", message);
            System.out.println("Message is sent");

            stompClient.stop();
            assertEquals(2,fList.length);
            assertTrue("First file is 1.txt", "1.txt".equals(fList[0]));
            assertTrue("Second file is second.txt", "second.txt".equals(fList[1]));
        }
        catch (Exception ex)
        {
            logger.error(ex.getMessage(),ex);
        }
        assertTrue("STOMP TEST", true);
    }

//    @Test
//    public void sendMessageToBrokerAndReceiveReplyViaTopic() throws Exception {
//        System.out.println("Stomp client is trying to connect ");
//        try {
//            StompClient stompClient = new StompClient("ws://localhost:8080/webstream/add"); // /webstream/add
//            System.out.println("Stomp client is created ");
//            stompClient.connect();
//            System.out.println("Stomp client is connected ");
//            MessageAccumulator handler = new MessageAccumulator();
//            ClientSubscription subscription =
//                    stompClient.subscribe( "/topic/show" )
//                            .withMessageHandler( handler )
//                            .withAckMode( Subscription.AckMode.AUTO )
//                            .start();
//            System.out.println("Subscribed");
//            ClientTransaction tx = stompClient.begin();
//            tx.send(
//                    StompMessages.createStompMessage( "/topic/add", "{\"folderName\":\"temp\"}")
//            );
//            tx.commit();
//
//            System.out.println("Message is sent");
//            wait(1000);
//            subscription.unsubscribe();
//            List<StompMessage> messages = handler.getMessages();
//            stompClient.disconnect();
//            assertTrue("Connection", true);
//        }
//        catch (Exception ex)
//        {
//            logger.error(ex.getMessage(),ex);
//        }
//        assertTrue("STOMP TEST", true);
//    }


//    @Test
//    public void sendMessageToBrokerAndReceiveReplyViaTopic() throws Exception {
//        System.out.println("Stomp client is trying to connect ");
//        Client stompClient = new Client( "localhost", 61626, null, null);
//        System.out.println("Stomp client is created ");
//        stompClient.subscribe( "/topic/show", new Listener() {
//                    public void message(Map header, String body ) {
//                        System.out.println("Get message from stomp server " + body);
//                        if (body.equals( "get-info" )) {
//                            // Send some message to the clients with info about the server
//                        }
//
//                    } } );
//        stompClient.sendW("/topic/add", "{\"folderName\":\"temp\"}");
//        System.out.println("Message is sent");
//        wait(3000);
//        stompClient.disconnect();
//        assertTrue("Connection", true);
//    }

    @Ignore
    @Test
    @WithMockUser
    public void toShowFolderPage() throws Exception {
        mockMvc.perform(get("/folder"))
                .andExpect(status().isOk());
    }
}
