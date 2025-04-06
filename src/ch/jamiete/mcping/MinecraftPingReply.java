package ch.jamiete.mcping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MinecraftPingReply {
    private Object description; // Змінюємо тип на Object
    private Players players;
    private Version version;
    private String favicon;

    public String getDescription() {
        if (description instanceof String) {
            return (String) description;
        } else if (description instanceof JsonObject) {
            JsonObject descObj = (JsonObject) description;
            JsonElement textElement = descObj.get("text");
            return textElement != null ? textElement.getAsString() : descObj.toString();
        }
        return null;
    }

    public Players getPlayers() { return players; }
    public Version getVersion() { return version; }
    public String getFavicon() { return favicon; }

    public static class Players {
        private int max;
        private int online;
        private List<Player> sample;

        public int getMax() { return max; }
        public int getOnline() { return online; }
        public List<Player> getSample() { return sample; }
    }

    public static class Version {
        private String name;
        private int protocol;

        public String getName() { return name; }
        public int getProtocol() { return protocol; }
    }

    public static class Player {
        private String name;
        private String id;

        public String getName() { return name; }
        public String getId() { return id; }
    }
}