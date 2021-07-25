package Controller;

import User.Location;
import User.User;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * A classe ControllerSkeleton serve como um middleware que decide a qual dos
 * "Controllers" recorrer, dependendo da query que está a ser executada.
 * Neste momento, como só foi implementado um Controller, a escolha desta classe será
 * simples, mas, eventualmente, será necessário criar novos Controllers que armazenam
 * diferentes objetos e aí irá ser posta em prática a utilidade desta classe.
 * Por isso, esta classe está preparada para uma eventual expansão de código.
 */
public class ControllerSkeleton implements Skeleton {
    private final UsersController userscontroller;
    private final RegisterUsers regUsers;

    /**
     * Método parametrizado da classe ControllerSkeleton
     */
    public ControllerSkeleton(UsersController controller, RegisterUsers regUsers) {
        this.userscontroller = controller;
        this.regUsers = regUsers;
    }

    @Override
    public void handle(DataInputStream dis, DataOutputStream dos) throws Exception {
        int limit = 5;
        boolean cont = true;
        while (cont) {
            switch (dis.readUTF()) {
                case "register" -> {
                    String name = dis.readUTF();
                    String pw = dis.readUTF();
                    boolean privileged = dis.readBoolean();
                    boolean suc = this.userscontroller.register(name, pw, privileged);
                    if (suc) this.regUsers.createEntry(name);
                    dos.writeBoolean(suc);
                    dos.flush();
                }
                case "login" -> {
                    String username = dis.readUTF();
                    String password = dis.readUTF();
                    boolean success = this.userscontroller.login(username, password);
                    dos.writeBoolean(success);
                    dos.flush();
                }
                case "change location" -> {
                    String n = dis.readUTF();
                    String locX = dis.readUTF();
                    String locY = dis.readUTF();
                    Location loc = new Location(locX, locY);
                    boolean r = this.userscontroller.setLocalizacao(n, loc, limit);
                    if (r) {
                        Collection<User> registo = this.userscontroller.getNewRegUsers(loc);
                        this.regUsers.createNewRegisters(registo);
                    }
                    dos.writeBoolean(r);
                    dos.flush();
                }
                case "how many people in location" -> {
                    String x = dis.readUTF();
                    String y = dis.readUTF();
                    Location l = new Location(x,y);
                    int number = -1;
                    if (l.isInLimit(limit)) number = this.userscontroller.getNumberInLoc(new Location(x, y));
                    dos.writeInt(number); //-1 caso a localização seja inválida, >0 caso contrário
                    dos.flush();
                }
                case "communicate infection" -> {//para além de comunicar, avisar todos os users que já tiveram na loc do User
                     String user = dis.readUTF();
                     this.userscontroller.commInfection(user);
                }
                case "loadmap" -> {
                    int num = dis.readInt();
                    Map<Location, Collection<String>> map = this.userscontroller.loadMap(num);
                    for (Map.Entry<Location, Collection<String>> entry : map.entrySet()) {
                        dos.writeBoolean(true);
                        dos.writeUTF(entry.getKey().getCoordX());
                        dos.writeUTF(entry.getKey().getCoordY());
                        dos.writeInt(entry.getValue().size());
                        for (String user : entry.getValue())
                            dos.writeUTF(user);
                    }
                    dos.writeBoolean(false);
                    dos.flush();
                }
                case "is privileged" -> {
                    String user = dis.readUTF();
                    dos.writeBoolean(this.userscontroller.isUserPrivileged(user));
                    dos.flush();
                }
                case "verify location" -> {
                    String locX = dis.readUTF(), locY = dis.readUTF();
                    dos.writeInt(this.userscontroller.getNumberInLoc(new Location(locX, locY)));
                    dos.flush();
                }
                case "check notification" -> {
                    String user = dis.readUTF();
                    Collection<User> contacts = this.regUsers.getListUser(user);
                    dos.writeBoolean(this.regUsers.hasInfected(contacts));
                    dos.flush();
                }
                case "exit" -> cont = false;
            }
        }
    }
}