package Client;

import Server.Stub;
import User.Location;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;

public class Prompt {
    private final Stub stub;

    /**
     * Construtor parametrizado da classe Prompt
     */
    public Prompt(Stub stub) {
        this.stub = stub;
    }

    /**
     * Display do menu de logout
     */
    public void exit () {
        System.out.println("Desligando aplicação...");
        this.stub.killStub();
    }

    /**
     * Dislay de um menu para um utilizador se registar
     */
    public void registar() {
        String user, pw, answer;
        boolean ret, privileged;
        Scanner s = new Scanner(System.in);
        System.out.print("Introduza o seu username: ");
        user = s.nextLine();
        System.out.print("Introduza a password: ");
        pw = s.nextLine();
        do {
            System.out.print("É um utilizador Premium? (s/n): ");
            answer = s.nextLine();
        } while (!answer.equals("s") && !answer.equals("n"));
        privileged = answer.equals("s");
        ret = stub.register(user, pw, privileged);
        if (ret) System.out.println("Registo completo!");
        else System.out.println("Ups! Esse username já existe...");
    }

    /**
     * Display do menu de login
     * @return true caso tenha havido sucesso, false caso contrário
     */
    public String login() {
        String pw, user;
        Scanner s = new Scanner(System.in);
        System.out.print("Introduza o utilizador: ");
        user = s.nextLine();
        System.out.print("Introduza a password: ");
        pw = s.nextLine();
        if (stub.login(user, pw))
            System.out.println("Autenticação bem sucedida!");
        else {
            System.out.println("Autenticação inválida");
            user = null;
        }
        return user;
    }

    /**
     * Um User sai da sua conta de utilizador
     * @param user String com o username atualmente conectado. Nulo caso não esteja ninguem conectado
     */
    public void logout(String user) {
        if (user != null)
            System.out.println("Terminando sessão...");
        else
            System.out.println("De momento, não se encontra autenticado qualquer utilizador...");
    }

    /**
     * Verifica se uma String corresponde a um inteiro
     * @param s String
     * @return true caso s corresponda não a um inteiro,
     * false caso contrário
     */
    public boolean isNotInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return true;
        }
        return false;
    }

    /**
     * Atualiza a localização de um User
     * @param user username
     */
    public void changeLoc(String user) {
        Scanner s = new Scanner(System.in);
        System.out.println("Para que localização se pretende deslocar? (Responda p.ex: 1 1)");
        String[] loc = s.nextLine().split(" ");
        if (!(isNotInteger(loc[0]) || isNotInteger(loc[1]) || loc[0] == null || loc[1] == null)) {
            if (this.stub.changeLoc(user, loc[0], loc[1]))
                System.out.println("Atualização bem sucedida!");
            else System.out.println("Essa localização não está dentro dos limites");
        } else System.out.println("Os valores da localização são 2 e têm de ser inteiros...");
    }

    /**
     * Quantas pessoas estão numa dada localização
     */
    public void howManyInLoc() {
        Scanner s = new Scanner(System.in);
        System.out.println("Em que localização pretende saber o número de pessoas? (Responda p.ex: 1 1)");
        String[] loc = s.nextLine().split(" ");
        if (isNotInteger(loc[0]) || isNotInteger(loc[1]) || loc[0] == null || loc[1] == null)
            System.out.println("Valores inválidos!");
        else {
            int number = this.stub.howManyInLocation(loc[0], loc[1]);
            if (number > 1) System.out.println("Estão neste momento " + number + " pessoas em (" + loc[0] + "," + loc[1] + ")");
            else if (number == 1) System.out.println("Está neste momento 1 pessoa em (" + loc[0] + "," + loc[1] + ")");
            else if (number == 0) System.out.println("Não há de momento ninguém nessa localização...");
            else System.out.println("Essa localização não é válida!");
        }
    }

    /**
     * Display do mapa onde estão indicandos quantos utilizadores e quantos
     * doentes visitaram cada localização.
     */
    public void loadMapa(int n)  {
        Map<Location, Collection<String>> map = this.stub.loadMap(n);
        for(Map.Entry<Location, Collection<String>> entry : map.entrySet()) {
            Collection<String> list = entry.getValue();
            System.out.print("Localização " + entry.getKey().toString() + ": ");
            if (list.isEmpty()) System.out.println("Vazia");
            for(String s : list)
                System.out.print("\n - Utilizador: " + s);
            if(!list.isEmpty()) System.out.println();
        }
    }

    /**
     * Display para a alteração da localização atual do user
     */
    public void verifLoc() {
        Scanner s = new Scanner(System.in);
        System.out.println("Qual a localização sobre a qual pretende ser notificado/a?");
        String[] loc = s.nextLine().split(" ");
        if (isNotInteger(loc[0]) || isNotInteger(loc[1]) || loc[0] == null || loc[1] == null)
            System.out.println("Valores inválidos!");
        else {
            Runnable r = () -> {
                this.stub.verifLoc(loc[0], loc[1]);
                System.out.print("\nAviso: Já se pode deslocar para a localização (" + loc[0] + ", " + loc[1] + ")\n> ");
            };
            new Thread(r).start();
        }
    }

    /**
     * Display para notificar um Utilizador que esteve em contacto
     * @param user identificador do utilizador
     * @return thread que corre sobre o Runnable notif
     */
    public Thread notification(String user) {
        Runnable notif = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                boolean r = this.stub.notification(user);
                if (r) System.out.print("\nVocê esteve em contacto com um infetado\n> ");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        return new Thread(notif);
    }

    /**
     * Função de IO com o utilizador
     */
    public void display() {
        boolean cont = true;
        String loggedUser = null;
        Thread t = null;
        int limit = 5;
        Scanner s = new Scanner(System.in);
        System.out.println("Conexão estabelecida! Escreva help caso necessite de ajuda...");
        while (cont) {
            System.out.print("> ");
            String input = s.nextLine();
            switch (input) {
                case "logout" -> {
                    if (loggedUser != null) {
                        logout(loggedUser);
                        loggedUser = null;
                        t.interrupt();
                    } else System.out.println("Não autenticado");
                }
                case "sair" -> {
                    exit();
                    cont = false;
                }
                case "registar" -> {
                    if (loggedUser == null) {
                        registar();
                    } else System.out.println("Já se encontra autenticado");
                }
                case "login" -> {
                    if (loggedUser == null) {
                       if ((loggedUser = login()) != null) {
                           t = notification(loggedUser);
                           t.start();
                       }
                    } else System.out.println("Já se encontra autenticado");
                }
                case "atualizar localizacao" -> {
                    if (loggedUser != null)
                        changeLoc(loggedUser);
                    else System.out.println("Não autenticado");
                }
                case "quantas pessoas" -> {
                    if (loggedUser != null)
                        howManyInLoc();
                    else System.out.println("Não autenticado");
                }
                case "carregar mapa" -> {
                    if (loggedUser != null && this.stub.isUserPrivileged(loggedUser)) {
                        loadMapa(limit);
                    } else System.out.println("Permissões insuficientes ou não autenticado.");
                }
                case "verificar localizacao" -> {
                    if (loggedUser != null) {
                        verifLoc();
                    } else System.out.println("Não autenticado");
                }
                case "comunicar infecao" -> {
                    if (loggedUser != null) {
                        this.stub.commInfection(loggedUser);
                        System.out.println("É recomendado entrar em isolamento");
                        logout(loggedUser);
                        loggedUser = null;
                        t.interrupt();
                    } else System.out.println("Não autenticado");
                }
                case "help" -> {
                    System.out.println("Lista de comandos:");
                    System.out.println(" - login                 -> Autenticação");
                    System.out.println(" - logout                -> Termina sessão do utilizador");
                    System.out.println(" - registar              -> Regista o utilizador na aplicação");
                    System.out.println(" - atualizar localizacao -> Altera a localização");
                    System.out.println(" - quantas pessoas       -> Diz quantas pessoas se encontram numa determinada localização");
                    System.out.println(" - carregar mapa         -> Carrega o Mapa com todas as localizações e utilizadores associados");
                    System.out.println(" - comunicar infecao     -> Reporta que o utilizador está infetado");
                    System.out.println(" - verificar localizacao -> Pede à aplicação para notificar o utilizador quando uma localização estiver vazia");
                    System.out.println(" - sair                  -> Fecha aplicação");
                }
                default -> System.out.println("Comando não encontrado. Escreva 'help' para uma lista de comandos.");
            }
        }
    }
}