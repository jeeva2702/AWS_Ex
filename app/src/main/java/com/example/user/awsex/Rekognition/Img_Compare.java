package com.example.user.awsex.Rekognition;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.user.awsex.R;

public class Img_Compare extends AppCompatActivity {
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3Client s3;
    AmazonS3 ss3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img__compare);



        credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),"ap-south-1:30b9dbde-1fa0-4636-abc7-161337917185", Regions.AP_SOUTH_1);
        s3=new AmazonS3Client(credentialsProvider);
        ObjectListing objectListing=s3.listObjects("myfirstappbow");
        for(S3ObjectSummary objectSummary:objectListing.getObjectSummaries()){
            System.out.println("Key :"+objectSummary.getKey().toString());
//            key_list.add(objectSummary.getKey().toString());
        }



        new Rcg_Process().execute();


    }
    public class Rcg_Process extends AsyncTask<Integer, String, Integer>{

        @Override
        protected Integer doInBackground(Integer... integers) {
            AmazonRekognitionClient rekognitionClient=new AmazonRekognitionClient(credentialsProvider);

            CompareFacesRequest compareFacesRequest=new CompareFacesRequest()
                    .withSourceImage(new Image().withS3Object(new S3Object().withName("cmp1").withBucket("myfirstappbow")))
                    .withTargetImage(new Image().withS3Object(new S3Object().withName("cmp2").withBucket("myfirstappbow")))
                    .withSimilarityThreshold(78f);
            System.out.println("Compared : "+compareFacesRequest);
            try {
                CompareFacesResult result=rekognitionClient.compareFaces(compareFacesRequest);
                System.out.println("Result of Comparison : "+ result);
            }catch (Exception e){
                System.out.println("Exception : "+ e);
            }
            return null;
        }
    }
}
