package dao;

import entity.Price;
import entity.User;

import javax.persistence.Query;
import java.util.List;

public class PriceDAO extends AbstractDAO<Price,Integer>{
    public Price getPrice(Integer productId) {
        try {
            String sql = "select p from Price p where p.product.id =:id order by p.value";
            Query query =entityManager.createQuery(sql);
            query.setParameter("id", productId);
            List<Price> prices = query.getResultList();
            return prices.get(0);
        }
        catch (Exception e){
            return null;
        }
    }
}
