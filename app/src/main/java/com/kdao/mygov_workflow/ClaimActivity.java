package com.kdao.mygov_workflow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
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

public class ClaimActivity extends AppCompatActivity {
    static CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
    static String workflowTypeDefault = "Please select";

    static int TAKE_PHOTO = 1;
    static int CHOOSE_PHOTO = 2;
    static int CURRENT_INDEX = 0;
    static int NumOfSubmittedImages = 0;
    static int workflowTypeSelect = 0;
    static int critical = 0;
    static String note = "";
    final private static Integer NumOfImages = 4;

    private LocationManager locationManager;
    Location location; // Location
    double latitude = 0.0; // Latitude
    double longitude = 0.0; // Longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    Button btnAttachImage;
    Button btnSubmitClaim;
    Button btnRemoveLastImage;
    Button btnResetClaim;

    Spinner workflowType;
    Spinner criticalLevel;
    EditText descriptionText;
    String[] workflowTypeList;
    String[] workflowTypeIDs;
    String[] imageAddress;
    boolean[] imageExist;
    ImageView[] viewImage;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle){

        }
    };

    private TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        setupBottomBar();
        init();

    }

    private void init(){
        transferUtility = Util.getTransferUtility(this);

        btnResetClaim = (Button)findViewById(R.id.btnResetClaim);
        btnSubmitClaim = (Button)findViewById(R.id.btnSubmitClaim);
        btnAttachImage = (Button)findViewById(R.id.btnAttachImages);
        btnRemoveLastImage = (Button)findViewById(R.id.btnRemovePreviousImage);
        workflowType = (Spinner)findViewById(R.id.WorkflowTypeList);
        criticalLevel = (Spinner)findViewById(R.id.CriticalLevels);
        descriptionText = (EditText)findViewById(R.id.Description_text);

        imageAddress = new String[NumOfImages];
        imageExist = new boolean[NumOfImages];

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // TODO: Modify if NumOfImages != 4
        viewImage = new ImageView[NumOfImages];
        viewImage[0] = (ImageView) findViewById(R.id.image1);
        viewImage[1] = (ImageView) findViewById(R.id.image2);
        viewImage[2] = (ImageView) findViewById(R.id.image3);
        viewImage[3] = (ImageView) findViewById(R.id.image4);

        btnResetClaim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ClaimActivity.this)
                        .setTitle("Clear everything?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                refresh();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        btnRemoveLastImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ClaimActivity.this)
                        .setTitle("Remove last image?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (CURRENT_INDEX > 0) {
                                    Drawable emptyImage = getResources().getDrawable(android.R.drawable.ic_menu_camera);
                                    viewImage[--CURRENT_INDEX].setImageDrawable(emptyImage);
                                    imageAddress[CURRENT_INDEX] = "";
                                    imageExist[CURRENT_INDEX] = false;
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        btnAttachImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClaimActivity.this);
                builder.setTitle("Attach an image (Up to 4)!");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            startActivityForResult(intent, TAKE_PHOTO);
                        } else if (options[item].equals("Choose from Gallery")) {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, CHOOSE_PHOTO);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        btnSubmitClaim.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // TODO: POST subject, claimType and Description
                    if (workflowType.getSelectedItemId() == 0) {
                        Toast.makeText(ClaimActivity.this,
                                "Please select the one which best describes your claim!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (criticalLevel.getSelectedItemId() == 0) {
                        Toast.makeText(ClaimActivity.this,
                                "Please select the critical level!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (descriptionText.getText().length() == 0) {
                        Toast.makeText(ClaimActivity.this,
                                "Please enter the description!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    workflowTypeSelect = (int)workflowType.getSelectedItemId();
                    critical = (int)criticalLevel.getSelectedItemId();
                    note = descriptionText.getText().toString();

                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
                    }
                    catch (SecurityException e){
                        e.printStackTrace();
                    }

                    Toast.makeText(ClaimActivity.this,
                            "long:"+String.valueOf(longitude)+" lat:"+String.valueOf(latitude),
                            Toast.LENGTH_LONG).show();

                    for (int i = 0; i < NumOfImages; i++) {
                        if (imageExist[i]) {
                            File file = new File(imageAddress[i]);
                            // The file's link = Constants.URL_S3Bucket + imageAddress[i]
                            TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(),
                                    file);
                            observer.setTransferListener(new UploadListener());
                        }
                    }
                    postClaim();
                    refresh();
                } catch (Exception e) {
                    Toast.makeText(ClaimActivity.this,
                            "Unable to get the file from the given URI.  See error log for details",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        refresh();

    }

    private void refresh(){
        descriptionText.setText("");
        getAllWorkflowConfigures();
        workflowType.setSelection(0);
        criticalLevel.setSelection(0);
        resetImages();
    }

    private void resetImages() {
        Drawable emptyImage = getResources().getDrawable(android.R.drawable.ic_menu_camera);
        for (int i = 0; i < NumOfImages; i++) {
            viewImage[i].setImageDrawable(emptyImage);
            imageAddress[i] = "";
            imageExist[i] = false;
        }
        CURRENT_INDEX = 0;
        NumOfSubmittedImages = 0;
    }


    private void getAllWorkflowConfigures() {
        class GetWorkflowTypesAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {

                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(new String(Constants.URL_API + "workflow_configure"));
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
                try {
                    JSONParser jsonParser = new JSONParser();
                    JSONArray jsonArray = (JSONArray) jsonParser.parse(result);
                    if (jsonArray.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No workflow types available", Toast.LENGTH_LONG).show();
                    } else {
                        workflowTypeList = new String[jsonArray.size()+1];
                        workflowTypeIDs = new String[workflowTypeList.length];
                        workflowTypeList[0] = workflowTypeDefault;
                        workflowTypeIDs[0] = "";
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JSONObject object = (JSONObject) jsonArray.get(i);
                            if(object != null) {
                                workflowTypeList[i+1] = object.get("name").toString();
                                workflowTypeIDs[i+1] = object.get("id").toString();
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,workflowTypeList);
                        workflowType.setAdapter(adapter);
                        OnItemSelectedListener OnCatSpinnerCL = new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                ((TextView) parent.getChildAt(0)).setTextColor(0xFF000000);
                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        };
                        workflowType.setOnItemSelectedListener(OnCatSpinnerCL);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        GetWorkflowTypesAsyncTask getWorkflowTypesAsyncTask = new GetWorkflowTypesAsyncTask();
        getWorkflowTypesAsyncTask.execute();
    }

    private void postClaim() {
        class PostClaimAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {

                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(new String(Constants.URL_API+"workflows"));
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);
                    //Send request body
                    DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream ());
                    try {
                        org.json.JSONObject jsonParam = new org.json.JSONObject();
                        jsonParam.put("type_id",  workflowTypeIDs[workflowTypeSelect]);
                        jsonParam.put("critical", critical);
                        jsonParam.put("note", note);
                        jsonParam.put("longitude", longitude);
                        jsonParam.put("latitude", latitude);
                        wr.writeBytes(jsonParam.toString());
                        wr.flush();
                        wr.close();
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }
                    OutputStream oStream = urlConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(oStream));
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    oStream.close();
                    int responseCode = urlConnection.getResponseCode();
                    System.out.println(urlConnection.getResponseMessage());
                    System.out.println(responseCode);
                    return String.valueOf(responseCode);

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
                Toast.makeText(getApplicationContext(), "Response from server: " + result, Toast.LENGTH_LONG).show();
            }
        }
        PostClaimAsyncTask postClaimAsyncTask = new PostClaimAsyncTask();
        postClaimAsyncTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { //Image comes straight from take photo
            if (CURRENT_INDEX >= NumOfImages) { // Already attached 4 images
                new AlertDialog.Builder(ClaimActivity.this)
                        .setTitle("Images limits exceeded!")
                        .setMessage("You can only attach as many as 4 images. Do you want to remove all the previous attached images?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                resetImages();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else if (requestCode == TAKE_PHOTO) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bitmapOptions);
                    viewImage[CURRENT_INDEX].setImageBitmap(bitmap);
                    String path = android.os.Environment.getExternalStorageDirectory() + File.separator + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, file.getName() , file.getName());
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();

                        // Record the image's path for uploading
                        imageAddress[CURRENT_INDEX] = file.getName();
                        imageExist[CURRENT_INDEX++] = true;

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CHOOSE_PHOTO) { //Image comes from gallery
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                viewImage[CURRENT_INDEX].setImageBitmap(thumbnail);

                // Record the image's path for uploading
                imageAddress[CURRENT_INDEX] = picturePath;
                imageExist[CURRENT_INDEX++] = true;
            }
        }
    }

    private class UploadListener implements TransferListener {
        @Override
        public void onError(int id, Exception e) {
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            if (newState == TransferState.COMPLETED) {
                NumOfSubmittedImages++;
            }
            if (NumOfSubmittedImages == CURRENT_INDEX) {
                refresh();
            }
        }
    }

    /**
     * Private function to set up bottom bar
     */
    private void setupBottomBar() {
        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTabPosition(1);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_home) {
                    // The tab with id R.id.tab_favorites was selected,
                    // change your content accordingly.
                    Intent newIntent = new Intent(ClaimActivity.this, MainActivity.class);
                    startActivity(newIntent);
                } else if (tabId == R.id.tab_tracking) {
                    Intent newIntent = new Intent(ClaimActivity.this, TrackingActivity.class);
                    startActivity(newIntent);
                } else if (tabId == R.id.tab_message) {
                    Intent newIntent = new Intent(ClaimActivity.this, MessageActivity.class);
                    startActivity(newIntent);
                }
            }
        });
    }
}
