package com.weily.test;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.mystery0.ipicturechooser.iPictureChooser;
import com.mystery0.ipicturechooser.iPictureChooserListener;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private static final int INFILE_CODE = 233;
    private static final int PERMISSION = 322;
    private iPictureChooser pictureChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button button = (Button) findViewById(R.id.button);
        final View view=findViewById(R.id.coordinatorLayout);
        pictureChooser = (iPictureChooser) findViewById(R.id.picture);
        pictureChooser.setDataList(R.drawable.ic_add, new iPictureChooserListener()
        {
            @Override
            public void MainClick()
            {
                if (pictureChooser.getList().size() >= 5)
                {
                    Toast.makeText(MainActivity.this, getString(R.string.hint_out), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, iPictureChooser.REQUEST_IMG_CHOOSE);
            }
        });
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(pictureChooser.getList().size()!=0)
                {
                    replace();
                }else
                {
                    Snackbar.make(view,getString(R.string.hint_null),Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_edit:
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("*/*");
                intent1.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent1, INFILE_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case INFILE_CODE:
                if (data != null)
                {
                    String path = FileDo.getFloder(MainActivity.this, data.getData());
                    SharedPreferences.Editor editor = getSharedPreferences("configure", MODE_PRIVATE).edit();
                    editor.putString("location", path);
                    editor.apply();
                }
                break;
            case iPictureChooser.REQUEST_IMG_CHOOSE:
                if (data != null)
                {
                    pictureChooser.setUpdatedPicture(data.getData());
                }
                break;
        }
    }

    private void replace()
    {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        try
        {
            String path = getSharedPreferences("configure", MODE_PRIVATE).getString("location", "");
            File file = new File(path);
            File[] files = file.listFiles();
            List<String> pictures = pictureChooser.getList();
            int number = files.length / pictures.size() + 1;
            for (int i = 0; i < files.length; i += number)
            {
                for (int j = 0; j < number; j++)
                {
                    if (i + j >= files.length)
                    {
                        progressDialog.hide();
                        return;
                    }
                    FileDo.copy(pictures.get(i / number), files[i + j].getAbsolutePath());
                    Log.i(TAG, "replace: old: " + i + " new: " + (i + j));
                }
            }
        } catch (Exception e)
        {
            progressDialog.hide();
        }
        progressDialog.hide();
    }

    private void checkPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION)
        {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                finish();
            }
        }
    }
}
