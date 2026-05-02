package dev.firstdark.rpc.enums;

/**
 * @author HypherionSA
 * Valid types of IPC packets that can be sent
 */
public enum OpCode {
    HANDSHAKE(0),
    FRAME(1),
    CLOSE(2),
    PING(3),
    PONG(4);

    private final int id;

    OpCode(int id) {
        this.id = id;
    }

    public int getId() { return id; }
}
