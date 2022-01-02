package service;

import dao.PriceDAO;
import dao.UserDAO;
import dto.OfferForm;
import dto.Request;
import dto.Response;
import dto.UserForm;
import entity.Price;
import entity.Product;
import entity.User;
import job.MyJob;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import utils.HashUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServerService extends Thread {

    private ServerSocket serverSocket;
    private static Map<String, ClientHandler> clientHandlers;
    public static Map<String,Boolean> userOnline;
    public static Product product;

    public ServerService() {
        this.clientHandlers = new HashMap<>();
        this.userOnline = new HashMap<>();
        startJob();
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
                this.clientHandlers.put(clientHandler.getUsername(), clientHandler);
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

    public static void unblockAll(){
        if (userOnline.size()<1){
            System.out.println("no client connect to server");
            return;
        }
        Map<String,Boolean> tmp=userOnline;
        for (Map.Entry<String, Boolean> user : tmp.entrySet()) {
            userOnline.put(user.getKey(), false);

        }
    }
    public static void sendAll(String message) {
        if (clientHandlers.size()<1){
            System.out.println("no client connect to server");
            return;
        }
        clientHandlers.entrySet().forEach(p -> {
            Response response= new Response();
            response.setData(message);
            response.setStatusCode(200);
            try {
                p.getValue().response(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private void startJob(){
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("triggerName", "group1")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(60).repeatForever()).build();

        JobDetail job = JobBuilder.newJob(MyJob.class)
                .withIdentity("jobName", "group1").build();
        Scheduler scheduler = null;
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }
    @Getter
    @Setter
    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private String username;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
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
//                            case CONNECT:
//                            {
//                                Response response= new Response();
//                                response.setData("connected to server");
//                                response.setStatusCode(200);
//                                this.response(response);
//                            }
//                            break;
                            case LOGIN: {
                                UserDAO userDAO= new UserDAO();
                                UserForm userForm= (UserForm) request.getData();
                                this.username = userForm.getUsername();
                                User result=userDAO.findByUsernameAndPassword(userForm.getUsername(), HashUtil.hashPassword(userForm.getPassword()));
                                if (ObjectUtils.isNotEmpty(result)||!(userOnline.get(this.username))){
                                    userOnline.put(this.username,false);
                                    Response response= new Response();
                                    response.setData("đăng nhập thành công /n số tiền bạn hiện có là: "
                                            + result.getBalance().toString()+"/n Sản phẩm đang đấu giá là:"+product.getName()
                                            +"với giá khởi điểm là "+product.getFirstPrice().toString());
                                    response.setStatusCode(200);
                                    this.response(response);
                                }
                                else{
                                    Response response= new Response();
                                    response.setData("không thể đăng nhập");
                                    response.setStatusCode(403);
                                    this.response(response);
                                    clientHandlers.remove(this.username);
                                }
                                break;
                            }
                            case OFFER: {
                                OfferForm offer = (OfferForm) request.getData();
                                UserDAO userDAO= new UserDAO();
                                User user=userDAO.findByUsername(this.username);
                                if (offer.getPrice()>user.getBalance() ||user.getBalance()<product.getFirstPrice()){
                                    Response response= new Response();
                                    response.setData("giá không hợp lệ");
                                    response.setStatusCode(400);
                                    this.response(response);
                                }
                                else {
                                    PriceDAO priceDAO= new PriceDAO();
                                    Price maxPrice= priceDAO.getPrice(product.getId());
                                    if (ObjectUtils.isEmpty(maxPrice)||maxPrice.getValue()<offer.getPrice()){
                                        unblockAll();
                                        User maxPriceUser= userDAO.findByUsername(this.username);
                                        maxPriceUser.setBlock(true);
                                        userOnline.put(this.username,true);
                                    }
                                    Price price= new Price();
                                    price.setUser(userDAO.findByUsername(this.username));
                                    price.setValue(offer.getPrice());
                                    price.setProduct(product);
                                    price.setTimeCreated(new Date());

                                    priceDAO.create(price);
                                    Response response= new Response();
                                    response.setData("mức giá của bạn đã được ghi nhận");
                                    response.setStatusCode(200);
                                    this.response(response);
                                }
                                break;
                            }
//
                            case DISCONNECT: {
                                clientHandlers.remove(this.username);
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
