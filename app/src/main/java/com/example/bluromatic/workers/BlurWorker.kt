package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG ="BlurWorker"

class BlurWorker(ctx : Context , params:WorkerParameters):CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        //crear un bloque withContext(), dentro la llamada pasamos Dispatchers.IO para
        //que funcion lambda se ejecute en un grupo de subprocesos especial  para bloquear
        //las operaciones IO

        return withContext(Dispatchers.IO) {


        //agregamos el bloaue de codigo return try...catch
        return@withContext try {

            //debido a que worker se ejecuta muy rapido se recomienda  agragar una demora para
            //emular que se ejecuta  con lentitud
            delay(DELAY_TIME_MILLIS)



            //creamos una variable picture, propagala con el mapa de bits despues de llamar el
            //mapa el metodo BitmapFactoru.decodeResourse()
            val picture = BitmapFactory.decodeResource(
                applicationContext.resources,
                R.drawable.android_cupcake
            )

            //desenfocamos el mapa de bits llamando a la funcion blurBitmap() ,
            //pasamos la variable picture y un valor de 1 para el parametro blurLevel
            val output = blurBitmap(picture, 1)

            //creamos una variable outputUri con una llamada a la funcion writeBitmapToFile()
            //pasamos el contexto de la aplicacion y la variable output como argumentos
            val outputUri = writeBitmapToFile(applicationContext, output)

            //mostramos un mensaje de notificacion que contenga la variable outputUri
            makeStatusNotification(
                "Output is $outputUri",
                applicationContext
            )


            // en el bloque try agremgamos una llamda a Result.succes()
            Result.success()

        } catch (throwable: Throwable) {

            //registramos un mensaje de error para indicar que se produjo un error al intentar
            //desenfocar la imagen
            Log.e(
                TAG,
                applicationContext.resources.getString(R.string.error_applying_blur),
                throwable
            )


            //en el bloque cach agregamso auna llamada a Result.failure
            Result.failure()

        }

    }
    }
}