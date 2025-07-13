package com.codigocreativo.mobile.features.equipos

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.modelo.SelectorModeloFragment
import com.codigocreativo.mobile.features.paises.SelectorPaisFragment
import com.codigocreativo.mobile.features.proveedores.SelectorProveedorFragment
import com.codigocreativo.mobile.features.tipoEquipo.SelectorTipoEquipoFragment
import com.codigocreativo.mobile.features.ubicacion.SelectorUbicacionFragment
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import android.app.DatePickerDialog
import java.io.ByteArrayOutputStream
import java.util.Calendar


class IngresarEquipoFragment(
    private val onConfirm: (EquipoRequest) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        private const val DEFAULT_DATE = "2024-01-01"
    }

    private lateinit var nombreInput: EditText
    private lateinit var btnConfirmar: Button
    private lateinit var modeloPickerFragment: SelectorModeloFragment
    private lateinit var paisPickerFragment: SelectorPaisFragment
    private lateinit var tipoEquipoPickerFragment: SelectorTipoEquipoFragment
    private lateinit var proveedorPickerFragment: SelectorProveedorFragment
    private lateinit var ubicacionPickerFragment: SelectorUbicacionFragment
    private lateinit var nroSerieInput: EditText
    private lateinit var garantiaInput: EditText
    private lateinit var fechaAdquisicionInput: EditText
    private lateinit var identificacionInternaInput: EditText
    private lateinit var imagenImageView: ImageView
    private var imagenUri: Uri? = null
    private var imageUrl: String? = null // Para almacenar la URL de la imagen subida

    // Registrar actividad para recibir la imagen seleccionada
    private val pickImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imagenUri = data?.data
            imagenUri?.let {
                imagenImageView.setImageURI(it)
                subirImagen(it) // Subir la imagen inmediatamente después de seleccionarla
            }
        }
    }

    // Función para convertir la imagen a Base64
    private fun convertirImagenABase64(uri: Uri): String? {
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    // Función para subir la imagen a imgbb
    private fun subirImagen(uri: Uri) {
        val base64Image = convertirImagenABase64(uri)
        if (base64Image != null) {
            lifecycleScope.launch {
                val apiKey = "7c25531eca2149d7618fe5241473b513"
                try {
                    // Usa RetrofitClient para obtener la instancia de imgbb
                    val imgBBService = RetrofitClient.getImgBBClient().create(ImgBBService::class.java)
                    val response = imgBBService.subirImagen(apiKey, base64Image)

                    if (response.isSuccessful) {
                        imageUrl = response.body()?.data?.url
                        if (imageUrl != null) {
                            Log.d("IngresarEquipoFragment", "Imagen subida: $imageUrl")
                            Snackbar.make(requireView(), "Imagen subida correctamente", Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("IngresarEquipoFragment", "Error al subir la imagen: ${response.errorBody()?.string()}")
                        Snackbar.make(requireView(), "Error al subir la imagen", Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("IngresarEquipoFragment", "Error inesperado: ${e.message}", e)
                    Snackbar.make(requireView(), "Error inesperado al subir la imagen", Snackbar.LENGTH_LONG).show()
                }
            }
        } else {
            Snackbar.make(requireView(), "Error al convertir la imagen", Snackbar.LENGTH_LONG).show()
        }
    }

    private val dataRepository = DataRepository()

    // Función para formatear la fecha al formato esperado por el servidor (YYYY-MM-DD)
    private fun formatearFecha(fecha: String): String {
        return try {
            // Si la fecha está en formato DD/MM/YYYY, convertirla a YYYY-MM-DD
            if (fecha.contains("/")) {
                val partes = fecha.split("/")
                if (partes.size == 3) {
                    val dia = partes[0].padStart(2, '0')
                    val mes = partes[1].padStart(2, '0')
                    val anio = partes[2]
                    "$anio-$mes-$dia"
                } else {
                    fecha
                }
            } else {
                fecha
            }
        } catch (e: Exception) {
            Log.e("IngresarEquipoFragment", "Error formateando fecha: ${e.message}")
            DEFAULT_DATE // Fecha por defecto
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val (year, month, day) = Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            editText.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay))
        }, year, month, day).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_equipo, container, false)

        // Inicializar vistas
        nombreInput = view.findViewById(R.id.nombreInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        modeloPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorModelo) as SelectorModeloFragment
        paisPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorPais) as SelectorPaisFragment
        tipoEquipoPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorTipoEquipo) as SelectorTipoEquipoFragment
        proveedorPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorProveedor) as SelectorProveedorFragment
        ubicacionPickerFragment = childFragmentManager.findFragmentById(R.id.fragmentSelectorUbicacion) as SelectorUbicacionFragment
        nroSerieInput = view.findViewById(R.id.serieInput)
        garantiaInput = view.findViewById(R.id.garantiaInput)
        fechaAdquisicionInput = view.findViewById(R.id.fechaAdquisicionInput)
        identificacionInternaInput = view.findViewById(R.id.identificacionInternaInput)
        imagenImageView = view.findViewById(R.id.imagenImageView)

        // Permitir seleccionar imagen al hacer click
        imagenImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImageResult.launch(intent)
        }

        // Configurar DatePicker para garantía
        garantiaInput.setOnClickListener {
            showDatePicker(garantiaInput)
        }
        // Configurar DatePicker para fecha de adquisición
        fechaAdquisicionInput.setOnClickListener {
            showDatePicker(fechaAdquisicionInput)
        }

        // Configurar el botón de confirmación
        btnConfirmar.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val identificacionInterna = identificacionInternaInput.text.toString().trim()
            val nroSerie = nroSerieInput.text.toString().trim()
            val garantia = garantiaInput.text.toString().trim()
            val fechaAdquisicion = fechaAdquisicionInput.text.toString().trim()
            val modelo = modeloPickerFragment["selectedModelo"]
            val pais = paisPickerFragment["selectedCountry"]
            val tipoEquipo = tipoEquipoPickerFragment["selectedTipo"]
            val proveedor = proveedorPickerFragment["selectedProveedor"]
            val ubicacion = ubicacionPickerFragment["selectedUbicacion"]

            if (nombre.isEmpty() || identificacionInterna.isEmpty() || nroSerie.isEmpty() || garantia.isEmpty() || fechaAdquisicion.isEmpty() || modelo == null || pais == null || tipoEquipo == null || proveedor == null || ubicacion == null) {
                Snackbar.make(view, "Todos los campos son obligatorios", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (imageUrl == null) {
                Snackbar.make(view, "Por favor espera a que la imagen se suba", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val equipo = crearEquipo(imageUrl!!)
            val equipoRequest = convertirAEquipoRequest(equipo)
            onConfirm(equipoRequest)
            dismiss()
        }

        return view
    }

    private fun crearEquipo(imagenUrl: String): Equipo {
        val modelo = modeloPickerFragment["selectedModelo"]
        val pais = paisPickerFragment["selectedCountry"]
        val tipoEquipo = tipoEquipoPickerFragment["selectedTipo"]
        val proveedor = proveedorPickerFragment["selectedProveedor"]
        val ubicacion = ubicacionPickerFragment["selectedUbicacion"]

        val nombre = nombreInput.text.toString().trim()
        val nroSerie = nroSerieInput.text.toString().trim()
        val garantia = formatearFecha(garantiaInput.text.toString().takeIf { it.isNotEmpty() } ?: DEFAULT_DATE)
        val fechaAdquisicion = formatearFecha(fechaAdquisicionInput.text.toString().takeIf { it.isNotEmpty() } ?: DEFAULT_DATE)
        val estado = Estado.ACTIVO
        val identificacionInterna = identificacionInternaInput.text.toString().trim()

        return Equipo(
            id = null,
            nombre = nombre,
            idModelo = modelo,
            estado = estado,
            equiposUbicaciones = emptyList(),
            fechaAdquisicion = fechaAdquisicion,
            garantia = garantia,
            idInterno = identificacionInterna,
            idPais = pais,
            idProveedor = proveedor,
            idTipo = tipoEquipo,
            imagen = imagenUrl,
            nroSerie = nroSerie,
            idUbicacion = ubicacion,
            descripcion = null
        )
    }

    // Función para convertir Equipo a EquipoRequest (objetos completos)
    private fun convertirAEquipoRequest(equipo: Equipo): EquipoRequest {
        return EquipoRequest(
            id = equipo.id,
            nombre = equipo.nombre,
            idModelo = equipo.idModelo,
            estado = equipo.estado,
            equiposUbicaciones = equipo.equiposUbicaciones,
            fechaAdquisicion = equipo.fechaAdquisicion,
            garantia = equipo.garantia,
            idInterno = equipo.idInterno,
            idPais = equipo.idPais,
            idProveedor = equipo.idProveedor,
            idTipo = equipo.idTipo,
            idUbicacion = equipo.idUbicacion,
            imagen = equipo.imagen,
            nroSerie = equipo.nroSerie,
            descripcion = equipo.descripcion
        )
    }
}
