package Server;

import Controller.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe Servidor
 * Nesta classe, é criado um Map que visa guardar classes como o Controller.Controller que têm
 * o conjunto de todos os Users e, caso seja necessário criar uma nova classe deste
 * género mas com outras aplicabilidades, apenas será necessário inserir um novo
 * Controller.Controller no map, não havendo alterações significativas no código.
 */
public class Server {
    public static void main(String[] args) throws Exception {
        Skeleton skeleton = new ControllerSkeleton(new UsersController(), new RegisterUsers());
        var ss = new ServerSocket(12345);
        while(true) {
            System.out.println("À espera de conexão");
            Socket s = ss.accept();

            Runnable user = () -> {
                try (s) {
                    var dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
                    var dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
                    skeleton.handle(dis,dos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            new Thread(user).start();
        }
    }
}
