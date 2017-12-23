package com.example.user.awsex;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Signup extends AppCompatActivity {


    EditText n,d,p,cp,em;
    CognitoCachingCredentialsProvider credentialsProvider;
    CognitoSyncManager syncManager;

    Button b;
    String ud,ue,up,cm,tp,ye,se;

    Uri imageUri,selectedImageUri;
    private static final int PICK_Camera_IMAGE=2;
    private static final int PICK_IMAGE=1;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);



        n=(EditText)findViewById(R.id.n);
        d=(EditText)findViewById(R.id.d);
        em=(EditText)findViewById(R.id.em);
        p=(EditText)findViewById(R.id.p);
        cp=(EditText)findViewById(R.id.cp);
        b=(Button)findViewById(R.id.button3);

      credentialsProvider =new CognitoCachingCredentialsProvider(
                getApplicationContext(),
              "ap-south-1:30b9dbde-1fa0-4636-abc7-161337917185",
                Regions.AP_SOUTH_1

        );
      syncManager =new CognitoSyncManager(getApplicationContext(),Regions.AP_SOUTH_1,credentialsProvider);




        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(n.getText().toString().isEmpty()&&em.getText().toString().isEmpty()&&p.getText().toString().isEmpty()){
                    Toast.makeText(Signup.this, "Enter all the credentials", Toast.LENGTH_SHORT).show();
                }else{
                    new MyTask().execute();
                }


        }});


    }

//
    public class MyTask extends AsyncTask<String , Integer, String > {
        @Override
        protected void onPreExecute() {


            super.onPreExecute();
            //progressDialog=ProgressDialog.show(getApplicationContext(),"Message","Signing in....");
        }
        @Override
        protected String doInBackground(String... params) {
            final String username=n.getText().toString();
            String email=em.getText().toString();
            String pass=p.getText().toString();

            Dataset dataset=syncManager.openOrCreateDataset(username);
            dataset.put("username",username);
            dataset.put("email",email);
            dataset.put("pass",pass);
            dataset.synchronize(new DefaultSyncCallback(){
                @Override
                public void onSuccess(Dataset dataset, List<Record> updatedRecords) {

                    System.out.println("creds are"+dataset.get("username")+dataset.get("email")+dataset.get("pass"));
                    Intent intent = new Intent(Signup.this, MainActivity.class);

                    startActivity(intent);
                    finish();
                    super.onSuccess(dataset, updatedRecords);
                }
            });

            return null;
        }



        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);

           // progressDialog.dismiss();
        }
    }

}
