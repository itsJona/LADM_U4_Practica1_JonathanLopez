package com.example.ladm_u4_practica1_jonathanlopez

import android.app.Dialog
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    val siPermiso = 1
    val siPermiso2 = 2
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listaContactos()


        //PERMISOS

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE),siPermiso)
        }


        btnAlta.setOnClickListener {

            var registrarNum = Dialog(this)
            registrarNum.setContentView(R.layout.registrar_numero)

            var txtName = registrarNum.findViewById<EditText>(R.id.txtNombre)
            var txtPhone = registrarNum.findViewById<EditText>(R.id.txtCelular)
            var btnReg = registrarNum.findViewById<Button>(R.id.btnRegistrar)
            var btnCancel = registrarNum.findViewById<Button>(R.id.btnCancelar)
            var radDeseado = registrarNum.findViewById<RadioButton>(R.id.rbtDeseado)
            var radNoDeseado = registrarNum.findViewById<RadioButton>(R.id.rbtNoDeseado)

            btnReg.setOnClickListener {
                if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS),siPermiso2)
                }

                if(txtName.text.toString().isEmpty()){
                    Toast.makeText(this,"Ingrese un nombre",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if(txtPhone.text.toString().isEmpty()){
                    Toast.makeText(this,"Ingrese un número de celular",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                var tipo= 0
                if(radDeseado.isChecked){tipo=1}
                if(radNoDeseado.isChecked){tipo=0}

                try {
                    var baseDatos = BaseDatos(this,"contactos",null,1)
                    var insertar = baseDatos.writableDatabase
                    var sql = "INSERT INTO CONTACTOS VALUES ('${txtName.text.toString()}','${txtPhone.text.toString()}',${tipo})"

                    insertar.execSQL(sql)
                    baseDatos.close()
                    Toast.makeText(this,"Se registró con éxito", Toast.LENGTH_LONG).show()
                    registrarNum.dismiss()
                }catch (err: SQLiteException){
                    Toast.makeText(this,"Error: Este número ya se encuentra registrado",Toast.LENGTH_LONG).show()
                }

                listaContactos()

            }
            btnCancel.setOnClickListener{
                registrarNum.dismiss()
                listaContactos()
            }
            registrarNum.show()
        }
    }


    private fun listaContactos(){

        try{
            var cursor = BaseDatos(this,"contactos",null,1)
                .readableDatabase
                .rawQuery("SELECT * FROM CONTACTOS", null)
            var registro=""
            var arreglo = ArrayList<String>()

            arreglo.clear()
            listaID.clear()
            if(cursor.count>0){
                cursor.moveToFirst()
                var cantidad = cursor.count-1
                var type=""
                (0..cantidad).forEach{

                    if(cursor.getInt(2)==0){type="No deseado"}
                    if(cursor.getInt(2)==1){type="Deseado"}

                   registro ="\nNombre: "+cursor.getString(0)+
                              "\nCelular: "+cursor.getString(1)+
                            "  \nTipo: "+ type +"\n"

                    arreglo.add(registro)
                    listaID.add(cursor.getString(1))
                    cursor.moveToNext()
                }
                lista.adapter = ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,arreglo)
                lista.setOnItemClickListener { parent, view, position, id ->
                    AlertDialog.Builder(this)
                        .setTitle("Atención")
                        .setMessage("¿Qué desea hacer con: "+listaID[position])
                        .setPositiveButton("Eliminar"){d,i->
                            eliminarPorID(listaID[position])
                        }
                        .setNeutralButton("Cambiar Tipo"){d,i ->
                            actualizarPorID(listaID[position])
                        }
                        .setNegativeButton("Cancelar") {d,i->
                            d.dismiss()
                        }
                        .show()

                }
            }else{
                registro="Sin contactos registrados."
                arreglo.add(registro)
                lista.adapter = ArrayAdapter<String> (this,android.R.layout.simple_list_item_1,arreglo)
            }
        }catch (err:SQLiteException){
            Toast.makeText(this,"Error:"+err.message,Toast.LENGTH_LONG).show()
        }
    }
    fun eliminarPorID(id: String) {
        try{
            var baseDatos=BaseDatos(this,"contactos",null,1)
            var eliminar = baseDatos.writableDatabase
            var query = "DELETE FROM CONTACTOS WHERE TELEFONO = '${id}' "
            eliminar.execSQL(query)
            Toast.makeText(this,"Se eliminó este contacto",Toast.LENGTH_LONG).show()
            eliminar.close()
            baseDatos.close()
            listaContactos()
        }catch (error:SQLiteException){
            Toast.makeText(this,"Error: "+ error,Toast.LENGTH_LONG).show()
        }

    }

    fun actualizarPorID(id: String) {
        var cambiarTipo = Dialog(this)
        cambiarTipo.setContentView(R.layout.cambiar_tipo)

        var btnD = cambiarTipo.findViewById<RadioButton>(R.id.deseado)
        var btnND = cambiarTipo.findViewById<RadioButton>(R.id.noDeseado)
        var btnG = cambiarTipo.findViewById<Button>(R.id.guardar)
        var btnC = cambiarTipo.findViewById<Button>(R.id.cancelar)
        var newTipe = 0


        btnG.setOnClickListener {
            if (btnD.isChecked) {
                newTipe = 1
            }
            if (btnND.isChecked) {
                newTipe = 0
            }
        try {
            var baseDatos = BaseDatos(this, "contactos", null, 1)
            var actualizar = baseDatos.writableDatabase
            var query = "UPDATE CONTACTOS SET tipo = ${newTipe} WHERE TELEFONO= '${id}'"
            actualizar.execSQL(query)
            Toast.makeText(this, "Se cambio el tipo de contacto: "+ id, Toast.LENGTH_LONG).show()
            actualizar.close()
            baseDatos.close()

            cambiarTipo.dismiss()
                listaContactos()
        } catch (error: SQLiteException) {
            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show()
        }
    }
        btnC.setOnClickListener {
            cambiarTipo.dismiss()
        }
        cambiarTipo.show()
    }



}

