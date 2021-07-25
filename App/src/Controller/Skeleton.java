package Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Interface Controller.Skeleton
 * Esta interface serve para que, sempre que haja a intenção de se criar um
 * novo Controller.Controller para armazenar outros objetos, esse novo Controller.Controller
 * implemente esta interface com o método "handle" que irá, principalmente,
 * servir como um middleware entre o servidor (que irá necessitar de certos
 * dados provenientes de um pedido de um determinado User.User) e um Controller.Controller
 * (que controla o mapeamento dos dados pretendidos). Apesar de, até ao
 * momento, existir um só Controller.Controller, esta interface deve ser implementada
 * por qualquer tipo de Controller.Controller, desde que se pretenda criar uma conexão
 * entre o mesmo e o Server.Server.
 */
public interface Skeleton {
    /**
     * Dependendo da query que recebe do servidor, este método irá pedir
     * ao respetivo Controller.Controller os dados que a query necessita. De seguida e
     * caso seja necessário, irá enviar esses mesmos dados ao servidor
     * @param dis input
     * @param dos output
     * @throws Exception exceção
     */
    void handle(DataInputStream dis, DataOutputStream dos) throws Exception;
}
