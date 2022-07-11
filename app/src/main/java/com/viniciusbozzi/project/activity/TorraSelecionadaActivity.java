package com.viniciusbozzi.project.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.github.nikartm.button.FitButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.helper.ConfiguracaoFirebase;
import com.viniciusbozzi.project.helper.GraficoDAO;
import com.viniciusbozzi.project.model.Grafico;
import com.viniciusbozzi.project.model.XYValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Arrays;


/**
 * DESENVOLVIDO POR VINICIUS BOZZI
 */


public class TorraSelecionadaActivity extends AppCompatActivity {

    private LineGraphSeries<DataPoint> xySeries;
    private GraphView graphView;
    private ArrayList<XYValue> xyValueArray;
    private Grafico grafico;
    private StringBuilder dados;
    private FitButton fitButton;
    private FitButton buttonFirebase;
    private FitButton buttonEdit;
    private FitButton buttonIniciarTorra;
    private FitButton buttonPreAquecer;

    private AlertDialog dialog;
    private TextView tempTorraSelecionada;

    private StringBuilder dadosBluetooth;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    UUID uuidBluetooth = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int BLUETOOTH_ATIVADO = 1;
    private static final int LISTA_PAREADOS = 2;
    public static final int MESSAGE_READ = 3;
    private static String MAC;
    boolean conexao = false;
    public static ConnectedThread connectedThread;
    static Handler mHandler = new Handler();
    int count = 0;
    boolean dadosmandados = false;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    DatabaseReference myRef2;
    DatabaseReference myRef3;
    DatabaseReference myRef4;
    ValueEventListener listener3;
    String macDispositivo;
    String globalEdit;
    Double temper = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torra_selecionada);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        grafico = (Grafico) getIntent().getSerializableExtra("graficoSelecionado");
        macDispositivo = (String) getIntent().getSerializableExtra("macDispositivo");
        globalEdit = (String) getIntent().getSerializableExtra("globaledit");

        Log.d("345", "aqui: " + globalEdit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        graphView = findViewById(R.id.graficoSelecionado);
        fitButton = findViewById(R.id.buttonExport);
        tempTorraSelecionada = findViewById(R.id.tempTorraSelecionada);

        buttonEdit = findViewById(R.id.buttonEdit);
        buttonIniciarTorra = findViewById(R.id.buttonTorrar);
        buttonPreAquecer = findViewById(R.id.buttonPreAquecer);
        buttonFirebase = findViewById(R.id.buttonFirebase);

        if(globalEdit != null) {
            Log.d("567", "entrei:" + globalEdit);
            buttonEdit.setVisibility(View.INVISIBLE);
        }

        Log.d("123", "onCreate: " + macDispositivo);

        myRef = database.getReference("dispositivos/" + macDispositivo + "/dados");
        myRef2 = database.getReference("dispositivos/" + macDispositivo + "/aquecendo");
        myRef3 = database.getReference("dispositivos/" + macDispositivo + "/temperatura");
        myRef4 = database.getReference("dispositivos/" + macDispositivo + "/torrando");

        if (grafico != null) {
            toolbar.setTitle(grafico.getNomeGrafico().toUpperCase());
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            xyValueArray = new ArrayList<>();
            Log.d("TAG", "onCreate: " + grafico.getValorXY());
            String graficosStr[] = grafico.getValorXY().split(",");
            //String graficosStr[] = Arrays.copyOfRange(op, 0, op.length); //retira elemento 0
            int i;
            int j= 0;
            for(i = 0; i < graficosStr.length; i=i+2){
                xyValueArray.add(new XYValue(Double.parseDouble(graficosStr[i+1])/60,Double.parseDouble(graficosStr[i])));
                j++;
            }

            xySeries = new LineGraphSeries<>();
            if (xyValueArray.size() != 0) {
                iniciarGrafico();
            } else {
            }
        }

        fitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcularValoresCsv();

                try {
                    FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
                    out.write((dados.toString()).getBytes());
                    out.close();

                    Context context = getApplicationContext();
                    File file = new File(getFilesDir(), "data.csv");
                    Uri path = FileProvider.getUriForFile(context, "com.viniciusbozzi.project.fileprovider", file);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/csv");
                    intent.putExtra(Intent.EXTRA_SUBJECT, grafico.getNomeGrafico());
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(intent, "Exportar .CSV"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mandaDadosCsv();
                abrirDialogCarregamento();

                dadosmandados = true;

                new CountDownTimer(1500, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        dialog.cancel();
                    }
                }.start();

                buttonPreAquecer.setVisibility(View.VISIBLE);
            }
        });

        buttonIniciarTorra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dadosmandados = false;
                myRef4.setValue(true);
                myRef3.removeEventListener(listener3);

                Intent intent = new Intent(TorraSelecionadaActivity.this, TorraRealTime.class);
                intent.putExtra("graficoTempoReal", grafico);
                intent.putExtra("macDispositivo",macDispositivo);
                startActivity(intent);
                finish();
            }
        });

        buttonPreAquecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef2.setValue(true);

                abrirDialogCarregamento();

                dadosmandados = true;

                new CountDownTimer(1500, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        dialog.cancel();
                    }
                }.start();

                buttonFirebase.setVisibility(View.GONE);
                fitButton.setVisibility(View.GONE);
                buttonEdit.setVisibility(View.GONE);
                buttonPreAquecer.setText("AJUSTANDO TEMPERATURA!");
                buttonPreAquecer.setEnabled(false);
                tempTorraSelecionada.setVisibility(View.VISIBLE);

            }
        });

        if(globalEdit == null) {
            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    myRef2.setValue(false);

                    Intent intent = new Intent(TorraSelecionadaActivity.this, EditarTorraActivity.class);
                    intent.putExtra("graficoEditado", grafico);
                    intent.putExtra("macDispositivo", macDispositivo);
//                    if (conexao) {
//                        try {
//                            bluetoothSocket.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        conexao = false;
//                    }
                    startActivity(intent);
                    finish();
                }
            });
        }

    }

    @Override
    protected void onStop() {
        myRef2.setValue(false);
        buttonIniciarTorra.setVisibility(View.GONE);
        //buttonPreAquecer.setVisibility(View.GONE);
        tempTorraSelecionada.setVisibility(View.GONE);
        myRef3.removeEventListener(listener3);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        listener3 = myRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
                temper = dataSnapshot.getValue(Double.class);
//                Log.d("TAG", "Value is: " + value);
                tempTorraSelecionada.setText(temper+"ºC");
                //tempPreAque.setText(temper+"ºC");
                if(temper >= xyValueArray.get(0).getY() && dadosmandados){
                    dialog.cancel();
                    buttonPreAquecer.setVisibility(View.GONE);
                    buttonFirebase.setVisibility(View.GONE);
                    buttonEdit.setVisibility(View.GONE);
                    fitButton.setVisibility(View.GONE);
                    buttonIniciarTorra.setVisibility(View.VISIBLE);
                    Toast.makeText(TorraSelecionadaActivity.this,
                            "O torrador já está pré-aquecido!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Voce deseja abandonar a Torra?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        myRef2.setValue(false);
                        dialog.cancel();
                        TorraSelecionadaActivity.this.onSuperBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onSuperBackPressed(){
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case BLUETOOTH_ATIVADO:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth Conectado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bluetooth não Conectado", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case LISTA_PAREADOS:
                if (resultCode == Activity.RESULT_OK) {
                    MAC = data.getExtras().getString(ListaDispositivos.enderecoMAC);
                    bluetoothDevice = bluetoothAdapter.getRemoteDevice(MAC);
                    calcularValoresBluetooth();

                    try {
                        bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuidBluetooth);
                        bluetoothSocket.connect();
                        conexao = true;
                        connectedThread = new ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                        connectedThread.enviar(dadosBluetooth.toString());

                        /*
                        abrirDialogCarregamento();

                        new CountDownTimer(15000, 1000) {
                            public void onTick(long millisUntilFinished) {
                            }
                            public void onFinish() {
                                dialog.cancel();
                            }
                        }.start();
                         */
                        buttonPreAquecer.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        conexao = false;
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Bluetooth Falhou", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private void abrirDialogCarregamento() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert.setView(R.layout.carregamento);
        }
        dialog = alert.create();
        dialog.show();
    }

    private void iniciarGrafico() {

        xyValueArray = ordenarGrafico(xyValueArray);

        for (int i = 0; i < xyValueArray.size(); i++) {
            try {
                double x = xyValueArray.get(i).getX();
                double y = xyValueArray.get(i).getY();
                xySeries.appendData(new DataPoint(x, y), true, 1000);
            } catch (IllegalArgumentException e) {
                Log.e("TAG", "msgERROR " + e.getMessage());
            }
        }

        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScalableY(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.setCursorMode(true);
        xySeries.setColor(getResources().getColor(R.color.corCapuccino));
        xySeries.setDrawBackground(true);
        xySeries.setDrawDataPoints(true);
        xySeries.setDataPointsRadius(10);
        xySeries.setBackgroundColor(getResources().getColor(R.color.fundoGrafico));
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxY(350);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMaxX(18);
        graphView.getViewport().setMinX(0);
        graphView.addSeries(xySeries);

    }

    private ArrayList<XYValue> ordenarGrafico(ArrayList<XYValue> array) {

        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(), 2))));
        int m = array.size() - 1;
        int count = 0;

        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
            try {
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                }
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }
        return array;
    }

    private void calcularValoresBluetooth() {

        int i = 0;
        int j = 0;
        int seg = 1;
        Double valorSomado = 0.00;
        dadosBluetooth = new StringBuilder();
        dadosBluetooth.delete(0, dadosBluetooth.length());

        for (xyValueArray.get(i).getX(); xyValueArray.get(i).getX() < xyValueArray.get(xyValueArray.size() - 1).getX(); i++) {
            dadosBluetooth.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
            Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
        }
        dadosBluetooth.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60);
        Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");


        myRef.setValue(dadosBluetooth.toString());
        myRef2.setValue(false);

    }

    private void mandaDadosCsv(){
        int i = 0;
        dadosBluetooth = new StringBuilder();
        dadosBluetooth.delete(0, dadosBluetooth.length());
        for (xyValueArray.get(i).getX(); xyValueArray.get(i).getX() < xyValueArray.get(xyValueArray.size() - 1).getX(); i++) {
            dadosBluetooth.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
            Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
        }
        dadosBluetooth.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60);
        Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");

        myRef.setValue(dadosBluetooth.toString());
//        myRef2.setValue(true);
    }

//    private void mandaDadosFirebase() throws JSONException {
//
//        JSONObject jsonObject = new JSONObject();
//        FileWriter fileWriter =  null;
//
//        for (int i=0; i < xyValueArray.size(); i++){
//            jsonObject.put("tempo"+i,xyValueArray.get(i).getX());
//            jsonObject.put("tempe"+i,xyValueArray.get(i).getY());
//        }
//        try {
//            fileWriter = new FileWriter("data.json");
//            fileWriter.write(jsonObject.toString());
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.d("json", jsonObject.toString());
////        System.out.println(jsonObject);
//
//        myRef.setValue(jsonObject+"");
//        myRef2.setValue(true);
//
//    }


    private void calcularValoresCsv() {

        int i = 0;
        int j = 0;
        int seg = 1;
        Double valorSomado = 0.00;
        dados = new StringBuilder();
        dados.append("Tempo(s),Temperatura(C)");
        Double valorInicialY = xyValueArray.get(0).getY();
        Double valorInicialX = (xyValueArray.get(0).getX()) * 60;
        seg = seg + valorInicialX.intValue();
        dados.append("\n" + valorInicialX + "," + valorInicialY);

        for (xyValueArray.get(i).getX(); xyValueArray.get(i).getX() < xyValueArray.get(xyValueArray.size() - 1).getX(); i++) {
            Double value = (xyValueArray.get(j + 1).getY() - xyValueArray.get(j).getY())
                    /
                    ((xyValueArray.get(j + 1).getX() - xyValueArray.get(j).getX()) * 60);
            Log.i("saida", " " + value);

            for (int cont = 0; cont < 60 * (xyValueArray.get(i + 1).getX() - xyValueArray.get(i).getX()); cont++) {
                valorSomado = valorSomado + value;
                Double valorFinal = valorSomado + valorInicialY;
                Log.i("saida", " " + valorFinal);

                dados.append("\n" + seg + "," + valorFinal);
                seg++;
            }
            j++;
        }
    }

    public static void gethandler(Handler handler) {
        mHandler = handler;
    }

    public class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("TAG", "Error occurred when creating output stream", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    try {
                        sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    bytes = mmInStream.read(buffer);

                    String dadosBt = new String(buffer, 0, bytes);
                    Log.d("TAG", "dadosBT: " + dadosBt);

//                    if (dadosBt.indexOf('.') == 2) {
//                        tempTorraSelecionada.setText(dadosBt.substring(0, 2) + " ºC");
//                    }
//                    else if (dadosBt.indexOf('.') == 3) {
//                        tempTorraSelecionada.setText(dadosBt.substring(0, 3) + " ºC");
//                    }

                    if (count < 2) {
                        Log.d("cont", "" + count);

                        if (dadosBt.contains("K")) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    buttonIniciarTorra.setVisibility(View.VISIBLE);
                                    buttonPreAquecer.setVisibility(View.GONE);
                                }
                            });
                        }
                        count++;
                    } else {
                        if (dadosBt.indexOf('.') == 2) {
                            tempTorraSelecionada.setText(dadosBt.substring(0, 2) + " ºC");
                            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                            try {
                                sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else if (dadosBt.indexOf('.') == 3) {
                            tempTorraSelecionada.setText(dadosBt.substring(0, 3) + " ºC");
                            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                            try {
                                sleep(700);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (dadosBt.contains("K")) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    buttonIniciarTorra.setVisibility(View.VISIBLE);
                                    buttonPreAquecer.setVisibility(View.GONE);
                                }
                            });
                        /*runOnUiThread(new Runnable() {
                            public void run() {
                                buttonIniciarTorra.setVisibility(View.VISIBLE);
                                buttonPreAquecer.setVisibility(View.GONE);
                            }
                        });*/
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void enviar(String enviar) {

            byte[] bufferMsg = enviar.getBytes();
            try {
                mmOutStream.write(bufferMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
