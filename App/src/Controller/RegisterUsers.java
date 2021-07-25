package Controller;

import User.User;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Classe RegisterUsers
 * Armazena o conjunto de utilizadores que já estiveram na mesma localização
 * de qualquer utilizador. Se um utilizador ainda não partilhou a sua
 * localização com nenhum outro, a lista de utilizadores que lhe está associada
 * é vazia.
 */
public class RegisterUsers {
    private final Map<String, Set<User>> map;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rlock = lock.readLock();
    private final Lock wlock = lock.writeLock();

    /**
     * Construtor por omissão da classe RegisterUsers
     */
    public RegisterUsers() {
        this.map = new HashMap<>();
    }

    /**
     * Método que cria uma entrada para o mapa, ou seja, adiciona um novo
     */
    public void createEntry(String user) {
        try {
            wlock.lock();
            this.map.put(user, new TreeSet<>());
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Retorna a lista de todos os User que já entraram em contacto
     * com um determinado User
     * @param user username de um determinado User
     * @return lista de usernames que já entraram em contacto com o user
     */
    public Collection<User> getListUser(String user) {
        try {
            rlock.lock();
            return this.map.get(user);
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Quando um User muda de localização, poderá entrar em contacto com
     * novos Users, e por isso é necessário que todos esses registos sejam
     * atualizados. Por iss, cada username é adicionado nos registos de
     * todos os outros usernames. Por exemplo, numa lista [A,B,C,D],
     * o username A seria adicionado ao username B,C e D; e o mesmo processo
     * aconteceria para os restantes elementos da lista
     * @param list lista de Users que estão numa certa localização
     */
    public void createNewRegisters(Collection<User> list) {
        try {
            wlock.lock();
            for (User u : list) {
                Set<User> set = this.map.get(u.getUsername());
                //set = set.stream().filter(user -> !user.equals(u)).map(User::clone).map(User::clone).collect(Collectors.toSet());
                for (User user : list)
                    if (!user.equals(u)) set.add(user);
            }
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Procura se há pelo menos um user infetado numa collection de users
     * @param users collection onde se vai procurar o infetado
     * @return true se houver pelo menos um user infetado
     */
    public boolean hasInfected(Collection<User> users) {
        boolean r = false;
        for(User u : users) {
            if (u != null && u.isInfected()) {
                users.remove(u);
                r = true;
            }
        }
        return r;
    }
}