package com.example.user.awsex.Polly;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.example.user.awsex.R;

import java.util.HashMap;

public class awspolly extends AppCompatActivity {

    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonPollyPresigningClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_awspolly);

        credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),
                "us-west-2:20e04e1d-cd9d-46ca-9305-93fe4f13f312",
                Regions.US_WEST_2);




       new Polly_Task().execute();


    }
    public class Polly_Task extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {
            System.out.println("Bowwwwww"+ credentialsProvider.getCredentials());



//            DescribeVoicesRequest voicesRequest= new DescribeVoicesRequest();
//            DescribeVoicesResult voicesResult=client.describeVoices(voicesRequest);
//            System.out.println("The voices Result are : "+ voicesResult.getVoices());
            return null;
        }
    }
}
