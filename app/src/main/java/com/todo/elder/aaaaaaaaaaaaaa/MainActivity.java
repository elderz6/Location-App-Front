package com.todo.elder.aaaaaaaaaaaaaa;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private String username;
    private String id;
    private ListView userList;
    final List<String> list = new ArrayList<>();
    Button refresh;
    Button mapSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String requestUrl = "http://192.168.137.1:3000/api/user";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userList = findViewById(R.id.userList);
        Button createRoom = findViewById(R.id.newRoom);
        Button findRoom = findViewById(R.id.findRoom);
        refresh = findViewById(R.id.refreshBtn);
        mapSwitch  = findViewById(R.id.mapBtn);
        TextView uName = findViewById(R.id.userTxName);
        enterName();
        btnVisible();
        uName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                enterName();
                return true;
            }
        });
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newRoom();
            }
        });
        findRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username == null){
                    enterName();
                    searchRoom(requestUrl);
                }else {
                    searchRoom(requestUrl);
                }
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
            }
        });
    }
    private void enterName(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View alertView = getLayoutInflater().inflate(R.layout.user_input_name, null);
        final EditText userInp = alertView.findViewById(R.id.nameInput);
        final TextView userView = findViewById(R.id.userTxName);
        builder.setView(alertView);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = userInp.getText().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userView.setText(username);
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void searchRoom(final String url){
        System.out.println("Search Room -00>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View alertView = getLayoutInflater().inflate(R.layout.alert_search, null);
        final EditText roomId = alertView.findViewById(R.id.roomIdInput);
        builder.setView(alertView);
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                id = roomId.getText().toString();
                updateList();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
    private void btnVisible(){
        if (id == null){
            mapSwitch.setVisibility(View.INVISIBLE);
            refresh.setVisibility(View.INVISIBLE);
        }else{
            mapSwitch.setVisibility(View.VISIBLE);
            refresh.setVisibility(View.VISIBLE);
        }
    }
    private void updateList(){
        System.out.println("Update List -00++++++++++++++++++++++++++++++++++++++++");

        //send request to server
        final RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        list.clear();
        String requestUrl = "http://192.168.137.1:3000/api/user/room/search";
        JSONObject stuff = new JSONObject();
        try {
            stuff.put("_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final JSONArray array = new JSONArray();
        array.put(stuff);
        System.out.println(id+"//"+username);
        System.out.println("STUFF"+stuff);
        System.out.println("array"+array);
        System.out.println("list"+list);

        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.POST, requestUrl, array,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println(response+"reponse______________________");
                        try {
                            for (int i = 0; i<response.length(); i++){
                                JSONObject positions = response.getJSONObject(i);
                                list.add(positions.getString("name"));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView rId = findViewById(R.id.roomIdText);
                                    rId.setText("RoomID \n"+id);
                                    rId.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            ClipboardManager clipboardManager =
                                                    (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                                            ClipData clipData = ClipData.newPlainText("room Id", id);
                                            clipboardManager.setPrimaryClip(clipData);
                                            System.out.println(clipData);
                                            showToast("Copied to Clipboard");
                                            return true;
                                        }
                                    });
                                    System.out.println("added to textview-----------------------------");
                                }
                            });
                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    MainActivity.this,
                                    R.layout.cell_list,
                                    R.id.viewCell,
                                    list
                            );
                            userList.setAdapter(arrayAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
                id = null;
                TextView rId = findViewById(R.id.roomIdText);
                rId.setText("RoomID \n"+id);
                showToast("Invalid ID, or ID not found");
                btnVisible();
            }
        });
        queue.add(stringRequest);
        btnVisible();
    }
    private void showToast(String msg){
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    private void removeUser(){

    }

    private void newRoom(){
        System.out.println("New room -00-------------------------");
        String requestUrl = "http://192.168.137.1:3000/api/user/room/new";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(final String response) {
                id =  response.replace("\"", "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(response);
                        System.out.println(id+"çççççççççççççççççççççççç");
                        final TextView rId = findViewById(R.id.roomIdText);
                        rId.setText("RoomID \n"+ id);
                        rId.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                ClipboardManager clipboardManager =
                                        (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("room Id", id);
                                clipboardManager.setPrimaryClip(clipData);
                                showToast("Copied to Clipboard");
                                return true;
                            }
                        });
                    }
                });
                btnVisible();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                System.out.println(id);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> postMap = new HashMap<>();
                postMap.put("name", username);
                System.out.println(postMap);
                return postMap;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        System.out.println(stringRequest);
        queue.add(stringRequest);
    }
    public void MapAct(View view){
        Intent openMap = new Intent(this, MapsActivity.class);
        openMap.putExtra("name", username);
        openMap.putExtra("_id", id);
        startActivity(openMap);
    }
}
