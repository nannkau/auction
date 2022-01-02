package dao;

import entity.Product;
import entity.User;

import javax.persistence.Query;
import java.util.List;

public class ProductDAO extends AbstractDAO<Product,Integer>{
    public List<Product> getProducts() {
        try {
            String sql = "select p from Product p where p.status is not true";
            Query query =entityManager.createQuery(sql);
            return query.getResultList();
        }
        catch (Exception e){
            return null;
        }
    }
}
