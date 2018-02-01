package com.example.user.awsex.Rekognition;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Img_Compare extends AppCompatActivity {
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3Client s3;
    CompareFacesResult result;
    AmazonS3 ss3;
    TextView tv;
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
       tv=(TextView)findViewById(R.id.textView2);

        new Rcg_Process().execute();


    }
    public class Rcg_Process extends AsyncTask<Integer, String, Integer>{

        @Override
        protected Integer                doInBackground(Integer... integers) {
            AmazonRekognitionClient rekognitionClient=new AmazonRekognitionClient(credentialsProvider);
            rekognitionClient.setRegion(Region.getRegion(Regions.US_WEST_2));

            Image image1=new Image();
            Image image2=new Image();
            Image detectImage=new Image();
            S3Object s3Object=new S3Object();
            s3Object.setBucket("awsreg");

            File file=new File("/storage/emulated/0/IRIS/MI_08012018_1026.jpg");
            byte[] bytes=  new byte[(int) file.length()];
            try {
                FileInputStream fis=new FileInputStream(file);
                fis.read(bytes);
                fis.close();
                System.out.println("The bytes stream is : "+bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }




            image1.setS3Object(s3Object.withName("individual"));
            image2.setS3Object(s3Object.withName("group"));

            detectImage.setBytes(ByteBuffer.wrap(bytes));



            DetectLabelsRequest labelsRequest=new DetectLabelsRequest(detectImage).withMinConfidence(60f);


            CompareFacesRequest compareFacesRequest=new CompareFacesRequest().withSourceImage(image1)
                    .withTargetImage(image2).withSimilarityThreshold(75f);
            System.out.println("Comparing bowwwwwww");

//            CompareFacesRequest compareFacesRequest=new CompareFacesRequest()
//                    .withSourceImage(new Image().withS3Object(new S3Object().withName("cmp1").withBucket("awsreg")))
//                    .withTargetImage(new Image().withS3Object(new S3Object().withName("cmp2").withBucket("awsreg")))
//                    .withSimilarityThreshold(78f);
            System.out.println("Compared : "+compareFacesRequest);


            try {
               result=rekognitionClient.compareFaces(compareFacesRequest);
                System.out.println("Result of Comparison : "+ result.getFaceMatches());

                System.out.println("Comparison     "+ result.withFaceMatches());

                final DetectLabelsResult detectLabelsResult=rekognitionClient.detectLabels(labelsRequest);
                System.out.println("Detection Result : "+detectLabelsResult.getLabels());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.append(detectLabelsResult.toString());
                    }
                });

            }catch (InvalidS3ObjectException e){
                System.out.println("Bowww Exception : "+ e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            tv.append(result.toString());

           // Toast.makeText(Img_Compare.this, result.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
