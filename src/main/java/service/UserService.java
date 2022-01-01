package service;

import dao.UserDAO;
import entity.User;
import utils.HashUtil;

public class UserService {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        User user= new User();
        user.setUsername("admin");
        user.setPassword(HashUtil.hashPassword("123456"));
        user.setBalance(100000);
        user.setBlock(false);
        User create=userDAO.create(user);
        System.out.println(create.getUsername());

    }
}
