package com.viniciusbozzi.project.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.adapter.GraficoAdapter;
import com.viniciusbozzi.project.helper.ConfiguracaoFirebase;
import com.viniciusbozzi.project.helper.GraficoDAO;
import com.viniciusbozzi.project.helper.RecyclerItemClickListener;
import com.viniciusbozzi.project.model.Grafico;

import java.util.ArrayList;
import java.util.List;

public class TorrasListGlobal extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerView;
    private List<Grafico> graficoList = new ArrayList<>();
    private GraficoAdapter graficoAdapter;
    private ValueEventListener listenerGraficos;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser usuarioAtual;
    DatabaseReference myRefEmail;
    DatabaseReference myRefUsuarios;
    String aux2;
    String globalEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torras_list_global);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        myRefEmail = database.getReference(usuarioAtual.getUid());
        myRefUsuarios = database.getReference();

        globalEdit = (String) getIntent().getSerializableExtra("globaledit");

        DatabaseReference myRefEmailDispositivo = database.getReference(usuarioAtual.getUid() +
                "/u_dispositivos");
        myRefEmailDispositivo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class) == null){
                    //textResult2.setText("Nenhum dispositivo cadastrado!");
                }else {
                    aux2 = dataSnapshot.getValue(String.class).toUpperCase();
                    //System.out.println("\n\n1\n\n" + aux2);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //textResult2.setText("Nenhum dispositivo cadastrado!");
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getApplicationContext(),
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                if(aux2 == null){
                                    Toast.makeText(getApplicationContext(),
                                            "Cadastre um dispositivo antes!",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(TorrasListGlobal.this, ListaIpActivity.class);
                                    startActivity( intent );
                                }else{
                                    Log.d("321", "onItemClick: " + globalEdit);
                                    Grafico graficoSelecionado = graficoList.get(position);
                                    Intent intent = new Intent(TorrasListGlobal.this, TorraSelecionadaActivity.class);
                                    intent.putExtra("graficoSelecionado",graficoSelecionado);
                                    intent.putExtra("macDispositivo",aux2);
                                    intent.putExtra("globaledit", globalEdit);
                                    startActivity( intent );
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {}

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

    }

    @Override
    protected void onStart() {
        carregarGraficos();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        myRefEmail.removeEventListener(listenerGraficos);
    }

    private void carregarGraficos() {

        GraficoDAO graficoDAO = new GraficoDAO(getApplicationContext());
        //graficoList = graficoDAO.listar(); //listar pelo SQLITE
        recuperaGraficos();

        graficoAdapter = new GraficoAdapter(graficoList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        recyclerView.setAdapter(graficoAdapter);

    }

    private void recuperaGraficos() {

        listenerGraficos = myRefUsuarios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("tag31", "onDataChange: " + myRefUsuarios.toString());

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Log.d("strTest1", "onDataChange: " + ds.toString());

                    if( !ds.getKey().equals("dispositivos")) {

                        Log.d("strTest2", "onDataChange: " + ds.toString());
                        graficoList.clear();

                        DatabaseReference myRefTorra = database.getReference(ds.getKey());

                        myRefTorra.addListenerForSingleValueEvent(new ValueEventListener(){
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot){

                                for (DataSnapshot ds2 : dataSnapshot.getChildren()) {

                                    Log.d("strTest3", "onDataChange: " + ds2.toString());

                                    if( !ds2.getKey().equals("u_dispositivos")){
                                        Log.d("strTest4", "onDataChange: " + ds2.toString());
                                        Grafico grafico = ds2.getValue(Grafico.class);
                                        if(grafico.isGlobal()) {
                                            graficoList.add(grafico);
                                        }
                                    }
                                }
                                graficoAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("TAG", databaseError.getMessage());
                            }

                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAG", databaseError.getMessage());
            }
        });

    }
}