package com.example.SeniorProject.Service;

import com.google.gson.JsonArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleMapService
{
    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String DISTANCE_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    @Autowired
    private SecretsManagerService secretsManagerService;

    public String getPlaceId(String address)
    {
        String key = secretsManagerService.getSecretValue("googleMapAPIKey");
        OkHttpClient client = new OkHttpClient();
        String url = GEOCODE_URL + "?address=" + address + "&key=" + key;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Unexpected code " + response);
            }

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            return jsonResponse.getAsJsonArray("results")
                .get(0)
                .getAsJsonObject()
                .get("place_id")
                .getAsString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public double calculateDeliveryFee(String destinationPlaceId)
    {
        String originPlaceId = secretsManagerService.getSecretValue("googleMapAPIOriginPlaceID");
        String key = secretsManagerService.getSecretValue("googleMapAPIKey");
        OkHttpClient client = new OkHttpClient();
        String url = DISTANCE_URL + "?origins=place_id:" + originPlaceId + "&destinations=place_id:"+ destinationPlaceId +"&key=" + key;
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new IOException("Unexpected code " + response);
            }

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray rows = jsonResponse.getAsJsonArray("rows");

            // Access the first row and then the first element in the elements array
            JsonObject element = rows.get(0).getAsJsonObject().getAsJsonArray("elements").get(0).getAsJsonObject();

            // Extract the "distance" object and get the "value"
            double distance = element.getAsJsonObject("distance").get("value").getAsDouble();
            double deliveryFee = Math.round((distance * 0.0006213712 * 6.00) * 100.0) / 100.0;
            return Math.max(deliveryFee, 250.00);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
