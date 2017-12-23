package com.example.user.awsex;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.user.awsex.Rekognition.Img_Compare;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends AppCompatActivity {

    Uri imageUri,selectedImageUri;
    private static final int PICK_Camera_IMAGE=2;
    private static final int PICK_IMAGE=1;
    String Path;
    File IMG;
    ArrayList<String> key_list;
    ListView listView;
    ArrayAdapter<String> adapter;
    String itemVal;
    ImageView img;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncManager;
    Button b1,b2,b3;
    AmazonS3 s3;
    EditText keyedit;
    TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        img=(ImageView)findViewById(R.id.image);
        b1=(Button)findViewById(R.id.button4);
        b2=(Button)findViewById(R.id.download);
        b3=(Button)findViewById(R.id.compare);
        keyedit=(EditText)findViewById(R.id.editText3);
        credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),
                "ap-south-1:30b9dbde-1fa0-4636-abc7-161337917185",
                Regions.AP_SOUTH_1);


        s3=new AmazonS3Client(credentialsProvider);
        listView=(ListView)findViewById(R.id.keylist);
       // new MyTask_Listing().execute();





        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 99);
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri==null){
                    Toast.makeText(Main2Activity.this, "Select a pic", Toast.LENGTH_SHORT).show();
                }
                else if(keyedit.getText().toString().isEmpty()){
                    Toast.makeText(Main2Activity.this, "Enter the key to upload ", Toast.LENGTH_SHORT).show();
                }
                else{
                    new MyTask_S3_IMG_Upload().execute();
                }

            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(keyedit.getText().toString().isEmpty()){
                    Toast.makeText(Main2Activity.this, "Enter the key to download", Toast.LENGTH_SHORT).show();
                }
                new MyTask_S3_IMG_Down().execute();
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Main2Activity.this, Img_Compare.class));
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 99 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            imageUri=uri;
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Log.d(TAG, String.valueOf(bitmap));
            img.setImageBitmap(bitmap);
            Path = ImageFilePath.getPath(getApplicationContext(),uri);
            System.out.println("PATH IS "+Path);
            IMG = new File(Path);

        }
    }
    public class MyTask_S3_IMG_Upload extends AsyncTask<String , Integer, String>{



        @Override
        protected String doInBackground(String... strings) {
            s3.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
            String key=keyedit.getText().toString();
            transferUtility =new TransferUtility(s3,getBaseContext());
            TransferObserver transferObserver=transferUtility.upload(
                    "myfirstappbow",
                    key,
                    new File(Path)
            );
            transferObserver.setTransferListener(new TransferListener(){
                @Override
                public void onStateChanged(int id, TransferState state) {
                    // do something
                    Log.d("log", "state changed. id = "+id+"\tstate = "+state);
                    System.out.println(" state changed");
                }
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    int percentage = (int) (bytesCurrent/bytesTotal * 100);

                    //Display percentage transfered to user
                    publishProgress(percentage);
                    Log.d("log", "onProgressChanged = "+percentage);
                    System.out.println(" onProgressChanged "+percentage);

                    if(percentage==100){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Main2Activity.this, "FIle Uploaded", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                @Override
                public void onError(int id, Exception ex) {
                    // do something
                    Log.d("log", "error in uploading. id = "+id+"\nException = "+ex);
                    System.out.println(" ERROR "+ex);
                }
            });
            return null;

        }
    }
    public class MyTask_S3_IMG_Down extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings) {
            String key=keyedit.getText().toString();
            s3.setRegion(Region.getRegion(Regions.AP_SOUTH_1));
            TransferUtility transferUtility = new TransferUtility(s3, getBaseContext());
            TransferObserver observer = transferUtility.download(
                    "myfirstappbow",      /* The bucket to upload to */
                    key,     /* The key for the uploaded object */
                    new File("/storage/emulated/0/AWS/"+key+".jpg")        /* The file where the data to upload exists */
            );
            System.out.println("Tr Util");
            observer.setTransferListener(new TransferListener(){
                @Override
                public void onStateChanged(int id, TransferState state) {
                    // do something
                    Log.d("log", "state changed. id = "+id+"\tstate = "+state);
                    System.out.println(" state changed");
                }
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Toast.makeText(Main2Activity.this, "File Downloaded \n Available in internalstorage/aws/", Toast.LENGTH_SHORT).show();
                         }
                     });

                }
                @Override
                public void onError(int id, Exception ex) {
                    System.out.println("EXCEPTION "+ex);
                }
            });

            return null;
        }

    }
    public class MyTask_Listing extends AsyncTask<Integer, String, Integer>{

        @Override
        protected Integer doInBackground(Integer... integers) {
            key_list=new ArrayList<>();
            ObjectListing objectListing=s3.listObjects("myfirstappbow");
            for(S3ObjectSummary objectSummary:objectListing.getObjectSummaries()){
                System.out.println("Key :"+objectSummary.getKey().toString());
                key_list.add(objectSummary.getKey().toString());
            }
            adapter =new ArrayAdapter<String>(getApplicationContext(),R.layout.list_item,R.id.textitem,key_list);
            listView.setAdapter(adapter);
            return null;
        }
    }

}
