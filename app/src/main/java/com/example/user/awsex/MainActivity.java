package com.example.user.awsex;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.regions.Regions;

public class MainActivity extends AppCompatActivity {
    Button b1,b2;
    EditText e1,e2;
    CognitoCachingCredentialsProvider credentials;
    CognitoSyncManager syncClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1=(Button)findViewById(R.id.button);
        b2=(Button)findViewById(R.id.button2);
        e1=(EditText)findViewById(R.id.editText2);
        e2=(EditText)findViewById(R.id.editText);


         credentials= new CognitoCachingCredentialsProvider(getApplicationContext(), "ap-south-1:30b9dbde-1fa0-4636-abc7-161337917185", Regions.AP_SOUTH_1);


      syncClient= new CognitoSyncManager(getApplicationContext(),Regions.AP_SOUTH_1,credentials);


        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Signup.class));
            }
        });
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!e1.getText().toString().isEmpty()){
                    if(!e2.getText().toString().isEmpty()){
                        new Mytask().execute();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Enter the credentials", Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                    Toast.makeText(MainActivity.this, "Enter the credentials", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    public class Mytask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {
            String username=null;
            username =e1.getText().toString();
            String pass=e2.getText().toString();
            System.out.println("Username :"+ username);
            Dataset dataset=syncClient.openOrCreateDataset(username);
            System.out.println("Dataset : "+dataset.get("username"));
            if (dataset.get("username")== null) {
                System.out.println(" Invalid Credentials");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                });

            }
            else{

                if(dataset.get("username").toString().equals(username)){
                    if(dataset.get("pass").toString().equals(pass)){
                        System.out.println(dataset.get("username")+dataset.get("email")+dataset.get("pass"));
                        startActivity(new Intent(MainActivity.this,Main2Activity.class));
                        finish();

                    }

                }
                else{
                    Toast.makeText(MainActivity.this, "Invalid Credentials....", Toast.LENGTH_SHORT).show();
                }

            }

            return null;
        }
    }
}
