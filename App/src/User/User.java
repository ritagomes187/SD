package User;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class User implements Comparable<User> {
    private final String username;
    private final String password;
    private final boolean privileged;
    private Location location;
    private boolean infected;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock wlock = lock.writeLock();
    private final Lock rlock = lock.readLock();

    public User(String username, String password, boolean privileged) {
        this.username = username;
        this.password = password;
        this.privileged = privileged;
        this.infected = false;
        this.location = new Location();
    }

    public User(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.privileged = user.isPrivileged();
        this.infected = user.isInfected();
        this.location = user.getLocation();
    }


    public Location getLocation() {
        try {
            rlock.lock();
            return new Location(this.location);
        }
        finally {
          rlock.unlock();
        }
    }

    public String getUsername() {
        try {
            rlock.lock();
            return this.username;
        } finally {
            rlock.unlock();
        }
    }

    public String getPassword() {
        try {
            rlock.lock();
            return this.password;
        } finally {
            rlock.unlock();
        }
    }

    public void setLocation(Location location) {
        try {
            wlock.lock();
            this.location = location.clone();
        } finally {
            wlock.unlock();
        }
    }

    public boolean isPrivileged() {
        try {
            rlock.lock();
            return this.privileged;
        } finally {
            rlock.unlock();
        }
    }

    public boolean isInfected() {
        try {
            rlock.lock();
            return this.infected;
        } finally {
            rlock.unlock();
        }
    }

    public void setInfected(boolean infected) {
        try {
            wlock.lock();
            this.infected = infected;
        } finally {
            wlock.unlock();
        }
    }

    /**
     * Retorna true se o User est?? atualmente numa localizacao e false caso n??o esteja
     * A vari??vel loc serve para alcan??ar a localizacao atual que est?? na cauda
     * da lista de localizacoes do User
     * @param l localizacao
     * @return true se estiver na location, false caso contr??rio
     */
    public boolean isInLocation(Location l) {
        try {
            rlock.lock();
            boolean r = false;
            if (this.location.equals(l)) r = true;
            return r;
        }
        finally {
            rlock.unlock();
        }
    }

    /**
     * Verifica se a password corresponde ?? do User
     * @param pw password
     * @return true caso corresponda, false caso contr??rio
     */
    public boolean login(String pw) {
        try {
            rlock.lock();
            return this.password.equals(pw) && !this.infected;
        } finally {
            rlock.unlock();
        }
    }

    /**
     * Verifica se uma dada localiza????o ?? igual ?? mais atual
     * @param l localiza????o
     * @return true caso seja igual, false caso contr??rio
     */
    public boolean locEquals(Location l) {
        try {
            rlock.lock();
            return this.location.equals(l);
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public int compareTo(User user) {
        return this.username.compareTo(user.getUsername());
    }

    public User clone() {
        return new User(this);
    }
}

