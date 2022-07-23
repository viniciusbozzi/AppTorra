package com.viniciusbozzi.project.activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.viniciusbozzi.project.R;
import com.viniciusbozzi.project.adapter.GraficoAdapter;
import com.viniciusbozzi.project.helper.ConfiguracaoFirebase;
import com.viniciusbozzi.project.helper.GraficoDAO;
import com.viniciusbozzi.project.helper.RecyclerItemClickListener;
import com.viniciusbozzi.project.model.Grafico;

import java.util.ArrayList;
import java.util.List;

/**
 DESENVOLVIDO POR VINICIUS BOZZI
 */

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerView;
    private List<Grafico> graficoList = new ArrayList<>();
    private GraficoAdapter graficoAdapter;
    private ValueEventListener listenerGraficos;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser usuarioAtual;
    DatabaseReference myRefEmail;
    String aux2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        myRefEmail = database.getReference(usuarioAtual.getUid());

        DatabaseReference myRefEmailDispositivo = database.getReference(usuarioAtual.getUid() +
                "/u_dispositivos");
        myRefEmailDispositivo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class) == null){
                    //textResult2.setText("Nenhum dispositivo cadastrado!");
                }else {
                    aux2 = dataSnapshot.getValue(String.class).toUpperCase();
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
                                    Intent intent = new Intent(MainActivity.this, ListaIpActivity.class);
                                    startActivity( intent );
                                } else{
                                    Grafico graficoSelecionado = graficoList.get(position);
                                    Intent intent = new Intent(MainActivity.this, TorraSelecionadaActivity.class);
                                    intent.putExtra("graficoSelecionado",graficoSelecionado);
                                    intent.putExtra("macDispositivo",aux2);
                                    startActivity( intent );
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                                final Grafico graficoSelecionado = graficoList.get(position);
                                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                                //Configura título e mensagem
                                dialog.setTitle("Confirmar Exclusão");
                                dialog.setMessage("Deseja excluir a Torra: " + graficoSelecionado.getNomeGrafico() + " ?" );

                                dialog.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        GraficoDAO graficoDAO = new GraficoDAO(getApplicationContext());
                                        if ( graficoDAO.deletar(graficoSelecionado) ){
                                            carregarGraficos();
                                            Toast.makeText(getApplicationContext(),
                                                    "Sucesso ao Excluir Torra!",
                                                    Toast.LENGTH_SHORT).show();

                                        }else {
                                            Toast.makeText(getApplicationContext(),
                                                    "Erro ao Excluir Torra!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                dialog.setNegativeButton("Não", null );
                                dialog.create();
                                dialog.show();

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AdicionarGrafico.class);
                startActivity(intent);
            }
        });
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

        listenerGraficos = myRefEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                graficoList.clear();
                Log.d("tag31", "onDataChange: " + myRefEmail.toString());

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("strTest", "onDataChange: " + ds.toString());
                    if( !ds.getKey().equals("u_dispositivos")) {
                        Grafico grafico = ds.getValue(Grafico.class);
                        graficoList.add(grafico);
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

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void deslogarUsuario() {
        try {
            autenticacao.signOut();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_list_global:
                intent = new Intent(getApplicationContext(), TorrasListGlobal.class);
                intent.putExtra("globaledit", "edit_global");
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(getApplicationContext(), ListaIpActivity.class);
                startActivity(intent);
                break;
            case R.id.action_sair:
                deslogarUsuario();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
