package Controller;

import User.User;
import User.Location;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Classe Controller
 * Pretende guardar o conjunto de utilizadores normais da aplicação.
 * Dispõe de um Map para esse efeito e, para além disso, de um ReentrantLock
 * caso seja necessário proceder a alterações no Map de forma concorrente.
 */
public class UsersController {
    private final Map<String, User> mapUsers;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rlock = lock.readLock();
    private final Lock wlock = lock.writeLock();

    /**
     * Construtor por omissão da classe Controller
     */
    public UsersController() {
        this.mapUsers = new TreeMap<>();
    }

    /**
     * Verifica se o utilizador já existe no map
     * @param user username
     * @return true caso o utilizador já exista, false caso contrário
     */
    public boolean existsUser(String user) {
        try {
            rlock.lock();
            return this.mapUsers.get(user) != null;
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Regista um User no Map apenas se o username não seja igual
     * ao de outro User
     * @param name username
     * @param pw password
     */
    public boolean register(String name, String pw, boolean privileged) {
        try {
            wlock.lock();
            boolean success = false;
            User user = new User(name, pw, privileged);
            if (!existsUser(name)) {
                this.mapUsers.put(name, user);
                success = true;
            }
            return success;
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Autenticação de um User
     * @param name username
     * @param pw password
     * @return true caso as credenciais correspondam, false caso contrário
     */
    public boolean login(String name, String pw) {
        try {
            rlock.lock();
            boolean r = false;
            User user = this.mapUsers.get(name);
            if (user != null) r = user.login(pw);
            return r;
        } finally {
            rlock.unlock();
        }

    }

    /**
     * Procura no mapa de users os users que estão numa certa localização
     * @param loc localizacao a procurar
     * @return uma collection com os identificadores dos users que estão na localização dada
     */
    public Collection<User> getNewRegUsers(Location loc) {
        try {
            rlock.lock();
            Collection<User> list = new ArrayList<>();
            for (Map.Entry<String, User> entry : mapUsers.entrySet())
                if (entry.getValue().locEquals(loc)) list.add(entry.getValue());
            return list;
        } finally {
           rlock.unlock();
        }
    }

    /**
     * Adiciona uma localizacao à lista de localizacoes
     * Usa um readLock porque só irá escrever no User e não no Map
     * @param username identificador
     * @param l localizacao
     */
    public boolean setLocalizacao(String username, Location l, int n) {
        try {
            wlock.lock();
            boolean success = false;
            User user = this.mapUsers.get(username);
            if (user != null && l.isInLimit(n)) {
                user.setLocation(l);
                success = true;
            }
            return success;
        } finally {
            wlock.unlock();
        }

    }

    /**
     * Retorna o número de Users numa localizacao
     * @param loc localizacao
     * @return número de users
     */
    public int getNumberInLoc(Location loc) {
        try {
            rlock.lock();
            int r = 0;
            for (Map.Entry<String, User> entry : this.mapUsers.entrySet()) {
                User user = entry.getValue();
                if (user.isInLocation(loc)) r++;
            }
            return r;
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Verifica se um determinadao user é privilegiado
     * @param user identificador do user em questão
     * @return true se o user for privilegiado
     */
    public boolean isUserPrivileged(String user) {
        try {
            rlock.lock();
            User u = this.mapUsers.get(user);
            return u != null && u.isPrivileged();
        } finally {
            rlock.unlock();
        }

    }

    /**
     * Altera o estado de um cliente para afetado
     * @param user identificador do user em questão
     */
    public void commInfection(String user) {
        try {
            rlock.lock();
            User u = this.mapUsers.get(user);
            if (u != null) {
                u.setInfected(true);
                u.setLocation(new Location());
            }
        } finally {
            rlock.unlock();
        }

    }



    /**
     * Envia o mapa de todas as localizações da grelha
     * Começa por preencher o mapa com todas as localizações
     * De seguida, percorre a lista de Users e verifica a localização
     * mais atual de cada um. Com base nesse resultado, insere o username
     * na respetiva localização (chave) do Mapa.
     * @return Mapa em que a chave é a localização e o valor corresponde
     * a uma lista de Users que lá se encontram
     * @param n número de linhas e colunas da grelha
     */
    public Map<Location, Collection<String>> loadMap(int n) {
        try {
            rlock.lock();
            Map<Location, Collection<String>> map = new TreeMap<>();
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    map.put(new Location(String.valueOf(i), String.valueOf(j)), new ArrayList<>());
            for (Map.Entry<String, User> entry : this.mapUsers.entrySet()) {
                String username = entry.getKey();
                Location loc = entry.getValue().getLocation();
                Collection<String> usersLoc = map.get(loc);
                if (usersLoc != null) usersLoc.add(username);
            }
            return map;
        } finally {
            rlock.unlock();
        }
    }
}