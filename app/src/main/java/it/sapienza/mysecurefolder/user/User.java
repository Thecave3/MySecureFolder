package it.sapienza.mysecurefolder.user;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Class representing the user in the application
 */
public class User implements Serializable {

    private final String name;
    private final String faceId;
    private final String audioId;
    private final String identificationAudioId;

    /**
     * @param user JSONObject representing the user, passed by the server
     * @throws JSONException if one of the fields is not present (probably because is not returned by server)
     */
    public User(JSONObject user) throws JSONException {
        this.name = user.getString("name");
        this.faceId = user.getString("GUUID_Face");
        this.audioId = user.getString("GUUID_Audio");
        this.identificationAudioId = user.getString("GUUID_Identification_Audio");
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

    public String getIdentificationAudioId() {
        return identificationAudioId;
    }
}
