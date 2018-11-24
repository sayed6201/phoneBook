package com.sayed.phonrbook;

import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class InitialActivity extends AppCompatActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mTextMessage;
    View v;
    LinearLayout contactViewLayout;
    RelativeLayout contactAddLayout;

    private static final String TAG = "ContentProviderDemo";

    //private int MY_PERMISSIONS_REQUEST_READ_CONTACTS=20;

    private boolean firstTimeLoaded=false;

    private TextView textViewQueryResult;
    private Button buttonLoadData, buttonAddContact,buttonRemoveContact,buttonUpdateContact;

    private ContentResolver contentResolver;

    private EditText editTextContactName;
    private CursorLoader mContactsLoader;

    private String[] mColumnProjection = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.CONTACT_STATUS,
            ContactsContract.Contacts.HAS_PHONE_NUMBER};

    private String mSelectionCluse = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " = ?";

    private String[] mSelectionArguments = new String[]{"Ajay"};

    private String mOrderBy = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS=20;
    private int MY_PERMISSION_REQUEST_WRITE_CONTACTS=30;


    private ArrayList<Contacts> contactList;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText("Home");
                    contactViewLayout.setVisibility(View.VISIBLE);
                    contactAddLayout.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText("Dashboard");
                    contactViewLayout.setVisibility(View.INVISIBLE);
                    contactAddLayout.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText("notification");
                    contactViewLayout.setVisibility(View.INVISIBLE);
                    contactAddLayout.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        // set an exit transition
        getWindow().setExitTransition(new Explode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);



        contactAddLayout = findViewById(R.id.contact_add_layout);
        contactViewLayout = findViewById(R.id.contact_test_layout);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        textViewQueryResult = (TextView) findViewById(R.id.textViewQueryResult);

        editTextContactName=(EditText)findViewById(R.id.editTextContactName);

        buttonLoadData = (Button) findViewById(R.id.buttonLoadData);

        buttonAddContact=(Button)findViewById(R.id.buttonAddContact);
        buttonRemoveContact=(Button)findViewById(R.id.buttonRemoveContact);
        buttonUpdateContact=(Button)findViewById(R.id.buttonUpdateContact);



        buttonLoadData.setOnClickListener(this);

        buttonAddContact.setOnClickListener(this);
        buttonRemoveContact.setOnClickListener(this);
        buttonUpdateContact.setOnClickListener(this);

        contentResolver=getContentResolver();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(i==1){
            return  new CursorLoader(this,ContactsContract.Contacts.CONTENT_URI,null, null,null,null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        contactList = new ArrayList<>();

        if(cursor!=null && cursor.getCount()>0){

            while (cursor.moveToNext()){

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                ArrayList<String> num = new ArrayList<>();



                Cursor contact = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+id,null,null);
                if(contact != null){
                    while (contact.moveToNext()){
                        num.add(contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
//                        String email = contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
//                        if (email.equals("")) email = "NO_EMAIL";
                    }
                    if(num.size() == 0) num.add("NO_NUMBER");
                }

                Contacts contactObj = new Contacts(name,num);
                contactList.add(contactObj);

                textViewQueryResult.append(contactObj.toString()+"\n");

                contact.close();
            }

        }else{
            textViewQueryResult.setText("No Contacts in device");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLoadData:
                loadContacts();
                break;
            case R.id.buttonAddContact: addContact();
                break;
            case R.id.buttonRemoveContact:deleteContact();
                break;
            case R.id.buttonUpdateContact: modifyCotact();
                break;
            default:
                break;
        }
    }

    private void insertContacts(){

        String newName=editTextContactName.getText().toString();
        if(newName!=null && !newName.equals("") && newName.length()!=0){
            ArrayList<ContentProviderOperation> cops=new ArrayList<ContentProviderOperation>();

            cops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,"accountname@gmail.com")
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "com.google")
                    .build());
            cops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, editTextContactName.getText().toString())
                    .build());

            try{
                getContentResolver().applyBatch(ContactsContract.AUTHORITY,cops);
            }catch (Exception exception){
                Log.i(TAG,exception.getMessage());
                Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addContact() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            insertContacts();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_CONTACTS)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Needs Contacts write permission",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(InitialActivity.this, new String[]{android.Manifest.permission.WRITE_CONTACTS},MY_PERMISSION_REQUEST_WRITE_CONTACTS);
                            }
                        }).show();
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSION_REQUEST_WRITE_CONTACTS);
            }
        }
    }


    private void updateContact(){

        String [] updateValue=editTextContactName.getText().toString().split(" ");
        ContentProviderResult[] result=null;

        String targetString=null;
        String newString=null;
        if(updateValue.length==2){
            targetString=updateValue[0];
            newString=updateValue[1];
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                if(newString!=null && !newString.equals("") && newString.length()!=0)
                {
                    String where= ContactsContract.RawContacts._ID + " = ? ";
                    String [] params= new String[] {targetString};
                    ContentResolver contentResolver=getContentResolver();
                    ContentValues contentValues=new ContentValues();
                    contentValues.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,newString);
                    // UPDATE <table_name> SET column1 = value1, column2 = value2 where column3 = selection_value
                    contentResolver.update(ContactsContract.RawContacts.CONTENT_URI,contentValues, where,params);
                }
            }
        }
    }

    private void modifyCotact(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            updateContact();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_CONTACTS)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Needs Contacts write permission",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(InitialActivity.this, new String[]{android.Manifest.permission.WRITE_CONTACTS},MY_PERMISSION_REQUEST_WRITE_CONTACTS);
                            }
                        }).show();
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSION_REQUEST_WRITE_CONTACTS);
            }
        }
    }

    private void removeContacts(){
        String newName=editTextContactName.getText().toString();
        if(newName!=null && !newName.equals("") && newName.length()!=0){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                //display_name = '<entered_value>'
                String whereClause=ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+ " = '"+editTextContactName.getText().toString()+"'";
                //DELETE FROM <table_name> where column1 = selection_value
                getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,whereClause,null);
            }
        }

    }

    private void deleteContact(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_CONTACTS)==PackageManager.PERMISSION_GRANTED){
            removeContacts();
        }else{
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_CONTACTS)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Needs Contacts write permission",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(InitialActivity.this, new String[]{android.Manifest.permission.WRITE_CONTACTS},MY_PERMISSION_REQUEST_WRITE_CONTACTS);
                            }
                        }).show();
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSION_REQUEST_WRITE_CONTACTS);
            }
        }
    }

    private void addContactsViaIntents(){
        String tempContactText=editTextContactName.getText().toString();
        if(tempContactText!=null && !tempContactText.equals("") && tempContactText.length()>0 ){
            Intent intent=new Intent(ContactsContract.Intents.Insert.ACTION);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.NAME,tempContactText);
            startActivity(intent);
        }
    }


    private void loadContacts() {

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED){
            Log.i(TAG,"Permisssion is granted");
            if (firstTimeLoaded == false) {
                getLoaderManager().initLoader(1, null, this);
                firstTimeLoaded = true;
            } else {
                getLoaderManager().restartLoader(1, null, this);
            }
        }else{
            Log.i(TAG,"Permisssion is not granted");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_CONTACTS)){
                Log.i(TAG,"Permisssion is not granted, hence showing rationale");
                Snackbar.make(findViewById(android.R.id.content),"Need permission for loading data",Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(InitialActivity.this,new String[]{android.Manifest.permission.READ_CONTACTS},MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                            }
                        }).show();
            }else {
                Log.i(TAG,"Permisssion being requested for first time");
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_CONTACTS},MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }



}
