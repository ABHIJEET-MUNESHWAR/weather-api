package org.weather.api;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static class WeatherInfo {
        String name;
        String weather;
        List<String> status;

        @Override
        public String toString() {
            String wind = this.status.get(0);
            wind = wind.substring(6, wind.length() - 4);
            String humidity = this.status.get(1);
            humidity = humidity.substring(10, humidity.length() - 1);
            String weather = this.weather;
            weather = weather.substring(0, weather.length() - 7);
            return name + "," + weather + "," + wind + "," + humidity;
        }
    }

    static class WeatherResponse {
        int page;
        int per_page;
        int total;
        int total_pages;
        List<WeatherInfo> data;
    }

    public static void main(String[] args) throws Exception {
        String keyword = args[0];
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            List<WeatherInfo> data = getAllWeather(keyword, httpClient);
            for (WeatherInfo info : data) {
                if (!keyword.equalsIgnoreCase("all")) {
                    if (info.name.contains(keyword)) {
                        System.out.println(info.toString());
                    }
                } else {
                    System.out.println(info.toString());
                }
            }
        }
    }

    public static List<WeatherInfo> getAllWeather(String keyword, CloseableHttpClient httpClient) throws Exception {
        int page = 1;
        List<WeatherInfo> data = new ArrayList<>();
        while (true) {
            WeatherResponse weatherResponse = getWeather(keyword, page, httpClient);
            if (weatherResponse.data.isEmpty()) {
                break;
            }
            data.addAll(weatherResponse.data);
            page++;
        }
        return data;
    }

    public static WeatherResponse getWeather(String keyword, int page, CloseableHttpClient httpClient) throws Exception {
        URIBuilder builder = new URIBuilder("https://jsonmock.hackerrank.com/api/weather/search");
        builder.addParameter("name", keyword);
        builder.addParameter("page", String.valueOf(page));
        URI uri = builder.build();
        HttpGet request = new HttpGet(uri);
        try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException(String.format("Unexpected status code: %d", statusCode));
            }
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            Gson gson = new Gson();
            WeatherResponse weatherResponse = gson.fromJson(result, WeatherResponse.class);
            return weatherResponse;
        }
    }
}