package dev.firstdark.rpc.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author HypherionSA
 * Represents a Discord User. Not all information is returned here, because it's not needed
 */
public class User {

    private String userId;
    private String username;

    @Deprecated
    private String discriminator;

    @SerializedName("global_name")
    private String globalName;
    private String avatar;

    public User() {}

    public User(String userId, String username, String discriminator, String globalName, String avatar) {
        this.userId = userId;
        this.username = username;
        this.discriminator = discriminator;
        this.globalName = globalName;
        this.avatar = avatar;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getDiscriminator() { return discriminator; }
    public String getGlobalName() { return globalName; }
    public String getAvatar() { return avatar; }
}
