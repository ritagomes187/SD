package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import User.Location;
import java.util.Map;
import java.util.TreeMap;

/**
 * Classe Server.Stub
 * Tem o propósito de criar uma conexão com o Servidor.
 * Sempre que algum User.User fizer uma query, o Server.Stub irá enviar os dados
 * necessários e, de seguida, irá receber a resposta do Server.Server e
 * retorná-la ao User.User correspondente.
 */
public class Stub {
    private final DataOutputStream dos;
    private final DataInputStream dis;
    private final Socket s;

    /**
     * Construtor por omissão da classse Stub
     */
    public Stub() {
        Socket s = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try {
            s = new Socket("localhost", 12345);
            dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.s = s;
        this.dos = dos;
        this.dis = dis;
    }

    /**
     * Registo de um User.User no Map
     * @param user username
     * @param pw password
     * @param priv true caso seja acessos privilegiados, false caso contrário
     * @return true caso o registo tenha sido bem sucedido, false caso contrário
     */
    public boolean register(String user, String pw, boolean priv) {
        boolean res = false;
        try {
            this.dos.writeUTF("register");
            this.dos.writeUTF(user);
            this.dos.writeUTF(pw);
            this.dos.writeBoolean(priv);
            this.dos.flush();
            res = this.dis.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Autenticação de um User.User
     * @param user username
     * @param pw password
     * @return true caso a autenticação seja bem sucedida, false em caso contrário
     */
    public boolean login(String user, String pw) {
        boolean r = false;
        try {
            this.dos.writeUTF("login");
            this.dos.writeUTF(user);
            this.dos.writeUTF(pw);
            this.dos.flush();
            r = this.dis.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * Usado caso o stub não tenha conseguido estabelecer conexão.
     * Destroi o socket criado.
     */
    public void killStub() {
        try {
            this.dos.writeUTF("exit");
            this.dos.flush();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Atualiza a localização de um User.User
     * @param user username
     * @param locX primeiro parâmetro da localização, sendo que uma localização
     *             corresponde a um par (x,y)
     * @param locY segundo parâmetro da localização, sendo que uma localização
     *             corresponde a um par (x,y)
     */
    public boolean changeLoc(String user, String locX, String locY) {
        boolean r = false;
        try {
            this.dos.writeUTF("change location");
            this.dos.writeUTF(user);
            this.dos.writeUTF(locX);
            this.dos.writeUTF(locY);
            this.dos.flush();
            r = this.dis.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * Calcula o número de pessoas situadas numa dada localização
     * @param locX primeiro parâmetro da localização, sendo que uma localização
     *             corresponde a um par (x,y)
     * @param locY segundo parâmetro da localização, sendo que uma localização
     *             corresponde a um par (x,y)
     * @return número de pessoas situadas numa dada localização
     */
    public int howManyInLocation(String locX, String locY) {
        int r = 0;
        try {
            this.dos.writeUTF("how many people in location");
            this.dos.writeUTF(locX);
            this.dos.writeUTF(locY);
            this.dos.flush();
            r = this.dis.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * Verifica se um certo user é privilegiado
     * @param user identificador do user em questão
     * @return true se for privilegiado
     */
    public boolean isUserPrivileged(String user) {
        boolean r = false;
        try {
            this.dos.writeUTF("is privileged");
            this.dos.writeUTF(user);
            this.dos.flush();
            r = this.dis.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
    }

    /**
     * Comunica que um user está infetado
     * @param user identificador do user
     */
    public void commInfection(String user) {
        try {
            this.dos.writeUTF("communicate infection");
            this.dos.writeUTF(user);
            this.dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Carrega o mapa de todas as localizações da grelha
     * Primeiro, começa por enviar ao seu servidor um boolean para que seja validado
     * com o objetivo de ter acesso ao mapa. Também é enviado o número de linhas e
     * colunas da grelha. De seguida, inicia-se um ciclo que começa por receber um
     * booleano que lhe indica se irá receber uma última "entrada" do mapa pretendido
     * com o par (Localização, Lista de usernames). Caso esse booleano seja true,
     * significa que será a última iteração do ciclo. Depois, recebe duas Strings que
     * correspondem aos parâmetros da localização, com o par (x,y). Recebe também,
     * associado a essa localização, o tamanho da lista de usernames irá receber,
     * relembrando que essa lista corresponde aos usernames que estão nessa localização.
     * Ao receber o tamanho, começa-se um novo ciclo que adiciona todos os usernames
     * à lista associada. Caso não haja nenhum elemento a adicionar, a lista irá
     * permanecer vazia, significando que não há ninguém nessa localização.
     * @param n número de linhas e colunas da grelha
     * @return mapa com todas as localizações e listas de usernames de cada cliente
     * associadas
     */
    public Map<Location, Collection<String>> loadMap(int n)  {
        Map<Location, Collection<String>> map = new TreeMap<>();
        try {
            this.dos.writeUTF("loadmap");
            this.dos.writeInt(n);
            this.dos.flush();
            while (dis.readBoolean()) {
                String locX = dis.readUTF();
                String locY = dis.readUTF();
                int size = dis.readInt();
                Location loc = new Location(locX, locY);
                map.put(loc, new ArrayList<>());
                for (int i = 0; i < size ; i++) {
                    String user = dis.readUTF();
                    Collection<String> list = map.get(loc);
                    list.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Pausa a thread 5 segundos para tentar reconectar
     */
    public void timeout(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executa até numa dada localização estarem 0 users (é seguro ir)
     * @param locX coordenada x da localização
     * @param locY coordenada y da localização
     */
    public void verifLoc(String locX, String locY) {
        int numPeople;
        try {
            do {
                this.dos.writeUTF("verify location");
                this.dos.writeUTF(locX);
                this.dos.writeUTF(locY);
                this.dos.flush();
                numPeople = this.dis.readInt();
                if (numPeople > 0) timeout(5);
            } while (numPeople > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Pergunta ao servidor se o user esteve em contacto com alguem infetado,
     *  em caso positivo o utilizador é notificado
     * @param user identificador do utilizador
     */
    public boolean notification(String user) {
        boolean r = false;
        try {
            this.dos.writeUTF("check notification");
            this.dos.writeUTF(user);
            this.dos.flush();
            r = this.dis.readBoolean();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
    }
}
