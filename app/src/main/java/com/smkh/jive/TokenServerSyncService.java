package com.smkh.jive;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class TokenServerSyncService extends IntentService {
    String token;

    public TokenServerSyncService() {
        super("TokenServerSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("TOKENSERVERSYNCSERVICE", "SERVICE STARTED");
        SharedPreferences prefs = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null) {
            scheduleRestart();
        } else {
            this.token = token;
            Integer appID = prefs.getInt("appid", -1);
            Boolean isTokenSent = prefs.getBoolean("isSentToken", false);
            if (!isTokenSent) {
                if (appID == -1) {
                    getIDFromServer();
                } else {
                    sendTokenToServer(token, appID);
                }
            }
        }
    }

    public void getIDFromServer() {
        RequestClass.stringRequest(this, "get_id.php", Request.Method.GET, null, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getIDFromServerSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getIDFromServerError(error);
            }
        });
    }

    public void sendTokenToServer(final String token, final Integer id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("appid", String.valueOf(id));
        RequestClass.stringRequest(this, "add_token.php", Request.Method.POST, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                sendTokenToServerSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendTokenToServerError(error);
            }
        });
    }

    private void getIDFromServerSuccess(String response) {
        try {
            JSONObject jsonResult = new JSONObject(response);
            String result = jsonResult.getString("success");
            if (result.equals("1")) {
                Integer id = jsonResult.getInt("appid");
                SharedPreferences.Editor editor = getSharedPreferences("SharedPreferences", MODE_PRIVATE).edit();
                editor.putInt("appid", id);

                editor.commit();
                sendTokenToServer(token, id);
            } else {
                scheduleRestart();
            }
        } catch (JSONException e) {
            scheduleRestart();
            e.printStackTrace();
        }
    }

    private void getIDFromServerError(VolleyError error) {
        scheduleRestart();
    }

    private void sendTokenToServerSuccess(String response) {
        try {
            JSONObject jsonResult = new JSONObject(response);
            String result = jsonResult.getString("success");
            if (result.equals("1")) {
                SharedPreferences.Editor editor = getSharedPreferences("SharedPreferences", MODE_PRIVATE).edit();
                editor.putBoolean("isSentToken", true);
                editor.commit();
            } else {
                scheduleRestart();
            }
        } catch (JSONException e) {
            scheduleRestart();
            e.printStackTrace();
        }
    }

    private void sendTokenToServerError(VolleyError error) {
        scheduleRestart();
    }

    private void scheduleRestart() {
        //Intent intent = new Intent(this, MyReceiver.class);
        Intent intent = new Intent("com.smkh.jive.MyReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.ELAPSED_REALTIME, 600000, pendingIntent);
    }

}
