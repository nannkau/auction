import dao.UserDAO;
import entity.User;
import service.ServerService;
import utils.HashUtil;

public class Main {
    public static void main(String[] args) {
        new ServerService().start(8080);
    }
}
