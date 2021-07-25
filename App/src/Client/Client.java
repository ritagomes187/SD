package Client;

import Server.Stub;

public class Client {
    public static void main(String[] args) {
        Stub stub = new Stub();
        new Prompt(stub).display();
    }
}