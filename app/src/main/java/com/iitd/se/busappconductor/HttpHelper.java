package com.iitd.se.busappconductor;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by harshit on 19/4/16.
 */
public class HttpHelper {
    private String domain;

    public HttpHelper(String domain) {
        this.domain = domain;
    }

    public JSONObject postJson(String location, JSONObject params, String email, String token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
        HttpPost httpPost = new HttpPost(domain + location);
        HttpResponse httpResponse = null;
        JSONObject jsonResponse = null;
        try {
            StringEntity stringEntity = new StringEntity(params.toString());
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("X-User-Email", email);
            httpPost.setHeader("X-User-Token", token);
            httpResponse = httpClient.execute(httpPost);
            if(httpResponse == null)
                return null;
            InputStream in = httpResponse.getEntity().getContent();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);
            String response = stringBuilder.toString();
            jsonResponse = new JSONObject(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    public JSONObject putJson(String location, JSONObject params, String email, String token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
        HttpPut httpPut = new HttpPut(domain + location);
        HttpResponse httpResponse = null;
        JSONObject jsonResponse = null;
        try {
            StringEntity stringEntity = new StringEntity(params.toString());
            httpPut.setEntity(stringEntity);
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader("X-User-Email", email);
            httpPut.setHeader("X-User-Token", token);
            httpResponse = httpClient.execute(httpPut);
            if(httpResponse == null)
                return null;
            InputStream in = httpResponse.getEntity().getContent();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);
            String response = stringBuilder.toString();
            jsonResponse = new JSONObject(response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    public void deleteJson(String location, String email, String token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
        HttpDelete httpDelete = new HttpDelete(domain + location);
        try {
            httpDelete.setHeader("Accept", "application/json");
            httpDelete.setHeader("Content-type", "application/json");
            httpDelete.setHeader("X-User-Email", email);
            httpDelete.setHeader("X-User-Token", token);
            httpClient.execute(httpDelete);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJson(String location, String email, String token) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 10000);
        HttpGet httpGet = new HttpGet(domain + location);
        HttpResponse httpResponse = null;
        JSONObject jsonResponse = null;
        try {
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("X-User-Email", email);
            httpGet.setHeader("X-User-Token", token);
            httpResponse = httpClient.execute(httpGet);
            if(httpResponse == null)
                return null;
            InputStream in = httpResponse.getEntity().getContent();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line;
            while((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line);
            String response = stringBuilder.toString();
            jsonResponse = new JSONObject(response);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }
}
