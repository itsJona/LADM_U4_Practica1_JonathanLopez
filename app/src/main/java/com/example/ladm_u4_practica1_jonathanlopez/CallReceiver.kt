package com.example.ladm_u4_practica1_jonathanlopez

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast


class CallReceiver : BroadcastReceiver() {
    /*override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Broadcast Intent Detected.",
            Toast.LENGTH_LONG).show()
    }*/

    var phoneNr =""
    var mensaje =""
    var flag =0
    private var prev_state = 0
    private var mTelephonyManager: TelephonyManager? = null
    var isListening = false

    override fun onReceive(context: Context, intent: Intent?) {
        val bundle: Bundle? = intent!!.extras
          phoneNr= bundle?.getString("incoming_number").toString()

        mTelephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val mPhoneStateListener: PhoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {

                super.onCallStateChanged(state, incomingNumber)

                        if (state==TelephonyManager.CALL_STATE_RINGING) {
                            Toast.makeText(context,"Llamada entrante: "+phoneNr,Toast.LENGTH_LONG).show()
                            prev_state=state;
                        }
                        if (state==TelephonyManager.CALL_STATE_OFFHOOK) {
                            prev_state=state;
                        }

                    if (state==TelephonyManager.CALL_STATE_IDLE) {

                        if ((prev_state == TelephonyManager.CALL_STATE_RINGING)) {
                            prev_state = state
                            //Toast.makeText(context, "Llamada entrando desde: "+phoneNr, Toast.LENGTH_SHORT).show()
                            try {
                                var cursor = BaseDatos(
                                    context,
                                    "contactos",
                                    null,
                                    1
                                ).readableDatabase.rawQuery(
                                    "SELECT * FROM CONTACTOS WHERE TELEFONO = '${phoneNr}' AND tipo = 0",
                                    null
                                )
                                if (cursor.count > 0) {
                                    SmsManager.getDefault().sendTextMessage(
                                        phoneNr,
                                        null,
                                        "Deja de estar molestándome, no contestaré tus llamadas.",
                                        null,
                                        null
                                    )
                                    Toast.makeText(context,"Enviando respuesta a: "+phoneNr,Toast.LENGTH_LONG).show()

                                } else { //Toast.makeText(context,"No se encuntra el contacto",Toast.LENGTH_LONG).show()
                                }
                            } catch (err: SQLiteException) {
                                Toast.makeText(context, "Error:" + err.message, Toast.LENGTH_LONG)
                                    .show()
                            }

                            try {
                                var cursor = BaseDatos(
                                    context,
                                    "contactos",
                                    null,
                                    1
                                ).readableDatabase.rawQuery(
                                    "SELECT * FROM CONTACTOS WHERE TELEFONO = '${phoneNr}' AND tipo = 1",
                                    null
                                )
                                if (cursor.count > 0) {
                                    SmsManager.getDefault().sendTextMessage(
                                        phoneNr,
                                        null,
                                        "Por ahora me encuentro en una junta y no puedo contestar. " +
                                                "Si gustas mandarme un mensaje lo leeré cuando termine y te marcaré.",
                                        null,
                                        null
                                    )
                                    Toast.makeText(context,"Enviando respuesta a: "+phoneNr,Toast.LENGTH_LONG).show()
                                } else {
                                    // Toast.makeText(context,"No se encuntra el contacto",Toast.LENGTH_LONG).show()
                                }
                            } catch (err: SQLiteException) {
                                Toast.makeText(context, "Error:" + err.message, Toast.LENGTH_LONG)
                                    .show()
                            }

                        }


                    }
            }
        }
        if (!isListening) {
            mTelephonyManager!!.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            isListening = true
        }
    }


}