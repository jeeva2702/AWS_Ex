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
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.InvalidS3ObjectException;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
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



        credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),"us-west-2:20e04e1d-cd9d-46ca-9305-93fe4f13f312", Regions.US_WEST_2);
        s3=new AmazonS3Client(credentialsProvider);
//        ObjectListing objectListing=s3.listObjects("myfirstappbow");
//        for(S3ObjectSummary objectSummary:objectListing.getObjectSummaries()){
//            System.out.println("Key :"+objectSummary.getKey().toString());
////            key_list.add(objectSummary.getKey().toString());
//        }
//        ListObjectsV2Result objectListing1=s3.listObjectsV2("myfirstappbow");
  //      System.out.println("key count :"+ objectListing1.getKeyCount());


        new Rcg_Process().execute();


    }
    public class Rcg_Process extends AsyncTask<Integer, String, Integer>{

        @Override
        protected Integer doInBackground(Integer... integers) {
            AmazonRekognitionClient rekognitionClient=new AmazonRekognitionClient(credentialsProvider);

            Image image1=new Image();
            Image image2=new Image();
            S3Object s3Object=new S3Object();
            s3Object.setBucket("awsreg");




            image1.setS3Object(s3Object.withName("groimg"));
            image2.setS3Object(s3Object.withName("indimg"));


            DetectLabelsRequest labelsRequest=new DetectLabelsRequest(image1).withMinConfidence(60f);


//            CompareFacesRequest compareFacesRequest=new CompareFacesRequest().withSourceImage(new Image().withS3Object(s3Object.withName("cmp1")))
//                    .withTargetImage(new Image().withS3Object(s3Object.withName("cmp2"))).withSimilarityThreshold(75f);
            CompareFacesRequest compareFacesRequest=new CompareFacesRequest().withSourceImage(image1)
                    .withTargetImage(image2).withSimilarityThreshold(75f);
            System.out.println("Comparing bowwwwwww");

//            CompareFacesRequest compareFacesRequest=new CompareFacesRequest()
//                    .withSourceImage(new Image().withS3Object(new S3Object().withName("cmp1").withBucket("awsreg")))
//                    .withTargetImage(new Image().withS3Object(new S3Object().withName("cmp2").withBucket("awsreg")))
//                    .withSimilarityThreshold(78f);
            System.out.println("Compared : "+compareFacesRequest);


            try {
//                CompareFacesResult result=rekognitionClient.compareFaces(compareFacesRequest);
//                System.out.println("Result of Comparison : "+ result);
                DetectLabelsResult detectLabelsResult=rekognitionClient.detectLabels(labelsRequest);
                System.out.println("Detection Result : "+detectLabelsResult.getLabels());
            }catch (InvalidS3ObjectException e){
                System.out.println("Bowww Exception : "+ e);
            }

            return null;
        }
    }
}
