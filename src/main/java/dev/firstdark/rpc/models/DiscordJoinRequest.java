package dev.firstdark.rpc.models;

/**
 * @author HypherionSA
 * Event sent when a Join/Spectate request is made
 */
public class DiscordJoinRequest {

    /**
     * The user that requested to join
     */
    private final User user;

    public DiscordJoinRequest(User user) {
        this.user = user;
    }

    public User getUser() { return user; }
}
