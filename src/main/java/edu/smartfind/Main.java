package edu.smartfind;
import edu.smartfind.web.SmartFindServer;
public class Main {
    public static void main(String[] args) throws Exception {
        int port=Integer.parseInt(System.getenv().getOrDefault("PORT","8080"));
        SmartFindServer server=new SmartFindServer(port);
        server.start();
        System.out.println("SmartFind is running at http://localhost:"+port);
    }
}
