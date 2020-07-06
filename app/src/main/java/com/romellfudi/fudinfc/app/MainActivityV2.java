package com.romellfudi.fudinfc.app;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.romellfudi.fudinfc.app.data.EntryType;
import com.romellfudi.fudinfc.app.data.NfcEntryLog;
import com.romellfudi.fudinfc.app.data.NfcUser;
import com.romellfudi.fudinfc.app.data.UserRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class MainActivityV2 extends AppCompatActivity {

    // list of NFC technologies detected:
    private final String[][] techList = new String[][]{
            new String[]{
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
    private TextView tvLastLogValue = null;
    private Button btnExportData = null;

    private String lastNfcId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setListeners();

        paintLastLog();
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
            String id = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
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
        tvLastLogValue = findViewById(R.id.tv_last_log_value);
        btnExportData = findViewById(R.id.btn_export_data);
    }

    private void setListeners() {
        btnSimulateNfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nfcId = etNfcId.getText().toString().trim();
                if (!nfcId.isEmpty()) {
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
                if (!lastNfcId.trim().isEmpty()) {
                    String dni = etDni.getText().toString().trim();
                    if (Utils.isValidDni(dni)) {
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

        btnExportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportEntryLogData();
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
        if (user == null) {
            showPopupMsg("Usuario no creado, introduzca el DNI, cree el usuario y vuelva a escanear el NFC");
            //End execution early
            return;
        }

        //User is already created... Create NfcEntryLog...
        //TODO should control if user doesn't have last Entry.IN don't allow Entry.OUT?? and viceversa? to ensure pairing IN <-> OUT....
        EntryType type = null;
        if (rgType.getCheckedRadioButtonId() == R.id.rb_entrada) {
            type = EntryType.IN;
        } else if (rgType.getCheckedRadioButtonId() == R.id.rb_salida) {
            type = EntryType.OUT;
        } else {
            throw new IllegalStateException("Expected to always have a checked radio button...");
        }
        repository.insertLog(nfcId, type);
        paintLastLog();
    }

    private void paintLastLog() {
        NfcEntryLog lastLog = new UserRepository(this).getLastLog();
        if (lastLog != null) {
            tvLastLogValue.setText(lastLog.getPrettyPrint());
        } else {
            tvLastLogValue.setText("...");
        }
    }

    private void showPopupMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private void exportEntryLogData() {
        //Request permission to write to external storage to export jsonData using library
        //https://github.com/Karumi/Dexter
        //https://developer.android.com/training/permissions/requesting
        Dexter.withContext(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        String jsonData = new UserRepository(MainActivityV2.this).getEntryLogJson();
                        //showPopupMsg(jsonData);
                        File downloadsFolder = getExternalCacheDir();
                        File jsonFile = createFileFromJson(jsonData, downloadsFolder);
                        shareFile(jsonFile);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();
    }

    private File createFileFromJson(String json, File folder) {
        FileWriter file = null;
        String fileName = "export_fichajes_" + Calendar.getInstance().getTime().getTime() + ".json";
        try {
            // Constructs a FileWriter given a file name, using the platform's default charset
            file = new FileWriter(new File(folder, fileName));
            file.write(json);
            //showPopupMsg("Datos exportados correctamente en la carpeta: " + folder);
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new File(folder, fileName);
    }

    private void shareFile(File file) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(shareIntent, "Enviar datos exportados a ..."));
    }
}
