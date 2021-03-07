package com.example.download_from_web;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.SyncStateContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtURI;
    private Button btnDownload;

    private ImageView imgPoster;

    private Button btnOpen;


    public static final String APP_PREFS_NAME = SyncStateContract.Constants.class.getPackage().getName();
    public static final String APP_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/data/com.example.download_from_web/" + APP_PREFS_NAME + "/picasso-cache/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();


    }

    private void initViews() {

        txtURI = findViewById(R.id.txtURI);
        btnDownload = findViewById(R.id.btnDownload);

        imgPoster = findViewById(R.id.imgPoster);

        btnOpen = findViewById(R.id.btnOpen);

        btnDownload.setOnClickListener(this);

        btnOpen.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.btnDownload: {

                assureDownloadFile();
                break;
               }
            case R.id.btnOpen:
                openFile();
                break;
        }

    }


    private void assureDownloadFile() {

        //Начиная с версии SDK 23 нужно явно запрашивать разрешения
        //у пользователя на доступ к разным папкам и ресурсам
        if(Build.VERSION.SDK_INT > 22)
        {
            //Функция checkPermission определяет есть ли у данной программы (у пакета)
            //уже доступ к заданному ресурсу

            //Если доступ пакету уже дан - вызываем функцию
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                downloadResource();
            }
            //Если доступа нет - запрашиваем
            else
            {
                //Показать окно запроса разрешений
                ActivityCompat.requestPermissions(this, new String[] {  Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
            }
        }
        else
        {
            downloadResource();
        }
    }

    //TODO: Move this function out to the service library
    private void downloadResource() {

        //Получаем доступ к одному из системных сервисов
        //в нашем случае к DownloadService, который управляется через класс
        //DownloadManager
        //Так как getSystemService возвращает Object, то его нужно явно приводить
        //к нужному классу
        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        //Адрес для скачивание постера
        Uri resourceUri = Uri.parse(txtURI.getText().toString());

        //Это запрос на скачивание ресурса из Интернет
        DownloadManager.Request downloadRequest = new DownloadManager.Request(resourceUri);

        //Установить название файла
        downloadRequest.setTitle("my_download");
        downloadRequest.setDescription("Downloading poster");
        //Загружаем файл в папку "Загрузки"
        downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "my_files");

        //imgPoster.setNextFocusDownId(Integer.parseInt("1"));

        //imgPoster.setImageURI(resourceUri);

        //Этот метод возвращает id вашей загрузки из таблицы загрузок
        long myDownloadID = dm.enqueue(downloadRequest);

        //imgPoster = dm.openDownloadedFile(1);

        //Запускаем цикл ожидания закачки

        boolean isDownloadDone = false;

        while(!isDownloadDone)
        {
            //Ищем из списка всех загрузок системы нашу загрузку
            Cursor myDownloadSearchCursor = dm.query(new DownloadManager.Query().setFilterById(myDownloadID));

            //Если наша загрузка найдена в списке загрузок
            if(myDownloadSearchCursor.moveToFirst())
            {
                //Читаем текущий статус-код нашей загрузки
                int downloadStatusID = myDownloadSearchCursor.getInt(myDownloadSearchCursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                  switch (downloadStatusID)
                {
                    case DownloadManager.STATUS_FAILED:
                    case DownloadManager.STATUS_PAUSED:
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING: { break; }
                    //Файл загружен
                    case DownloadManager.STATUS_SUCCESSFUL: {

                        Toast.makeText(this, "File downloaded", Toast.LENGTH_LONG).show();

                        isDownloadDone = true;
                        break;
                    }

                }
            }
        }
    }

    //Когда пользователь выдал или не выдал нам права на запись в папку
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Если доступ дан
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            downloadResource();
        }
    }
    private void openFile() {

//        imgPoster.setImageURI(Uri.parse(txtURI.getText().toString()));
       // Uri.fromFile( new File( SyncStateContract.Constants.CONTENT_DIRECTORY + this));


    }
}