package com.kdao.mygov_workflow;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import android.support.annotation.IdRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TrackingActivity extends AppCompatActivity {

    private static String GET_TRACKING_URL = "http://ec2-54-201-62-129.us-west-2.compute.amazonaws.com/api/workflows";
    private ProgressDialog progressDialog;
    private List<Case> caseList = new ArrayList<>();
    private ListView cases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        setupBottomBar();
        getAllCases();
    }

    /**
     * Private function to set up bottom bar
     */
    private void setupBottomBar() {
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTabPosition(2);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_home) {
                    // The tab with id R.id.tab_favorites was selected,
                    // change your content accordingly.
                    Intent newIntent = new Intent(TrackingActivity.this, MainActivity.class);
                    startActivity(newIntent);
                } else if (tabId == R.id.tab_claim) {
                    Intent newIntent = new Intent(TrackingActivity.this, ClaimActivity.class);
                    startActivity(newIntent);
                } else if (tabId == R.id.tab_message) {
                    Intent newIntent = new Intent(TrackingActivity.this, MessageActivity.class);
                    startActivity(newIntent);
                }
            }
        });

    }

    /**
     * Set up the list view for tracking cases
     */
    private void composeListView(){
        ArrayAdapter<Case> caseAdapter = new MyListAdapter();
        cases = (ListView) findViewById(R.id.trackingList);
        cases.setAdapter(caseAdapter);
    }

    /**
     * Get all the tracking cases in the list
     */
    private void getAllCases() {
        class CaseAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(TrackingActivity.this, "", "Loading");
            }

            @Override
            protected String doInBackground(String... params) {

                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(GET_TRACKING_URL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    InputStreamReader streamReader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    StringBuilder sb = new StringBuilder();
                    String tmp = null;
                    while ((tmp = bufferedReader.readLine()) != null) {
                        sb.append(tmp + "\n");
                    }
                    return sb.toString();
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
                super.onPostExecute(result);
                progressDialog.dismiss();
                try {
                    JSONParser jsonParser = new JSONParser();
                    JSONArray jsonArray = (JSONArray) jsonParser.parse(result);
                    composeCases(jsonArray);
                    composeListView();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        CaseAsyncTask caseAsyncTask = new CaseAsyncTask();
        caseAsyncTask.execute();
    }

    /**
     * Sub-function for getting tracking cases
     */
    private void composeCases(JSONArray jsonArray) {
        if (jsonArray.size() == 0) {
            Toast.makeText(getApplicationContext(), "No Tracking cases", Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                JSONObject workFlow = (JSONObject) object.get("WorkflowType");
                if(object != null) {
                    String name = workFlow.get("name").toString();
                    String status = object.get("currentStateId").toString();
                    String comment = workFlow.get("description").toString();
                    Case trackingCase = new Case(name,status,comment);
                    caseList.add(trackingCase);
                }
            }
            System.out.println(caseList.size());
        }
    }

    private class MyListAdapter extends ArrayAdapter<Case>{
        public MyListAdapter(){
            super(TrackingActivity.this, R.layout.tracking_case, caseList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.tracking_case, parent, false);
            }

            Case trackingCase = caseList.get(position);
            //TextView caseIDText = (TextView) itemView.findViewById(R.id.case_id);
            //caseIDText.setText(trackingCase.getCaseID());
            TextView caseNameText = (TextView) itemView.findViewById(R.id.case_name);
            caseNameText.setText(trackingCase.getCaseName());
            TextView caseStatusText = (TextView) itemView.findViewById(R.id.case_status);
            caseStatusText.setText(trackingCase.getStatus());
            TextView descriptionText = (TextView) itemView.findViewById(R.id.comment_content);
            descriptionText.setText(trackingCase.getComment());
            return itemView;
        }

    }
}
