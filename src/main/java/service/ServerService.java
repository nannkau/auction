package service;

import dao.UserDAO;
import dto.Request;
import dto.Response;
import dto.UserForm;
import entity.User;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import utils.HashUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerService extends Thread {

    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clientHandlers;
    private Map<String,User> userOnline;

    public ServerService() {
        this.clientHandlers = new HashMap<>();
        this.userOnline = new HashMap<>();
    }

    public void start(int port) {
        System.out.println("Server starting!!!");
        try {
            serverSocket = new ServerSocket(port);
            System.out.println(serverSocket.getInetAddress().getHostName());
            System.out.println(serverSocket.getLocalPort());
            while (true) {
                ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
                clientHandler.start();
                this.clientHandlers.put(clientHandler.getUid(), clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getUserIdOnline() {
        return this.clientHandlers.values().stream()
                .map(ClientHandler::getUid)
                .collect(Collectors.toList());
    }


    @Getter
    @Setter
    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String uid;
        private String username;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            this.uid = UUID.randomUUID().toString();
        }

        private void response(Response response) throws IOException {
            this.out.writeObject(response);
            this.out.flush();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Object input = in.readObject();
                    if (ObjectUtils.isNotEmpty(input)) {
                        Request request = (Request) input;
                        switch (request.getAction()) {
                            case CONNECT:
                            {
                                Response response= new Response();
                                response.setData("connected to server");
                                response.setStatusCode(200);
                                this.response(response);
                            }
                            break;
                            case LOGIN: {
                                UserDAO userDAO= new UserDAO();
                                UserForm userForm= (UserForm) request.getData();
                                User result=userDAO.findByUsernameAndPassword(userForm.getUsername(), HashUtil.hashPassword(userForm.getPassword()));
                                if (ObjectUtils.isNotEmpty(result)){
                                    userOnline.put(uid,result);
                                    Response response= new Response();
                                    response.setData("login success");
                                    response.setStatusCode(200);
                                    this.response(response);
                                }
                                else{
                                    Response response= new Response();
                                    response.setData("invalid username or password");
                                    response.setStatusCode(403);
                                    this.response(response);
                                    clientHandlers.remove(this.getUid());
                                }
                                break;
                            }
//                            case SEND_MESSAGE_TO_USER_SPECIFIC: {
//                                ClientHandler clientHandler = clientHandlers.get(((MessageRequest) (request)).getUid());
//                                if (clientHandler == null) {
//                                    this.response(MessageResponse.builder()
//                                            .statusCode(StatusCode.BAD_REQUEST)
//                                            .build());
//                                } else {
//                                    clientHandler.response(MessageResponse.builder()
//                                            .message(((MessageRequest) (request)).getMessage())
//                                            .senderId(this.getUid())
//                                            .statusCode(StatusCode.OK)
//                                            .build());
//                                }
//                                break;
//                            }
//                            case CHAT_ALL: {
//                                GroupMessageRequest groupMessageRequest = (GroupMessageRequest) request;
//                                for (String s : groupMessageRequest.getUids()) {
//                                    ClientHandler clientHandler = clientHandlers.get(s);
//                                    if (clientHandler != null) {
//                                        clientHandler.response(MessageResponse.builder()
//                                                .senderId(this.getUid())
//                                                .message(groupMessageRequest.getMessage())
//                                                .statusCode(StatusCode.OK)
//                                                .build());
//                                    }
//                                }
//                                break;
//                            }
                            case DISCONNECT: {
                                clientHandlers.remove(this.getUid());
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }

                    if (out != null) {
                        out.close();
                    }

                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
