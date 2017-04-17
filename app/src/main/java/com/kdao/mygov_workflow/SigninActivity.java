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

import com.kdao.mygov_workflow.util.PreferenceData;
import com.kdao.mygov_workflow.util.Utility;
import com.kdao.mygov_workflow.util.Config;


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
