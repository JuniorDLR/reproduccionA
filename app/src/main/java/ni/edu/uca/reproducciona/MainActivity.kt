package ni.edu.uca.reproducciona

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import ni.edu.uca.reproducciona.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /*
 Se cra una varibale de manera perezona que toma un lambad y luego lo instancia

 para poder reproducir nuestro fichero mp3 debemos de crear su descriptor,
 assets es la carpeta que contiene nuestas musicas

 */

    private val fd by lazy {
        assets.openFd(cancionActual)
    }

    /*
    Se crea una variable mp que va a representar a dicho objeto y se representara de manera perezosa ,
     no tomara valor hasta que sea llamada en el onCreate.

    */

    private val mp by lazy {

        val m = MediaPlayer()  // la variable m sera una variable de la clase mediaplayer.

        m.setDataSource(    // cargara los datos de nuestra cancion

            fd.fileDescriptor,  //Descriptor de archivo
            fd.startOffset,   //Desplazamiento de inicio
            fd.length // Longitud
        )
        fd.close()  // cerramos el fichero
        m.prepare() // Preparamos  el reproductor de musica
        m

        //mp se va igualar a m , de todo lo que hemos echo de m
    }

    /*
     la variable controllers va hacer una lista de los botones creados junto con el metodo map
      que permite aplicar una función sobre todos los elementos de una colección con el fin de
      una nueva colección con el cálculo final.
     */

    private val controles by lazy {
        listOf(R.id.btnAnterior, R.id.btnStop, R.id.btnPlay, R.id.btnSiguiente).map {
            findViewById<MaterialButton>(it)
            //Una lista utilizando el map que sea de tipo MaterialButton
        }

    }


    //Objeto para que diga el indice de cada boton por si acso en un futuro cambia entonces se modifica
    object ci {
        val anterior = 0
        val stop = 1
        val play = 2
        val siguiente = 3

    }


    //Se crea una variable nombreCancion que toma un lambda  de tipo texView
    // y devuelve una instancia que sera el nombre de la variable creada
    val nombreCancion by lazy {
        findViewById<TextView>(R.id.tvNombreCancion)
    }

    /*
    se crea una variable de funcion perezosa  y una variable intermedia que se le pasara la carpeta de musica
    que nos devuelve un array y lo pasaremos a una lista de kotlin
     */
    private val canciones by lazy {
        val nombreFicheros = assets.list("")?.toList() ?: listOf()


        //Hacemos un filtro de los ficheros  que solo escoga (.mp3)
        nombreFicheros.filter { it.contains(".mp3") }
    }


    //Se crea una variableIndex de la cancionActual sea 0 y si le damos al siguiente sera 1
    private var cancionActualIndex = 0
        set(value) { //le asignamos un valor por que si lo igualamos 11 y tengo 10 canciones habria un fallo

            val v = if (value == -1) {
                //Variable intermedia que si value es igual a -1 que se vaya a la ultima cancion y cual sera?
                // pues el tamaño de la lista de canciones-1
                canciones.size - 1
            } else { //Caso contrario gestionar si estamos en la ultima que hay que hacer pues irse a la posicion 0
                value % canciones.size
            }
            field = v // El campo va hacer igual V
            // a veces es necesario tener un campo de respaldo cuando se usan descriptores de acceso personalizados.

            cancionActual = canciones[v] //A la variable cancionActual va a recibir los datos por medio del campo personalizado
        }



    private lateinit var cancionActual: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Aqui se manda a llamar el texView y le decimos que active el estilo  de marquee
        binding.tvNombreCancion.isSelected = true

        /*Se llama a la lista de controles y hacemos referencia al objeto play,
         luego de manda a llamar la asignacion del boton y le especificamos el metodo */
        controles[ci.play].setOnClickListener(this::iniciar)
        controles[ci.stop].setOnClickListener(this::detener)
        controles[ci.anterior].setOnClickListener(this::atras)
        controles[ci.siguiente].setOnClickListener(this::siguiente)


        //La variable cancionActual le pasamos los datos de canciones  de sus indice de ciclo reproductor
        cancionActual = canciones[cancionActualIndex]

        //Mandamos a llamar el texView y le diremos que recibira los nombre de las canciones para que se pueda mostrar en la app
        nombreCancion.text = cancionActual
    }


    //Metodo  Play que le entrara una vista a ese mismo elemento pulsado
    private fun iniciar(v: View) {
        if (!mp.isPlaying) { //Si mi reproductor no se esta ejecuntado
            mp.start()  // que se inicie

            //Se manda a llamar la lista y se hace referencia al objeto y le asignamos el icono
            controles[ci.play].setIconResource(R.drawable.ic_baseline_pause_48)

            //Mandamos a llamar el texview y que su visibilidad se ponga invisible
            nombreCancion.visibility = View.VISIBLE

        } else {      //Caso contrario si la cancion se  esta ejecutando ,que la pause!
            mp.pause()


            //Se manda a llamar la lista y se hace referencia al objeto y le asignamos el icono
            controles[ci.play].setIconResource(R.drawable.ic_baseline_play_arrow_48)


        }
    }

    //Metodo detener Musica que le entrara una vista de ese mismo elemento pulsado
    private fun detener(v: View) {
        if (mp.isPlaying) { //Si la musica se esta reproduciendo , pausala
            mp.pause()

            //Se manda a llamar la lista y se hace referencia al objeto y le asignamos el icono
            controles[ci.play].setIconResource(R.drawable.ic_baseline_play_arrow_48)

            //Mandamos a llamar el texview y que su visibilidad se ponga visible
            nombreCancion.visibility = View.INVISIBLE
        }
        mp.seekTo(0) //Una vez pausado lo que hace el SeekTo es devolverlo al 0
    }


    //Metodo para enlazar boton siguiente
    private fun siguiente(v: View) {

        //LLamar el metodo para pasar a la siguiente cancion y se refrescara
        cancionActualIndex++
        refrescarCancion()
    }


    //Metodo para enlazar boton atras
    private fun atras(v: View) {

        //LLamar el metodo para retroceder la  cancion y se refrescara
        cancionActualIndex--
        refrescarCancion()

    }
    //Funcion para refrescar las canciones
    private fun refrescarCancion() {
        mp.reset() //Reseteamos el reproductor
        val fd = assets.openFd(cancionActual) // Creamos el descriptor para podre reproducirlo

        //Cargamos los datos con SetDataSource con sus 3 metodos
        mp.setDataSource(
            fd.fileDescriptor,
            fd.startOffset,
            fd.length
        )
        mp.prepare()//Preparamos el reproductor

        /*
         Mandamos a llamar el metodo play con la lista de controles y hacemos referencia con el objeto
         y le decimos que que el texView actualizara el nombre de la cancion

        */
        iniciar(controles[ci.play])
        nombreCancion.text = cancionActual

    }


}



