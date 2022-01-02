package job;

import dao.PriceDAO;
import dao.ProductDAO;
import dao.UserDAO;
import entity.Price;
import entity.Product;
import entity.User;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import service.ServerService;

import java.util.List;

public class MyJob implements Job {
    private ProductDAO dao;
    private PriceDAO priceDAO;
    public MyJob(){
        this.priceDAO= new PriceDAO();
        this.dao= new ProductDAO();
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("start random product");
        if (ObjectUtils.isEmpty(ServerService.product)){
            List<Product> products= dao.getProducts();
            int index= (int) ((Math.random()) * ((products.size() - 1) + 1));
            ServerService.product=products.get(index);
        }
        else{
            Product product= ServerService.product;
            Price price= priceDAO.getPrice(product.getId());
            if(ObjectUtils.isEmpty(price)){
                ServerService.sendAll("phiên đấu giá thất bại");
            }
            else {
                StringBuffer message= new StringBuffer("Phiên đấu giá hiện tại đã kết thúc /n");
                message.append("sản phẩm "+product.getName()+"/n");
                message.append("với giá "+price.getValue().toString()+ " bởi "+price.getUser().getUsername());
                ServerService.sendAll(message.toString());
                product.setStatus(true);
                dao.update(product);
                UserDAO userDAO= new UserDAO();
                User user= userDAO.findByUsername(price.getUser().getUsername());
                user.setBalance(user.getBalance()-price.getValue());
                userDAO.update(user);
            }

            List<Product> products= dao.getProducts();
            int index= (int) ((Math.random()) * ((products.size() - 1) + 1));
            ServerService.product=products.get(index);
            ServerService.unblockAll();
            ServerService.sendAll("phiên đấu giá sản phẩm "+products.get(index).getName()
            +" đang bắt đầu giá khởi điểm là: "+product.getFirstPrice().toString());

        }
        System.out.println("finish random product");
    }
}
