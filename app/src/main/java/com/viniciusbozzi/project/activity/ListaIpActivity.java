package com.viniciusbozzi.project.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.viniciusbozzi.project.R;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.viniciusbozzi.project.helper.ConfiguracaoFirebase;

public class ListaIpActivity extends AppCompatActivity {
    Button btnRead;
    Button btnQrcode;
    TextView textResult;
    TextView textResult2;

    ListView listViewNode;
    ArrayList<Node> listNote;
    private int LoopCurrentIP;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth autenticacao;
    FirebaseUser usuarioAtual;
    DatabaseReference myRefEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_ip);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuarioAtual = autenticacao.getCurrentUser();
        myRefEmail = database.getReference(usuarioAtual.getUid() + "/u_dispositivos");

        btnRead = (Button) findViewById(R.id.readclient);
        btnQrcode = (Button) findViewById(R.id.qrcode_button);
        textResult = (TextView) findViewById(R.id.result);
        textResult2 = (TextView) findViewById(R.id.torrador_cadastrado);

        listViewNode = (ListView) findViewById(R.id.nodelist);
        listNote = new ArrayList<>();
        ArrayAdapter<Node> adapter =
                new ArrayAdapter<Node>(
                        ListaIpActivity.this,
                        android.R.layout.simple_list_item_1,
                        listNote);
        listViewNode.setAdapter(adapter);

        listViewNode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Node node = (Node) parent.getAdapter().getItem(position);
                myRefEmail.setValue(node.mac);
                Toast.makeText(ListaIpActivity.this,
                        "MAC:\t" + node.mac + "\n" +
                                "IP:\t" + node.ip + "\n" +
                                "Cadastrado com Sucesso",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TaskReadAddresses(listNote, listViewNode).execute();
            }
        });

        btnQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListaIpActivity.this, QrCodeActivity.class);
                startActivity(intent);
                finish();
            }
        });


        myRefEmail.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class) == null){
                    textResult2.setText("Nenhum dispositivo cadastrado!");
                }else {
                    textResult2.setText("ID da torradeira: " + dataSnapshot.getValue(String.class));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                textResult2.setText("Nenhum dispositivo cadastrado!");
            }
        });
    }

    class Node {
        String ip;
        String mac;

        Node(String ip, String mac) {
            this.ip = ip;
            this.mac = mac;
        }
        @Override
        public String toString() {
            return "IP: " + ip + "\n" + "MAC: " + mac;
        }
    }

    private class TaskReadAddresses extends AsyncTask<Void, Node, Void> {

        ArrayList<Node> array;
        ListView listView;

        TaskReadAddresses(ArrayList<Node> array, ListView v) {
            listView = v;
            this.array = array;
            array.clear();
            textResult.setText("Carregando...");
        }

        @Override
        protected Void doInBackground(Void... params) {
            readAddresses();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            textResult.setText("Pronto");
        }

        @Override
        protected void onProgressUpdate(Node... values) {
            listNote.add(values[0]);
            ((ArrayAdapter) (listView.getAdapter())).notifyDataSetChanged();

        }

        private void readAddresses() {

            BufferedReader bufferedReader = null;

            String s = getLocalIpAddress();
            Log.d("222", "readAddresses: " + s);

            getConnectedDevices(s);

            try {
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] splitted = line.split(" +");
                    if (splitted != null && splitted.length >= 4) {
                        String ip = splitted[0];
                        String mac = splitted[3];
                        if (mac.matches("..:..:..:..:..:..")) {
                            if (!mac.equalsIgnoreCase("00:00:00:00:00:00")) {

                                //verifica se eh a cafeteira
                                try {
                                    Connection conn = Jsoup.connect("http://" + ip + "/confirm.html").ignoreHttpErrors(true).timeout(0);
                                    Document doc = conn.get();
                                    //System.out.println("oi\n\n" + doc.title());
                                    if (doc.title().equals("torradeira")) {
                                        Node thisNode = new Node(ip, mac);
                                        publishProgress(thisNode);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getLocalIpAddress() {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        public void getConnectedDevices(String YourPhoneIPAddress) {
            ArrayList<InetAddress> ret = new ArrayList<InetAddress>();
            LoopCurrentIP = 1;
            String[] myIPArray = YourPhoneIPAddress.split("\\.");
            InetAddress currentPingAddr;

            for (int i = 1; i <= 254; i++) {
                try {
                    // build the next IP address
                    currentPingAddr = InetAddress.getByName(myIPArray[0] + "." +
                            myIPArray[1] + "." +
                            myIPArray[2] + "." +
                            Integer.toString(LoopCurrentIP));
                    String ad = currentPingAddr.toString();   /////////////////
                    Log.d("MyApp", ad);                 //////////////

                    // 5ms Timeout for the "ping"
                    if (currentPingAddr.isReachable(1)) {

                        ret.add(currentPingAddr);
                        Log.d("MyApp", ad);

                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnknownHostException ex) {
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                LoopCurrentIP++;
            }
        }
    }
}