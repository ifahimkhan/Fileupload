package com.fahim.fileuploadphp.remote;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by root on 22/7/18.
 */

public interface UploadApi {
    @Multipart
    @POST("upload.php")
    Call<String> uploadFile(@Part MultipartBody.Part file);
}
