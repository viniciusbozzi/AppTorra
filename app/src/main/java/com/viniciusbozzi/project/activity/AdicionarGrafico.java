package com.viniciusbozzi.project.activity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.nikartm.button.FitButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.helper.ConfiguracaoFirebase;
import com.viniciusbozzi.project.helper.GraficoDAO;
import com.viniciusbozzi.project.model.Grafico;
import com.viniciusbozzi.project.model.XYValue;

import org.json.JSONException;

import java.util.ArrayList;

/**
 DESENVOLVIDO POR VINICIUS BOZZI
 */

public class AdicionarGrafico extends AppCompatActivity {

    private static final String TAG = "AdicionarGrafico";
    private LineGraphSeries<DataPoint> xySeries;
    private FitButton btnAddPt;
    private EditText mX,mY;
    private EditText nomeGrafico;
    private GraphView graphView;
    private ArrayList<XYValue> xyValueArray;
    private StringBuilder dados;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth autenticacao;
    FirebaseUser usuarioAtual;
    DatabaseReference myRefEmail;
    private Switch tipoAcesso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_grafico);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        Log.d("TAG", "onCreate: "+usuarioAtual.getUid());
        myRefEmail = database.getReference(usuarioAtual.getUid());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Adicionar Nova Torra");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnAddPt = findViewById(R.id.btnAddPt);
        nomeGrafico = findViewById(R.id.nomeGrafico);
        mX = findViewById(R.id.numX);
        mY = findViewById(R.id.numY);
        graphView = findViewById(R.id.scatterPlot);
        tipoAcesso = findViewById(R.id.switchAcessoGlobal);
        tipoAcesso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    tipoAcesso.setText("Público");
                }else{
                    tipoAcesso.setText("Privado");
                }
            }
        });
        xyValueArray = new ArrayList<>();
        iniciar();
    }

    private void iniciar(){

        xySeries = new LineGraphSeries<>();

        btnAddPt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mX.getText().toString().equals("") && !mY.getText().toString().equals("") ){
                    double x = Double.parseDouble(mX.getText().toString());
                    double y = Double.parseDouble(mY.getText().toString());
                    boolean b = true;
                    for(XYValue value: xyValueArray){
                        if(value.getX() == x){
                            b = false;
                            break;
                        }
                    }
                    if(y>290){
                        Toast.makeText(AdicionarGrafico.this, "Favor digitar uma Temperatura abaixo de 290ºC!", Toast.LENGTH_SHORT).show();
                        return;
                    }if(x>15){
                        Toast.makeText(AdicionarGrafico.this, "Favor digitar uma Tempo abaixo de 15 minutos!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(b){
                        xyValueArray.add(new XYValue(x,y));
                        iniciar();
                        mX.setText("");
                        mY.setText("");
                    }else{
                        Toast.makeText(AdicionarGrafico.this, "Impossivel duas temperaturas no mesmo instante de Tempo!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(AdicionarGrafico.this, "Preencha Os campos!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(xyValueArray.size() != 0){
            criarGrafico();
        }else{
        }
    }

    private void criarGrafico() {

        xyValueArray = ordenarGrafico(xyValueArray);

        for(int i = 0;i <xyValueArray.size(); i++){
            try{
                xySeries.resetData(generateData());
            }catch (IllegalArgumentException e){
                Log.e(TAG, "msgERROR " + e.getMessage() );
            }
        }

        //set some properties
        xySeries.setColor(getResources().getColor(R.color.corCapuccino));

        //set Scrollable and Scaleable
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScalableY(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScrollableY(true);
        graphView.setCursorMode(true);
        xySeries.setDrawBackground(true);
        xySeries.setDrawDataPoints(true);
        xySeries.setDataPointsRadius(10);
        xySeries.setBackgroundColor(getResources().getColor(R.color.fundoGrafico));
        xySeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, final DataPointInterface dataPoint) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AdicionarGrafico.this);
                builder.setTitle("Deseja Remover esse Ponto ? \n Tempo="+dataPoint.getX()+" ,Temperatura="+dataPoint.getY());
                builder.setCancelable(false);
                builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(XYValue value: xyValueArray){
                            if(value.getX() == dataPoint.getX()){
                                xyValueArray.remove(value);
                                break;
                            }
                        }
                        iniciar();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMaxY(350);
        graphView.getViewport().setMinY(0);

        //set manual y bounds
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMaxX(18);
        graphView.getViewport().setMinX(0);

        graphView.addSeries(xySeries);
    }

    private DataPoint[] generateData() {
        xyValueArray = ordenarGrafico(xyValueArray);
        graphView.removeAllSeries();

        int count = xyValueArray.size();
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = xyValueArray.get(i).getX();
            double y = xyValueArray.get(i).getY();
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }


    private ArrayList<XYValue> ordenarGrafico(ArrayList<XYValue> array){

        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;

        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
            Log.d(TAG, "sortArray: m = " + m);
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
                    Log.d(TAG, "sortArray: count = " + count);
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
                    Log.d(TAG, "sortArray: count = " + count);
                }
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "sortArray: ArrayIndexOutOfBoundsException. Need more than 1 data point to create Plot." +
                        e.getMessage());
                break;
            }
        }
        return array;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_adicionar_grafico, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.itemSalvar:

                GraficoDAO graficoDAO = new GraficoDAO(getApplicationContext());

                if(!nomeGrafico.getText().toString().equals("") || !nomeGrafico.getText().toString().isEmpty() ){
                    if (nomeGrafico.getText().length() > 20){
                        Toast.makeText(this, "O nome da Torra deve ter até 20 Algarismos!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if(xyValueArray.get(0).getX() != 0){
                        Toast.makeText(getApplicationContext(),"Favor inserir a Temperatura no Tempo 0!",Toast.LENGTH_SHORT).show();
                        break;
                    }

                    //PROCESSO DE SALVAR NO SQLITE
                    Grafico g = new Grafico();
                    g.setNomeGrafico(nomeGrafico.getText().toString());
                    g.setTempo(xyValueArray.get(xyValueArray.size() - 1).getX());
                    double max = 0.0;
                    for (int i = 1; i < xyValueArray.size(); i++) {//aqui a iteração irá ocorrer
                        if (xyValueArray.get(i).getY() > max){ //caso o valor da posição i seja maior que o valor de max, max será substituído pelo valor da i-ésima posição.
                            max = xyValueArray.get(i).getY();
                            System.out.println(max);
                        }
                    }

                    g.setTemperatura(max);
                    g.setValoresXY(xyValueArray);
                    try {
                        if(graficoDAO.salvar(g, tipoAcesso.isChecked())){
                            //mandaDadosCsv(g);
                            finish();
                            Toast.makeText(getApplicationContext(),"Sucesso ao salvar Torra!",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Erro ao salvar Torra!",Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(this, "Dê um nome a sua Torra!", Toast.LENGTH_SHORT).show();

                }
        }
        return super.onOptionsItemSelected(item);
    }

}
