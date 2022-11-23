package functions;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;

public class HelloWorld implements HttpFunction {
    // Simple function to return "Hello World"
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String[] result = adfasdf(request);
        BufferedWriter writer = response.getWriter();
        writer.write("AccessToken: " + result[0] + " Refresh: " + result[1]);
    }

    public String[] adfasdf(HttpRequest request) throws IOException {

        String code = request.getHeaders().get("Authorization").toString();

        if (request.getHeaders().get("X-Requested-With") == null) {
            // Without the `X-Requested-With` header, this request could be forged. Aborts.
        }

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                "https://oauth2.googleapis.com/token",
                "",
                "",
                "",
                "")  // Specify the same redirect URI that you use with your web
                // app. If you don't have a web version of your app, you can
                // specify an empty string.
                .execute();

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        String[] array = new String[2];
        array[0] = accessToken;
        array[1] = refreshToken;
        return array;
    }
}
