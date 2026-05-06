package dev.firstdark.rpc.models;

import com.google.gson.JsonPrimitive;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.firstdark.rpc.enums.ActivityType;
import dev.firstdark.rpc.enums.PartyPrivacy;
import dev.firstdark.rpc.enums.StatusDisplayType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HypherionSA
 * The Discord Rich Presence Structure
 * Use {@link DiscordRichPresence#builder()} to get started
 */
public class DiscordRichPresence {

    private String name;
    private String state;
    private String details;
    private long startTimestamp;
    private long endTimestamp;

    private String largeImageKey;
    private String largeImageText;
    private String smallImageKey;
    private String smallImageText;

    private String partyId;
    private int partySize;
    private int partyMax;

    private String matchSecret;
    private String joinSecret;
    private String spectateSecret;
    private boolean instance;

    // Urls
    private String stateUrl;
    private String detailsUrl;
    private String largeUrl;
    private String smallUrl;

    private PartyPrivacy privacy = PartyPrivacy.PRIVATE;
    private ActivityType activityType = ActivityType.PLAYING;
    private StatusDisplayType statusDisplayType = StatusDisplayType.NAME;

    private List<RPCButton> buttons = new ArrayList<>();

    public DiscordRichPresence() {}

    public static Builder builder() {
        return new Builder();
    }

    public void addButton(String label, String url) {
        if (buttons.size() + 1 > 2)
            return;

        buttons.add(new RPCButton(label, url));
    }

    public void clearButtons() {
        this.buttons.clear();
    }

    public JsonObject toJson(long pid, long nonce) {
        JsonObject data = new JsonObject();
        data.addProperty("nonce", nonce);
        data.addProperty("cmd", "SET_ACTIVITY");

        JsonObject args = new JsonObject();
        args.addProperty("pid", pid);

        JsonObject activity = new JsonObject();

        if (isNotNullOrEmpty(name))
            activity.addProperty("name", name);

        activity.addProperty("status_display_type", statusDisplayType.getId());

        if (isNotNullOrEmpty(state)) {
            activity.addProperty("state", state);
            if (isNotNullOrEmpty(stateUrl))
                activity.addProperty("state_url", stateUrl);
        }

        if (isNotNullOrEmpty(details)) {
            activity.addProperty("details", details);
            if (isNotNullOrEmpty(detailsUrl))
                activity.addProperty("details_url", detailsUrl);
        }

        if (this.startTimestamp != 0 || this.endTimestamp != 0) {
            JsonObject timestamps = new JsonObject();
            if (this.startTimestamp != 0)
                timestamps.addProperty("start", this.startTimestamp);
            if (this.endTimestamp != 0)
                timestamps.addProperty("end", this.endTimestamp);
            activity.add("timestamps", timestamps);
        }

        if (isNotNullOrEmpty(largeImageKey) || isNotNullOrEmpty(largeImageText) || isNotNullOrEmpty(smallImageKey) || isNotNullOrEmpty(smallImageText)) {
            JsonObject assets = new JsonObject();
            if (isNotNullOrEmpty(largeImageKey)) {
                assets.addProperty("large_image", this.largeImageKey);
                if (isNotNullOrEmpty(largeUrl))
                    assets.addProperty("large_url", largeUrl);
            }
            if (isNotNullOrEmpty(largeImageText))
                assets.addProperty("large_text", this.largeImageText);
            if (isNotNullOrEmpty(smallImageKey)) {
                assets.addProperty("small_image", this.smallImageKey);
                if (isNotNullOrEmpty(smallUrl))
                    assets.addProperty("small_url", smallUrl);
            }
            if (isNotNullOrEmpty(smallImageText))
                assets.addProperty("small_text", this.smallImageText);
            activity.add("assets", assets);
        }

        if (isNotNullOrEmpty(partyId) || this.partySize > 0 || this.partyMax > 0) {
            JsonObject party = new JsonObject();
            if (isNotNullOrEmpty(partyId))
                party.addProperty("id", partyId);
            if (partySize != 0) {
                JsonArray size = new JsonArray();
                size.add(new JsonPrimitive(partySize));
                if (partyMax > 0)
                    size.add(new JsonPrimitive(partyMax));
                party.add("size", size);
            }
            party.addProperty("privacy", privacy.ordinal());
            activity.add("party", party);
        }

        if (isNotNullOrEmpty(matchSecret) || isNotNullOrEmpty(spectateSecret) || isNotNullOrEmpty(joinSecret)) {
            JsonObject secrets = new JsonObject();
            if (isNotNullOrEmpty(matchSecret))
                secrets.addProperty("match", matchSecret);
            if (isNotNullOrEmpty(joinSecret))
                secrets.addProperty("join", joinSecret);
            if (isNotNullOrEmpty(spectateSecret))
                secrets.addProperty("spectate", spectateSecret);
            activity.add("secrets", secrets);
        }

        if (!buttons.isEmpty()) {
            List<RPCButton> finalButtons = buttons.stream().filter(RPCButton::isValid).collect(Collectors.toList());
            if (finalButtons.size() > 2)
                finalButtons = finalButtons.subList(0, 2);
            JsonArray btns = new JsonArray();
            finalButtons.forEach(b -> btns.add(b.toJson()));
            activity.add("buttons", btns);
        }

        activity.addProperty("type", activityType.ordinal());
        activity.addProperty("instance", instance);
        args.add("activity", activity);
        data.add("args", args);

        return data;
    }

    private boolean isNotNullOrEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    public static class RPCButton {
        private final String label;
        private final String url;

        public RPCButton(String label, String url) {
            this.label = label;
            this.url = url;
        }

        public boolean isValid() {
            return label != null && !label.isEmpty() && url != null && !url.isEmpty();
        }

        public JsonObject toJson() {
            JsonObject button = new JsonObject();
            button.addProperty("label", label.substring(0, Math.min(label.length(), 32)));
            button.addProperty("url", url);
            return button;
        }
    }

    public static class Builder {
        private final DiscordRichPresence presence = new DiscordRichPresence();

        public Builder name(String name) { presence.name = name; return this; }
        public Builder state(String state) { presence.state = state; return this; }
        public Builder details(String details) { presence.details = details; return this; }
        public Builder startTimestamp(long startTimestamp) { presence.startTimestamp = startTimestamp; return this; }
        public Builder endTimestamp(long endTimestamp) { presence.endTimestamp = endTimestamp; return this; }
        public Builder largeImageKey(String largeImageKey) { presence.largeImageKey = largeImageKey; return this; }
        public Builder largeImageText(String largeImageText) { presence.largeImageText = largeImageText; return this; }
        public Builder smallImageKey(String smallImageKey) { presence.smallImageKey = smallImageKey; return this; }
        public Builder smallImageText(String smallImageText) { presence.smallImageText = smallImageText; return this; }
        public Builder partyId(String partyId) { presence.partyId = partyId; return this; }
        public Builder partySize(int partySize) { presence.partySize = partySize; return this; }
        public Builder partyMax(int partyMax) { presence.partyMax = partyMax; return this; }
        public Builder matchSecret(String matchSecret) { presence.matchSecret = matchSecret; return this; }
        public Builder joinSecret(String joinSecret) { presence.joinSecret = joinSecret; return this; }
        public Builder spectateSecret(String spectateSecret) { presence.spectateSecret = spectateSecret; return this; }
        public Builder instance(boolean instance) { presence.instance = instance; return this; }
        public Builder privacy(PartyPrivacy privacy) { presence.privacy = privacy; return this; }
        public Builder activityType(ActivityType activityType) { presence.activityType = activityType; return this; }
        public Builder statusDisplayType(StatusDisplayType statusDisplayType) { presence.statusDisplayType = statusDisplayType; return this; }

        public DiscordRichPresence build() {
            return presence;
        }
    }
}
