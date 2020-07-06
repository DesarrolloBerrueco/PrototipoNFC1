package com.romellfudi.fudinfc.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.romellfudi.fudinfc.app.data.NfcUser;
import com.romellfudi.fudinfc.app.data.UserRepository;

public class MainActivityV2 extends AppCompatActivity {

    // list of NFC technologies detected:
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };

    private RadioGroup rgType = null;
    private RadioButton rbEntrada = null;
    private RadioButton rbSalida = null;
    private EditText etDni = null;
    private Button btnCreateUser = null;
    private EditText etNfcId = null;
    private Button btnSimulateNfc = null;

    private String lastNfcId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // creating pending intent:
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // creating intent receiver for NFC events:
        IntentFilter filter = new IntentFilter();
        filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // enabling foreground dispatch for getting intent from NFC event:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // disabling foreground dispatch:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            String id = "NFC Tag\n" + ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            Toast.makeText(this, id, Toast.LENGTH_LONG).show();
            onNfcScan(id);
        }
    }

    private void bindViews() {
        rgType = findViewById(R.id.rg_type);
        rbEntrada = findViewById(R.id.rb_entrada);
        rbSalida = findViewById(R.id.rb_salida);
        etDni = findViewById(R.id.et_dni);
        btnCreateUser = findViewById(R.id.btn_create_user);
        etNfcId = findViewById(R.id.et_nfc_id);
        btnSimulateNfc = findViewById(R.id.btn_simulate_nfc);
    }

    private void setListeners() {
        btnSimulateNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nfcId = etNfcId.getText().toString().trim();
                if(!nfcId.isEmpty()) {
                    etNfcId.setText("");
                    onNfcScan(nfcId);
                } else {
                    showPopupMsg("Introduzca dato para simular tickada con nfc...");
                }
            }
        });

        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lastNfcId.trim().isEmpty()) {
                    String dni = etDni.getText().toString().trim();
                    if(Utils.isValidDni(dni)) {
                        UserRepository repository = new UserRepository(MainActivityV2.this);
                        NfcUser user = new NfcUser(lastNfcId, dni);
                        repository.replaceUser(user);

                        lastNfcId = "";
                        etDni.setText("");
                        showPopupMsg("Usuario creado correctamente, vuelva a acercar el NFC para fichar entrada / salida");
                    } else {
                        showPopupMsg("El dni introducido no tiene un formato válido, reviselo");
                    }
                } else {
                    showPopupMsg("No se ha encontrado un tarjeta Nfc, escanee una e introduzca el Dni para crear el usuario");
                }
            }
        });
    }

    /*
    * IMPORTANT method
    * */
    private void onNfcScan(String nfcId) {
        lastNfcId = nfcId;
        UserRepository repository = new UserRepository(this);
        NfcUser user = repository.getUserById(nfcId);
        if(user == null) {
            showPopupMsg("TODO implement");
            //End execution early
            return;
        }

        //User is already created... Create NfcEntryLog...

    }

    private boolean validateForm() {

        return false;
    }

    private void showPopupMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

}
