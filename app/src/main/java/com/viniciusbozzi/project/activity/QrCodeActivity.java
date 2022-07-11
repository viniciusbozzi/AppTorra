package com.viniciusbozzi.project.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.viniciusbozzi.project.R;
import android.view.View;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.viniciusbozzi.project.helper.ConfiguracaoFirebase;

public class QrCodeActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth autenticacao;
    FirebaseUser usuarioAtual;
    DatabaseReference myRefEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.code_scanner_view);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        myRefEmail = database.getReference(usuarioAtual.getUid() + "/u_dispositivos");

        if (ContextCompat.checkSelfPermission(QrCodeActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(QrCodeActivity.this, new String[] {Manifest.permission.CAMERA}, 123);
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(QrCodeActivity.this,
                                "MAC:\t" + result.getText() + "\n" +
                                        "Cadastrado com Sucesso",
                                Toast.LENGTH_SHORT).show();
                        myRefEmail.setValue(result.getText());
                        if(!result.getText().isEmpty()){
                            Intent intent = new Intent(QrCodeActivity.this, ListaIpActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                startScanning();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
    }

    @Override
    protected void onPause() {
        if(mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }
}
