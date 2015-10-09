package com.example.yang.dailyselfie;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends ListActivity {

    // Camera request code
    static final int REQUEST_TAKE_PHOTO = 1;

    // Adapter
    private SelfieAdapter mAdapter;

    private AlarmManager mAlarmManager;
    private Intent mNotificationReceiverIntent;
    private PendingIntent mNotificationReceiverPendingIntent;
    private static final int AlarmInterval = 2 * 60 * 1000;

    // Get the storage directory
    private File storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);

    private Bitmap imageBitmap;
    // Photo path
    private String mCurrentPhotoPath;

    private final int THUMBSCALEDOWN = 5;

    private static String TAG = "DailySelfie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start alarm
        startAlarm();

        // Set up adapter
        mAdapter = new SelfieAdapter(getApplicationContext());

        // Get photos
        getPhotos();

        ListView photosListView = getListView();

        // Somehow, this doesn't work. I mean, it worked this morning!!
//        photosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.i(TAG, "onItemClick called");
//
//                Selfie selfie = (Selfie) mAdapter.getItem(i);
//                String photoPath = selfie.getPath();
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + photoPath), "image/*");
//                startActivity(intent);
//            }
//        });

        setListAdapter(mAdapter);

        // Register list items to show context menu
        registerForContextMenu(photosListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case  R.id.action_camera:
                dispatchTakePictureIntent();
                return true;

            case R.id.delete_all:
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to delete all photos?")
                        .setTitle("Delete All Photos")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete all files
                                for (File file : storageDir.listFiles()) {
                                    file.delete();
                                }

                                mAdapter.removeAllViews();
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Create context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    // Process clicks on menu items
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final int position = menuInfo.position;
        final Selfie selfie = (Selfie)mAdapter.getItem(position);

        switch (item.getItemId()){

            case R.id.delete_item:
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to delete this photo?")
                        .setTitle("Delete Photo")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete the file
                                File file = new File(selfie.getPath());
                                file.delete();

                                mAdapter.delete(position);
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){

            // Save picture
            galleryAddPic();

            // Get image bitmap
            File imgFile = new File(mCurrentPhotoPath);
            if(imgFile.exists()){
                imageBitmap = rescale(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
            }

            // Add selfie to adapter
            Selfie selfie = new Selfie(imageBitmap, imgFile.getName(), mCurrentPhotoPath,
                    new Date(imgFile.lastModified()));
            mAdapter.add(selfie);

        }
    }

    // Start alarm
    protected void startAlarm(){
        // Get the AlarmManagerService
        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        // Create an Intent to broadcast to the AlarmNotificationReceiver
        mNotificationReceiverIntent = new Intent(this, AlarmNotificationReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                this, 0, mNotificationReceiverIntent, 0
        );

        // Set up repeating alarm
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + AlarmInterval,
                AlarmInterval, mNotificationReceiverPendingIntent);
    }

    // Take photo
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // Save the photo
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void getPhotos(){


        if (storageDir.exists()) {
            mAdapter.removeAllViews();
            for (File file : storageDir.listFiles()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                // Check if the bitmap is null
                if (bitmap != null){
                    mAdapter.add(
                            new Selfie(
                                    rescale(bitmap),
                                    file.getName(),
                                    file.getAbsolutePath(),
                                    new Date(file.lastModified())
                            )
                    );
                }
            }

        }

    }

    private Bitmap rescale(Bitmap bitmap){
        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/ THUMBSCALEDOWN, bitmap.getHeight()/ THUMBSCALEDOWN, false);
    }
}
