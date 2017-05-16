package com.kdao.mygov_workflow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.kdao.mygov_workflow.util.PreferenceData;
import com.kdao.mygov_workflow.util.Utility;
import com.kdao.mygov_workflow.util.Config;

import java.io.*;

public class SigninActivity extends AppCompatActivity {
    private TextView errMsg;
    private EditText emailText;
    private EditText pwdText;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        boolean userLoggedIn = PreferenceData.getUserLoggedInStatus(getApplicationContext());
        if (userLoggedIn == false) {
            getElem();
        } else { //user logged in
            _nagivateToMainActivity();
        }
    }

    private void getElem() {
        emailText = (EditText) findViewById(R.id.loginEmail);
        pwdText = (EditText) findViewById(R.id.loginPassword);
    }

    private void logUserIn(String email, String pwd) {
        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(SigninActivity.this, "", Config.AUTHENTICATE);
            }

            @Override
            protected String doInBackground(String... params) {
                String userEmail = params[0];
                String userPassword = params[1];
                URL url = null;
                HttpURLConnection urlConnection = null;
                System.out.println(userEmail + "/" + userPassword);
                try {
                    url = new URL(new String(Constants.URL_API+ "login"));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    urlConnection.setDoInput(true);
                    urlConnection.setUseCaches (false);
                    urlConnection.addRequestProperty("account_type", "resident");
                    System.out.println(">>>> Request header <<<<");
                    System.out.println(urlConnection.getHeaderField("account_type"));
                    //Send request body
                    DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());
                    try {
                        JSONObject jsonParam = new JSONObject();
                        jsonParam.put("email", userEmail);
                        jsonParam.put("password", userPassword);
                        wr.writeBytes(jsonParam.toString());
                        wr.flush();
                        wr.close();
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                    //Get Response
                    int responseCode = urlConnection.getResponseCode();
                    System.out.println("\nSending 'POST' request to URL : " + url);
                    System.out.println("Response Code : " + responseCode);
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    System.out.println(response.toString());
                    return response.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                return null;
            }
            @Override
            protected void onPostExecute(String result) {
                progressDialog.dismiss();
                super.onPostExecute(result);
                String userId = "";
                System.out.println(">>>>>> Return value <<<<<");
                System.out.println(result);
                _nagivateToMainActivity();
                try {
                    JSONObject jObject  = new JSONObject(result);
                    userId = (String) jObject.get("id");
                } catch(Exception ex) {
                }
                if(!Utility.isEmptyString(userId)){
                    PreferenceData.setUserLoggedInStatus(getApplication(), true);
                    PreferenceData.setLoggedInUserId(getApplication(), userId);
                    _nagivateToMainActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid email or password. Please " +
                            "try again!", Toast.LENGTH_LONG).show();
                }
            }

        }
        SendPostReqAsyncTask postClaimAsyncTask = new SendPostReqAsyncTask();
        postClaimAsyncTask.execute(email, pwd);
    }

    public void loginUser(View view) {
        String email = "";
        String password = "";
        try {
            email = emailText.getText().toString();
            password = pwdText.getText().toString();
        } catch(Exception ex) {}
        if (!Utility.isEmptyString(email) && !Utility.isEmptyString(password)) {
            // When Email entered is Valid
            if (Utility.isEmailValid(email)) {
                logUserIn(email, password);
            } else { // When Email is invalid
                Toast.makeText(getApplicationContext(), Config.VALID_EMAIL, Toast.LENGTH_LONG).show();
            }
        } else { //Empty form handle
            Toast.makeText(getApplicationContext(), Config.VALID_FORM, Toast.LENGTH_LONG).show();
        }
    }

    private void _nagivateToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainIntent);
    }
}
