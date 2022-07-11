package com.viniciusbozzi.project.helper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.model.Grafico;
import com.viniciusbozzi.project.model.XYValue;
import org.json.JSONException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 DESENVOLVIDO POR VINICIUS BOZZI
 */


public class GraficoDAO implements IGraficoDAO {

    private SQLiteDatabase escreve;
    private SQLiteDatabase le;
    private StringBuilder dados;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth autenticacao;
    FirebaseUser usuarioAtual;
    DatabaseReference myRefEmail;

    public GraficoDAO(Context context) {
        DbHelper db = new DbHelper( context );
        escreve = db.getWritableDatabase();
        le = db.getReadableDatabase();
    }

    @Override
    public boolean salvar(Grafico grafico, boolean check_global) throws JSONException {

//        ContentValues cv = new ContentValues();
//        cv.put("nome", grafico.getNomeGrafico());
//        cv.put("tempo", grafico.getTempo());
//        cv.put("temperatura", grafico.getTemperatura());
//        if(check_global){
//            cv.put("global", 1);
//        }else{
//            cv.put("global", 0);
//        }

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        myRefEmail = database.getReference(usuarioAtual.getUid());
        String aux2 = myRefEmail.push().getKey();

        //cv.put("idtorra", aux2);

//        Gson gson = new Gson();
//        String json = gson.toJson(grafico.getValoresXY());
        ArrayList<XYValue> xyValueArray = grafico.getValoresXY();
//        Log.i("TAG3", json);
//        cv.put("uniqueArrays",json);

        try {
            //escreve.insert(DbHelper.TABELA_GRAFICOS, null, cv );

            int i = 0;
            dados = new StringBuilder();
            dados.delete(0, dados.length());
            for (xyValueArray.get(i).getX(); xyValueArray.get(i).getX() < xyValueArray.get(xyValueArray.size() - 1).getX(); i++) {
                dados.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
                Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
            }
            dados.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60);
            Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");


            //salvar Firebase
            String aux = usuarioAtual.getUid()+"/"+ aux2;
            myRefEmail = database.getReference(aux);

            DatabaseReference myRefEmailNome = database.getReference(aux + "/nomeGrafico");
            myRefEmailNome.setValue(grafico.getNomeGrafico());
            DatabaseReference myRefEmailTempo = database.getReference(aux + "/tempo");
            myRefEmailTempo.setValue(grafico.getTempo());
            DatabaseReference myRefEmailTemperatura = database.getReference(aux + "/temperatura");
            myRefEmailTemperatura.setValue(grafico.getTemperatura());
            DatabaseReference myRefEmailId = database.getReference(aux + "/idFirebase");
            myRefEmailId.setValue(aux2);
            DatabaseReference myRefEmailDados = database.getReference(aux + "/valorXY");
            myRefEmailDados.setValue(dados.toString());
            DatabaseReference myRefEmailGlobal = database.getReference(aux + "/global");

            if (check_global) { //se tiver marcado eh publico
                myRefEmailGlobal.setValue(true);
            }else{
                myRefEmailGlobal.setValue(false);
            }

            Log.i("INFO", "Tarefa salva com sucesso!");
        }catch (Exception e){
            Log.e("INFO", "Erro ao salvar tarefa " + e.getMessage() );
            return false;
        }

        return true;
    }

    @Override
    public boolean atualizar(Grafico grafico, boolean check_global) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        Log.d("TAG", "onCreate: "+usuarioAtual.getUid());
        myRefEmail = database.getReference(usuarioAtual.getUid());
        String aux2 = grafico.getIdFirebase();

        ArrayList<XYValue> xyValueArray = grafico.getValoresXY();

        try {
            //atualizar no firebase
            int i = 0;
            dados = new StringBuilder();
            dados.delete(0, dados.length());
            for (xyValueArray.get(i).getX(); xyValueArray.get(i).getX() < xyValueArray.get(xyValueArray.size() - 1).getX(); i++) {
                dados.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
                Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");
            }
            dados.append(xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60);
            Log.i("saida", xyValueArray.get(i).getY() + "," + xyValueArray.get(i).getX() * 60 + ",");


            //atualizar Firebase
            String aux = usuarioAtual.getUid()+"/"+ aux2;
            myRefEmail = database.getReference(aux);

            DatabaseReference myRefEmailTempo = database.getReference(aux + "/tempo");
            myRefEmailTempo.setValue(grafico.getTempo());
            DatabaseReference myRefEmailTemperatura = database.getReference(aux + "/temperatura");
            myRefEmailTemperatura.setValue(grafico.getTemperatura());
            DatabaseReference myRefEmailDados = database.getReference(aux + "/valorXY");
            myRefEmailDados.setValue(dados.toString());
            DatabaseReference myRefEmailGlobal = database.getReference(aux + "/global");
            if (check_global) { //se tiver marcado eh publico
                myRefEmailGlobal.setValue(true);
            }else{
                myRefEmailGlobal.setValue(false);
            }

            Log.i("INFO", "Tarefa atualizada com sucesso!");
        }catch (Exception e){
            Log.e("INFO", "Erro ao atualizada tarefa " + e.getMessage() );
            return false;
        }

        return true;
    }


    @Override
    public boolean deletar(Grafico grafico) {

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        Log.d("TAG", "onCreate: "+usuarioAtual.getUid());
        myRefEmail = database.getReference(usuarioAtual.getUid());
        String aux2 = grafico.getIdFirebase();

        try {
//            String[] args = { grafico.getId().toString() };
//            escreve.delete(DbHelper.TABELA_GRAFICOS, "id=?", args );

            String aux = usuarioAtual.getUid()+"/"+ aux2;
            myRefEmail = database.getReference(aux);

            myRefEmail.removeValue();

            Log.i("INFO", "Tarefa removida com sucesso!");


        }catch (Exception e){
            Log.e("INFO", "Erro ao remover tarefa " + e.getMessage() );
            return false;
        }

        return true;
    }

    @Override
    public List<Grafico> listar() {

        List<Grafico> graficos = new ArrayList<>();

        String sql = "SELECT * FROM " + DbHelper.TABELA_GRAFICOS + " ;";
        Cursor c = le.rawQuery(sql, null);

        while ( c.moveToNext() ){

            Grafico grafico = new Grafico();

            Long id = c.getLong( c.getColumnIndex("id") );
            String nomegrafico = c.getString( c.getColumnIndex("nome") );
            String tempoGrafico = c.getString( c.getColumnIndex("tempo") );
            String temperaturaGrafico = c.getString( c.getColumnIndex("temperatura") );
            String graficoObjct = c.getString(c.getColumnIndex("uniqueArrays"));
            String idtorraGrafico = c.getString(c.getColumnIndex("idtorra"));
            int globalGrafico = c.getInt(c.getColumnIndex("global"));
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<XYValue>>() {}.getType();

            grafico.setId( id );
            grafico.setNomeGrafico( nomegrafico );
            grafico.setTempo(Double.parseDouble(tempoGrafico));
            grafico.setTemperatura(Double.parseDouble(temperaturaGrafico));
            grafico.setValoresXY((ArrayList<XYValue>) gson.fromJson(graficoObjct,type));
            grafico.setIdFirebase(idtorraGrafico);
            if(globalGrafico == 0){
                grafico.setGlobal(false);
            }else{
                grafico.setGlobal(true);
            }

            graficos.add( grafico );
            Log.i("tarefaDao", grafico.getNomeGrafico() );
        }

        return graficos;

    }
}
