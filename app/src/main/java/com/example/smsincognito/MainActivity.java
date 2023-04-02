package com.example.smsincognito;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class

MainActivity extends AppCompatActivity {

    EditText number_input;
    EditText text_input;
    TextView text_credit_number;
    Button send_button;
    Button buy_button;
    Button get_id_button;
    Button restore_button;
    RequestQueue request_queue;
    String user_id = "";
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_all();
    }

    @Override
    public void onResume(){
        super.onResume();
        get_token_credit();
    }

    private void init_all() {
        // demarage de l'appli
        handler = new Handler();
        number_input = (EditText)findViewById(R.id.number_input);
        text_input = (EditText) findViewById(R.id.text_input);
        send_button = (Button) findViewById(R.id.send_button);
        buy_button = (Button) findViewById(R.id.buy_button);
        get_id_button = (Button) findViewById(R.id.get_id_button);
        restore_button = (Button) findViewById(R.id.restore_button);
        text_credit_number = (TextView) findViewById(R.id.text_credit_number);
        request_queue = Volley.newRequestQueue(getApplicationContext());

        get_start_message();

        try {
            // On voit si c'est premier lancement
            FileInputStream id_file = openFileInput("id.txt");
            DataInputStream in = new DataInputStream(id_file);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine = "";
            while ((strLine = br.readLine()) != null) {
                user_id = user_id + strLine;
            }
            id_file.close();
            get_token_credit();
        } catch (FileNotFoundException e) {
            // Premier lancement
            get_id_button.setClickable(false);
            get_id_button.setEnabled(false);
            create_alerte("First time launching application", "OK");
            Toast.makeText(this, "Generating ID, please wait...", Toast.LENGTH_LONG).show();
            // On fait la requete de l'id
            make_register_request();
            text_credit_number.setText("0");
        } catch (IOException e) {
            create_alerte("Error code 16, please contact support (timothee.escandell@gmail.com).", "Ok");
        }

        buy_button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent buyActivity = new Intent(getApplicationContext(), BuyActivity.class);
                buyActivity.putExtra("user_id", user_id);
                startActivity(buyActivity);
            }
        }));

        get_id_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strr = "This is your id please keep safe to be able to restore your credits: (screen it)\n\n\n"+user_id+"\n";
                create_alerte(strr, "Ok");
            }
        });

        restore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restore_id();
            }
        });

        send_button.setEnabled(true);
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_sms();
            }
        });
    }

    private void get_token_credit() {
        JSONObject data = new JSONObject();
        if (user_id == "" || user_id == "error") {
            return;
        }
        String urrr = "http://tim-web.fr:7777/token/"+user_id;

        JsonObjectRequest json_request = new JsonObjectRequest(Request.Method.GET, urrr, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String return_status = response.get("success").toString();

                            if (return_status == "true") {
                                text_credit_number.setText(response.get("credit").toString());
                            } else {
                                String msg = response.get("message").toString();
                                create_alerte(msg, "Ok");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String return_status = error.getMessage();
                create_alerte("Error code 74, please contact support (timothee.escandell@gmail.com).", "Ok");
                text_credit_number.setText("0");
            }
        });

        request_queue.add(json_request);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 100);
    }

    private void make_register_request() {
        JSONObject data = new JSONObject();

        JsonObjectRequest json_request = new JsonObjectRequest(Request.Method.POST, "http://tim-web.fr:7777/register", data,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //return_status = response.toString();
                try {
                    String return_status = response.get("success").toString();

                    if (return_status == "true") {
                        user_id = response.get("token").toString();
                    } else {
                        user_id = "error";
                        create_alerte("Unexcepted error (41), please contact timothee.escandell@gmail.com", "Ok");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String return_status = error.getMessage();
                user_id = "error";
                create_alerte("Unexcepted error (52), please contact timothee.escandell@gmail.com", "Ok");
            }
        });

        request_queue.add(json_request);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user_id.equals("error")) {
                    create_alerte("Failed create id, try reinstalling the application or contact the support (timothee.escandell@gmail.com) please.", "Ok");
                    finish();
                } else {
                    try {
                        FileOutputStream first_time_file = openFileOutput("id.txt", MODE_PRIVATE);
                        first_time_file.write(user_id.getBytes());
                        first_time_file.close();
                        get_id_button.setEnabled(true);
                        get_id_button.setClickable(true);
                    } catch (FileNotFoundException e) {
                        create_alerte("Error code 24, please contact support (timothee.escandell@gmail.com).", "Ok");
                    } catch (IOException e) {
                        create_alerte("Error code 25, please contact support (timothee.escandell@gmail.com).", "Ok");
                    }
                    create_alerte("Success you can see your id on clicking ID", "Ok");
                }
            }
        }, 3000);
    }

    private void send_sms() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        send_sms_core();
                        return;
                    case DialogInterface.BUTTON_NEGATIVE:
                        return;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to send sms?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void send_sms_core() {
        JSONObject data = new JSONObject();

        try {
            data.put("token", user_id);
            data.put("number", number_input.getText().toString());
            data.put("message", text_input.getText().toString());
        } catch (JSONException e) {
            return ;
        }

        JsonObjectRequest json_request = new JsonObjectRequest(Request.Method.POST, "http://tim-web.fr:7777/send_sms", data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //return_status = response.toString();
                        try {
                            String return_status = response.get("success").toString();

                            if (return_status == "true") {
                                create_alerte("SMS sent successfully !", "Ok");
                            } else {
                                String error = response.get("message").toString();
                                create_alerte(error, "Ok");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String return_status = error.getMessage();
                create_alerte("Error code 22, please contact support (timothee.escandell@gmail.com).", "Ok");
            }
        });

        request_queue.add(json_request);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                get_token_credit();
            }
        }, 1000);

        return ;
    }

    private void restore_id() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Restore ID");
        alert.setMessage("Put your ID to restore it.");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Validate", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                user_id = value;
                try {
                    FileOutputStream first_time_file = openFileOutput("id.txt", MODE_PRIVATE);
                    first_time_file.write(user_id.getBytes());
                    first_time_file.close();
                }   catch (FileNotFoundException e) {
                    create_alerte("Error code 24, please contact support (timothee.escandell@gmail.com).", "Ok");
                } catch (IOException e) {
                    create_alerte("Error code 25, please contact support (timothee.escandell@gmail.com).", "Ok");
                }
                get_token_credit();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    private void get_start_message() {
        JSONObject data = new JSONObject();

        JsonObjectRequest json_request = new JsonObjectRequest(Request.Method.GET, "http://tim-web.fr:7777/message", data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //return_status = response.toString();
                        try {
                            String return_status = response.get("success").toString();

                            if (return_status == "true") {
                                String msg = response.get("message").toString();
                                create_alerte(msg, "Ok");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String return_status = error.getMessage();
                user_id = "error";
                create_alerte("Unexcepted error (57), please contact timothee.escandell@gmail.com", "Ok");
            }
        });

        request_queue.add(json_request);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 3000);
    }

    private void create_alerte(String str, String str_button) {
        AlertDialog.Builder alert_builder;
        AlertDialog alert;

        alert_builder = new AlertDialog.Builder(this);

        alert_builder.setMessage(str);
        alert_builder.setCancelable(false);
        alert_builder.setNeutralButton(str_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alert = alert_builder.create();
        alert.show();
    }

}