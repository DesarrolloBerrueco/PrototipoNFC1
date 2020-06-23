/*
 * Copyright (c) 2020. BoostTag E.I.R.L. Romell D.Z.
 * All rights reserved
 * porfile.romellfudi.com
 */
package com.romellfudi.fudinfc.app

import android.app.ProgressDialog
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.rbddevs.splashy.Splashy
import com.romellfudi.fudinfc.app.di.component.DaggerNFCComponent
import com.romellfudi.fudinfc.gear.NfcAct
import com.romellfudi.fudinfc.gear.interfaces.OpCallback
import com.romellfudi.fudinfc.gear.interfaces.TaskCallback
import com.romellfudi.fudinfc.util.async.WriteCallbackNfc
import com.romellfudi.fudinfc.util.interfaces.NfcReadUtility
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : NfcAct() {

    @Inject
    lateinit var mProgressDialog: ProgressDialog
    @Inject
    lateinit var splashy: Splashy
    @Inject
    lateinit var mNfcReadUtility: NfcReadUtility
    @Inject
    lateinit var mTaskCallback: TaskCallback

    var mOpCallback: OpCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerNFCComponent.factory().create(this).inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        splashy.show()
        recordButton.setOnClickListener {
            if (dniText != null) {
                val dnitext = dniText.text.toString()
                val fechaentrada = fechaEntradaText.text.toString()
                val fechasalida = fechaSalidaText.text.toString()
                //NdefMessage ndefmessage = new NdefMessage
                var message = NdefMessage(
                        arrayOf(
                                NdefRecord.createTextRecord("en", dnitext),
                                NdefRecord.createTextRecord("en", fechaentrada),
                                NdefRecord.createTextRecord("en", fechasalida)
                        )
                )




                mOpCallback = OpCallback { it.writeEmailToTagFromIntent(dnitext, null, null, intent) }
                mOpCallback = OpCallback { it.writeTextToTagFromIntent(dnitext, intent)}
                mOpCallback = OpCallback { it.writeNdefMessageToTagFromIntent(message, intent)}
                showDialog()
            }
        }


        // Borrar en caso de que no funcione
        //readButton.setOnClickListener {
        //    val intent = Intent(this, MainActivity::class.java)
         //   startActivity(intent)

            /////////////////////
        enableBeam()
    }

    override fun onPause() {
        super.onPause()
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this)
        }
    }

    public override fun onNewIntent(paramIntent: Intent) {
        super.onNewIntent(paramIntent)
        if (mOpCallback != null && mProgressDialog != null && mProgressDialog!!.isShowing) {
            WriteCallbackNfc(mTaskCallback, mOpCallback!!).executeWriteOperation()
            mOpCallback = null
        } else {
            for (data in mNfcReadUtility.readFromTagWithMap(paramIntent).values)
                Toast.makeText(this, data, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNoInputToast() {
        Toast.makeText(this, getString(R.string.no_input), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mProgressDialog.dismiss()
    }

    fun showDialog() {
        mProgressDialog.setTitle(R.string.progressdialog_waiting_for_tag)
        mProgressDialog.setMessage(getString(R.string.progressdialog_waiting_for_tag_message))
        mProgressDialog.show()
    }

    private fun retrieveElement(id: Int): TextView? {
        val element = findViewById<View>(id) as TextView
        return if (element != null && (findViewById<View>(id) as TextView).text != null &&
                "" != (findViewById<View>(id) as TextView).text.toString()) element else null
    }
}