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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String requestUrl = "http://192.168.137.1:3000/api/user";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userList = findViewById(R.id.userList);
        Button createRoom = findViewById(R.id.newRoom);
        Button findRoom = findViewById(R.id.findRoom);
        Button refresh = findViewById(R.id.refreshBtn);
        TextView uName = findViewById(R.id.userTxName);
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
                searchRoom(requestUrl);
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                list.clear();
                updateList(id, queue, list);
            }
        });
    }
    private void searchRoom(final String url){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View alertView = getLayoutInflater().inflate(R.layout.alert_search, null);
        final EditText roomId = alertView.findViewById(R.id.roomIdInput);
        builder.setView(alertView);
        final RequestQueue queue = Volley.newRequestQueue(this);
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              updateList(roomId.getText().toString(), queue, list);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        R.layout.cell_list,
                        R.id.viewCell,
                        list
                );
                userList.setAdapter(arrayAdapter);
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
    private void updateList(String roomId, RequestQueue queue, final List<String> list){
        //send request to server
        String requestUrl = "http://192.168.137.1:3000/api/user/room/search";
        JSONObject stuff = new JSONObject();
        id = roomId;
        try {
            stuff.put("_id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray array = new JSONArray();
        array.put(stuff);
        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.POST, requestUrl, array,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println(response+"reponse______________________");
                        try {
                            for (int i = 0; i<response.length(); i++){
                                JSONObject positions = response.getJSONObject(i);
                                list.add(positions.getString("name"));
                                System.out.println("added to list -----------------------------");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TextView rId = findViewById(R.id.roomIdText);
                                        rId.setText("RoomID \n"+id);
                                        rId.setOnLongClickListener(new View.OnLongClickListener() {
                                            @Override
                                            public boolean onLongClick(View v) {
                                                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
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
                                System.out.println("printed to list -----------------------------");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
                id = "";
                TextView rId = findViewById(R.id.roomIdText);
                rId.setText("RoomID \n"+id);
                showToast("Invalid ID, or ID not found");
            }
        });
        queue.add(stringRequest);
    }
    private void showToast(String msg){
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
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
    private void newRoom(){

    }
    public void MapAct(View view){
        Intent openMap = new Intent(this, MapsActivity.class);
        openMap.putExtra("username", username);
        openMap.putExtra("_id", id);
        startActivity(openMap);
    }
}
