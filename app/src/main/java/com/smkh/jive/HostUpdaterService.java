package com.smkh.jive;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class HostUpdaterService extends IntentService {
    String token;

    public HostUpdaterService() {
        super("HostUpdaterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("HOSTUPDATERSERVICE", "SERVICE STARTED");
        RequestClass.hostUpdateRequest(getApplicationContext(), "https://sciencemaniac.000webhostapp.com/gethostforjive.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResult = new JSONObject(response);
                    String result = jsonResult.getString("host");
                    SharedPreferences.Editor editor =  getSharedPreferences("SharedPreferences", MODE_PRIVATE).edit();
                    editor.putString("serverpath", result);
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SharedPreferences prefs = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token != null) {
            Integer appID = prefs.getInt("appid", -1);
            if (appID != - 1) {
                sendTokenToServer(token, appID);
            }
        }
        scheduleRestart();
    }

    private void scheduleRestart() {
        //Intent intent = new Intent(this, MyReceiver.class);
        Intent intent = new Intent("com.smkh.jive.HostUpdaterReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am= (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.ELAPSED_REALTIME, 7200000, pendingIntent);
    }

    public void sendTokenToServer(final String token, final Integer id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("appid", String.valueOf(id));
        RequestClass.stringRequest(this, "add_token.php", Request.Method.POST, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}
        });
    }

}
