package dao;

import entity.User;

import javax.persistence.Query;

public class UserDAO extends AbstractDAO<User,Integer>{
    public User findByUsernameAndPassword(String username, String password){
        String sql = "select u from User u where u.username =:username and u.password =:password";
        Query query =entityManager.createQuery(sql);
        query.setParameter("username", username);
        query.setParameter("password", password);
        return (User) query.getSingleResult();
    }
}
