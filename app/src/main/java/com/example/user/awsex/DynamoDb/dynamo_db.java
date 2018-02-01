package com.example.user.awsex.DynamoDb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.StreamViewType;
import com.example.user.awsex.GoogleSignIn.GSignIn;
import com.example.user.awsex.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dynamo_db extends AppCompatActivity {

    AmazonDynamoDBClient ddb;
    Button submit,list,retrieve,bow;
    EditText bn,rat,title;
    static String tit;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String username,email,url,name;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamo_db);

        CognitoCachingCredentialsProvider credentialsProvider=new CognitoCachingCredentialsProvider(getApplicationContext(),"us-west-2:20e04e1d-cd9d-46ca-9305-93fe4f13f312", Regions.US_WEST_2);

        ddb=new AmazonDynamoDBClient(credentialsProvider);
        ddb.setRegion(Region.getRegion(Regions.US_WEST_2));

        DynamoDBMapper mapper=new DynamoDBMapper(ddb);




        submit=(Button)findViewById(R.id.submit);
        list=(Button)findViewById(R.id.list);
        title=(EditText)findViewById(R.id.title);
        bn=(EditText)findViewById(R.id.BookName);
        rat=(EditText)findViewById(R.id.Ratings);
        retrieve=(Button)findViewById(R.id.retrieve);
        bow=(Button)findViewById(R.id.bow);


        sharedPreferences = getSharedPreferences("User",MODE_PRIVATE);




        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DDB_MyTask().execute();
            }
        });

        bow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences= getSharedPreferences("User",MODE_PRIVATE);
                SharedPreferences.Editor editor =sharedPreferences.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(dynamo_db.this, GSignIn.class));
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Creating table");
                tit=title.getText().toString().replace(" ","");
                final String titl=tit;

                new Create_table_Task().execute();

            }
        });

        retrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new RetrieveItem_Task().execute();
            }
        });


    }
    public class DDB_MyTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {
            ListTablesResult listTablesResult=ddb.listTables();
            System.out.println("List of Tables : "+listTablesResult);

            return null;
        }
    }
    public class Create_table_Task extends  AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {


            String book=bn.getText().toString();
            String rating=rat.getText().toString();

            username=sharedPreferences.getString("dispname","").replace(" ","");
            email=sharedPreferences.getString("umail","").replace(" ","");
            url=sharedPreferences.getString("imgurl","").replace(" ","");
            name=sharedPreferences.getString("name","").replace(" ","");

            System.out.println("Books :  "+book+ "Ratings : "+rating);


//            List<AttributeDefinition> attributeDefinitions= new ArrayList<>();
//            attributeDefinitions.add(new AttributeDefinition().withAttributeName("Ratings").withAttributeType(ScalarAttributeType.N));
//            attributeDefinitions.add(new AttributeDefinition().withAttributeName("BookName").withAttributeType(ScalarAttributeType.S));
             // Three attributes cannot be defined

            //attributeDefinitions.add(new AttributeDefinition().withAttributeType("Author").withAttributeType(ScalarAttributeType.S));

            List<AttributeDefinition> attributeDefinitions= new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("Email").withAttributeType(ScalarAttributeType.S));
            attributeDefinitions.add(new AttributeDefinition().withAttributeName("Username").withAttributeType(ScalarAttributeType.S));


            List<KeySchemaElement> keySchemaElements=new ArrayList<>();
            keySchemaElements.add(new KeySchemaElement().withAttributeName("Email").withKeyType(KeyType.HASH));
            keySchemaElements.add(new KeySchemaElement().withAttributeName("Username").withKeyType(KeyType.RANGE));


//            List<KeySchemaElement> keySchemaElements=new ArrayList<>();
//            keySchemaElements.add(new KeySchemaElement().withAttributeName("Ratings").withKeyType(KeyType.HASH));
//            keySchemaElements.add(new KeySchemaElement().withAttributeName("BookName").withKeyType(KeyType.RANGE));
            // Must consists of the same number of attributes in attribute definintion
           // keySchemaElements.add(new KeySchemaElement().withAttributeName("Author").withKeyType(KeyType.RANGE));


            System.out.println("KEyAttributes  "+ keySchemaElements);

//            List<GlobalSecondaryIndex> globalSecondaryIndices=new ArrayList<>();
//            globalSecondaryIndices.add(new GlobalSecondaryIndex()
//                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1l).withWriteCapacityUnits(1l))
//                    .withIndexName("ISN")
//                    .withKeySchema(keySchemaElements.get(1))
//                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL)));
//            globalSecondaryIndices.add(new GlobalSecondaryIndex()
//                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1l).withWriteCapacityUnits(1l))
//                    .withIndexName("bow")
//                    .withKeySchema(keySchemaElements.get(1))
//                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL)));

            CreateTableRequest request=new CreateTableRequest().withTableName(username)
                    .withAttributeDefinitions(attributeDefinitions)
                    .withKeySchema(keySchemaElements)
                    .withStreamSpecification(new StreamSpecification().withStreamEnabled(Boolean.TRUE).withStreamViewType(StreamViewType.NEW_AND_OLD_IMAGES))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            try{
                CreateTableResult result=ddb.createTable(request);
                System.out.println("Result Of Table Creation : "+ result.getTableDescription()+ ddb.listTables());

                    new AddItem_Task().execute();


            }catch (final ResourceInUseException e){
                System.out.println("Error : "+ e.getErrorMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(dynamo_db.this, e.getErrorMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }


//            AttributeValue attributeValue=new AttributeValue();
//            attributeValue.setN(rating);
//            attributeValue.setS(book);
//
//
//
//
//            System.out.println("Attribs are : "+ attributeValue);
//




//            System.out.println("data are : "+ddb.getItem(tit,data));




            return null;
        }

    }
    public class AddItem_Task extends  AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... strings) {
            Map<String , AttributeValue> data=new HashMap<>();

            String book=bn.getText().toString();
            String rating=rat.getText().toString();


            data.put("Email",new AttributeValue().withS(email));
            data.put("Username",new AttributeValue().withS(username));
            data.put("Url",new AttributeValue().withS(url));
            data.put("First Name", new AttributeValue().withS(name));



            System.out.println("Map Values are : "+ data);
            List<String> tablees=ddb.listTables().getTableNames();

            System.out.println("Tables : "+tablees);


                PutItemRequest putItemRequest=new PutItemRequest().withTableName(username).withItem(data);
//            ddb.putItem(putItemRequest);
                try{
                    PutItemResult result=ddb.putItem(putItemRequest);
                    System.out.println("Item Success : "+result.getAttributes());
                }catch (Exception e){

                    new AddItem_Task().execute();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(dynamo_db.this, "Press the Button again...", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }







            return null;
        }
    }
    public class RetrieveItem_Task extends  AsyncTask<String, Integer, String >{

        @Override
        protected String doInBackground(String... strings) {

            List<String> gi= new ArrayList<>();
            gi.add("Email");
            gi.add("Username");
            gi.add("Url");
            gi.add("First Name");

            username=sharedPreferences.getString("dispname","").replace(" ","");
            email=sharedPreferences.getString("umail","");

            System.out.println("List : "+ gi + "\n Username : "+username  + email);
//
            Map<String, AttributeValue> data=new HashMap<>();
            data.put("Email",new AttributeValue().withS(email));
            data.put("Username",new AttributeValue().withS(username));


            GetItemRequest getItemRequest= new GetItemRequest().withTableName(username).withAttributesToGet(gi).withConsistentRead(Boolean.TRUE);
            getItemRequest.setKey(data);
            System.out.println("ItemRequest KEy :  "+getItemRequest.getKey());
            GetItemResult result =ddb.getItem(getItemRequest);
            System.out.println("The retrieval Result : "+ result.getItem());



//            ScanRequest scanRequest=new ScanRequest().withTableName(username).withSelect(email);
//            ScanResult scanResult=ddb.scan(scanRequest);
//
//            System.out.println("Scan Results : "+ scanResult.getCount());
//
//            Map<String, AttributeValue> userdata=  scanResult.getItems().get(0);
//
//            System.out.println("the User Map data is : "+ userdata);
//
//            String retrievedUname= String.valueOf(userdata.get("Username"));
//            String retrievedEmail=String.valueOf(userdata.get("Email"));
//            String retreivedurl=String.valueOf(userdata.get("Url"));
//            String retrievedFN=String.valueOf(userdata.get("First Name"));
//
//            System.out.println("Retrieved User name : "+ retrievedUname + retrievedEmail+retrievedFN+retreivedurl);





            return null;
        }
    }

}
