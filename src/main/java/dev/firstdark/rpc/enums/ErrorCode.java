package dev.firstdark.rpc.enums;

/**
 * @author HypherionSA
 * Various errors codes that can be returned by the IPC Pipe
 */
public enum ErrorCode {
    SUCCESS(0),
    PIPE_CLOSED(1),
    READ_CORRUPT(2),
    UNKNOWN(-1),
    USER_LOGOUT(1000);

    private final int id;

    ErrorCode(int id) {
        this.id = id;
    }

    public int getId() { return id; }
}
