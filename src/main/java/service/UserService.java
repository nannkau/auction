package service;

import dao.ProductDAO;
import dao.UserDAO;
import entity.Product;
import entity.User;
import utils.HashUtil;

import java.util.Date;

public class UserService {
    public static void main(String[] args) {
//        UserDAO userDAO = new UserDAO();
//        User user= new User();
//        user.setUsername("huong");
//        user.setPassword(HashUtil.hashPassword("123456"));
//        user.setBalance(100000);
//        user.setBlock(false);
//        User create=userDAO.create(user);
//        System.out.println(create.getUsername());
        Product product= new Product();
        product.setName("dao hai nghìn năm");
        product.setStatus(false);
        product.setFirstPrice(3500);
        product.setTimeCreated(new Date());
        ProductDAO productDAO= new ProductDAO();
        productDAO.create(product);
        System.out.println("success");

    }
}
