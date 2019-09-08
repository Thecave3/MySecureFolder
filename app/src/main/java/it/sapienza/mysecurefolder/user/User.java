package it.sapienza.mysecurefolder.user;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable {

    private final String name;
    private final String faceId;
    private final String audioId;

    public User(JSONObject user) throws JSONException {
        this.name = user.getString("name");
        this.faceId = user.getString("GUUID_Face");
        this.audioId = user.getString("GUUID_Audio");
    }

    public String getAudioId() {
        return audioId;
    }

    public String getName() {
        return name;
    }

    public String getFaceId() {
        return faceId;
    }
}
