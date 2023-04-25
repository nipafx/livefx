package dev.nipafx.livefx.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import dev.nipafx.livefx.twitch.TwitchCredentials;

@ConfigurationProperties(prefix = "twitch")
public record TwitchCredentialsProperties(String userName, String userId, String userToken, String appId, String appToken) {

    public TwitchCredentialsProperties {
        Assert.hasText(userName, "No userName available set property twitch.user-name");
        Assert.hasText(userId, "No userId available set property twitch.user-id");
        Assert.hasText(userToken, "No userToken available set property twitch.user-token");
        Assert.hasText(appId, "No appId available set property twitch.app-id");
        Assert.hasText(appToken, "No appToken available set property twitch.app-token");
    }

    public TwitchCredentials toTwitchCredentials() {
        return new TwitchCredentials(userName(), userId(), userToken(), appId(), appToken());
    }
}
