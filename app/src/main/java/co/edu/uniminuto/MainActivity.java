package co.edu.uniminuto;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 25;
    public static final int DEFAULT_VALUE = -1;
    private Button btnCheckPermissions;
    private Button btnRequestPermissionsCamera;
    private Button btnRequestPermissionsES;
    private Button btnRequestPermissionsRS;
    private Button btnRequestPermissionsBiometric;

    private TextView tvCamera;
    private TextView tvBiometric;
    private TextView tvExternalWS;
    private TextView tvReadExternalS;
    private TextView tvInternet;
    private TextView tvResponse;

    //1.1 Objetos para recursos
    private TextView versionAndroid;
    private int versionSDK;
    private ProgressBar pbLevelBatt;
    private TextView tvLevelBatt;
    private TextView tvConexion;
    IntentFilter batFilter;
    CameraManager cameraManager;
    String cameraId;
    private Button btnOff;
    private Button btnOn;
    ConnectivityManager conexion;

    public MainActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 3. Llamado del metodo de enlace de objetos
        initObject();

        //4. Enlace de botones a los metodos
        btnCheckPermissions.setOnClickListener(this::voidCheckPermissions);
        btnRequestPermissionsCamera.setOnClickListener(this::voidRequestPermissionsCamera);
        btnRequestPermissionsBiometric.setOnClickListener(this::voidRequestPermissionsBiometric);
        btnRequestPermissionsES.setOnClickListener(this::voidRequestPermissionsES);
        btnRequestPermissionsRS.setOnClickListener(this::voidRequestPermissionsRS);
        //Botones para la linterna
        btnOn.setOnClickListener(this::onLigth);
        btnOff.setOnClickListener(this::offLigth);
        //Bateria
        batFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver,batFilter);
        //Internet
        tvInternet = findViewById(R.id.tvInternet);internetStatus();

    }

    private void internetStatus() {
        if (ConexionInternet()){
            tvInternet.setText("Conexion a Internet disponible");
        }else{
            tvInternet.setText("Sin conexion a Internet");
        }
    }
    private boolean ConexionInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
        }
        return false;
    }


    //10. Bateria
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int levelBattery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,DEFAULT_VALUE);
            pbLevelBatt.setProgress(levelBattery);
            tvLevelBatt.setText("Level Battery"+levelBattery+" %");
        }
    };
    //8. Implementación de OnResume para la version Android

    @Override
    protected void onResume() {
        super.onResume();
        String versionSO = Build.VERSION.RELEASE;
        versionSDK = Build.VERSION.SDK_INT;
        versionAndroid.setText("Version SO"+versionSO+ " / SDK:"+versionSDK);
    }
    //9. Encendido y apagado de linterna

    private void onLigth(View view) {
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId,true);
        }catch (Exception e){
            Toast.makeText(this,"No se puede encender la linterna", Toast.LENGTH_SHORT).show();
            Log.i("FLASH",e.getMessage());
        }
    }
    private void offLigth(View view) {
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId,false);
        }catch (Exception e){
            Toast.makeText(this,"No se puede encender la linterna", Toast.LENGTH_SHORT).show();
            Log.i("FLASH",e.getMessage());
        }
    }

    //5. Verificacón de permisos
    private void voidCheckPermissions(View view) {
        //Si hay permiso --> 0 si no --> -1
        int statusCamera = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int statusWES = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int statusRES = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int statusInternet = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET);
        int statusBiometric = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.USE_BIOMETRIC);
        tvCamera.setText("Status Camera:"+statusCamera);
        tvExternalWS.setText("Status WES:"+statusWES);
        tvReadExternalS.setText("Status RES:"+statusRES);
        tvInternet.setText("Status Internet:"+statusInternet);
        tvBiometric.setText("Status Biometric:"+statusBiometric);
        btnRequestPermissionsCamera.setEnabled(true);
    }
    //6. Solicitud de permisos
    private void voidRequestPermissionsCamera(View view) {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},REQUEST_CODE);

        }
    }
    private void voidRequestPermissionsRS(View view) {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},6);
        }
    }
    private void voidRequestPermissionsES(View view) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7);
        }
    }
    private void voidRequestPermissionsBiometric(View view) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.USE_BIOMETRIC}, 8);
        }
    }


    //7. Gestion de respuesta del usuario respecto a la solicitud del permiso

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        tvResponse.setText(" "+grantResults[0]);
        if (requestCode == REQUEST_CODE){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                new AlertDialog.Builder(this).setTitle("Box Permissions").setMessage("Usted no otorgó los permisos").setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();

                    }
                }).create().show();

            }else{
                Toast.makeText(this,"Usted no ha otrogado los permisos", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"Usted no ha otrogado los permisos", Toast.LENGTH_SHORT).show();

        }
    }

    // 2. Enlace de objetos
    private void initObject(){
        btnCheckPermissions = findViewById(R.id.btnCheckPermission);
        btnRequestPermissionsCamera = findViewById(R.id.btnRequestPermission);
        btnRequestPermissionsCamera.setEnabled(false);
        btnRequestPermissionsBiometric = findViewById(R.id.btnRequestPermissionBiometric);
        btnRequestPermissionsES = findViewById(R.id.btnRequestPermissionES);
        btnRequestPermissionsRS = findViewById(R.id.btnRequestPermissionRS);
        tvCamera = findViewById(R.id.tvCamera);
        tvBiometric = findViewById(R.id.tvDactilar);
        tvExternalWS = findViewById(R.id.tvEws);
        tvReadExternalS = findViewById(R.id.tvRS);
        tvInternet = findViewById(R.id.tvInternet);
        tvResponse = findViewById(R.id.tvResponse);
        versionAndroid = findViewById(R.id.tvVersionAndroid);
        pbLevelBatt = findViewById(R.id.pbLevelBattery);
        tvLevelBatt = findViewById(R.id.tvLevelBattery);
        tvConexion = findViewById(R.id.tvConexion);
        btnOn = findViewById(R.id.btnOn);
        btnOff = findViewById(R.id.btnOff);

    }

}