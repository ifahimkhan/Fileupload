package com.fahim.fileuploadphp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fahim.fileuploadphp.Utils.ProgressRequest;
import com.fahim.fileuploadphp.Utils.UploadCallBacks;
import com.fahim.fileuploadphp.remote.RetrofitClient;
import com.fahim.fileuploadphp.remote.UploadApi;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements UploadCallBacks {

    public static final String BaseUrl = "http://192.168.0.105/androidphp/";
    private static final int PICK_FILE_REQUEST = 100;
    Uri selectedFileUri;
    ProgressDialog progressDialog;

    UploadApi uploadApiService;

    private UploadApi getUploadApiService() {
        return RetrofitClient.getClient(BaseUrl).create(UploadApi.class);
    }

    ImageView imageView;
    Button button;
    TextView txt_filename;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            String mimeType = getContentResolver().getType(selectedFileUri);
            String[] arrOfStrMimeType = mimeType.split("/", 2);
            if (selectedFileUri != null && !selectedFileUri.getPath().isEmpty()) {
                Cursor returnCursor =
                        getContentResolver().query(selectedFileUri, null, null, null, null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();

                if (arrOfStrMimeType[0].toString().equals("image")) {
                    imageView.setImageURI(selectedFileUri);
                } else imageView.setImageResource(R.drawable.ic_attach_file_black_24dp);


                txt_filename.setText("FileName: " + returnCursor.getString(nameIndex)
                        + "\n FileSize: " + (returnCursor.getLong(sizeIndex) / 1000) + "KB");
            } else Toast.makeText(this, "Cannot upload File", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button_upload);
        imageView = (ImageView) findViewById(R.id.image_view);
        txt_filename = (TextView) findViewById(R.id.txt_filename);
        uploadApiService = getUploadApiService();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

    }

    private void uploadFile() {

        if (selectedFileUri != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Uploading...");
            progressDialog.setMax(100);
            progressDialog.setCancelable(false);
            progressDialog.show();

            File file = FileUtils.getFile(this, selectedFileUri);
            ProgressRequest request = new ProgressRequest(file, this);

            final MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), request);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadApiService.uploadFile(body)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }).start();
        }
    }

    private void chooseFile() {
        Intent getContentFile = Intent.createChooser(FileUtils.createGetContentIntent(), "select a file");
        startActivityForResult(getContentFile, PICK_FILE_REQUEST);

    }

    @Override
    public void onProgressUpdate(int percentage) {
        progressDialog.setProgress(percentage);
    }
}

