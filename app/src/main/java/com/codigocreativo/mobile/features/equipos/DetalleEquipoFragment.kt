package com.codigocreativo.mobile.features.equipos

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.codigocreativo.mobile.R
import com.codigocreativo.mobile.features.marca.Marca
import com.codigocreativo.mobile.features.marca.MarcaApiService
import com.codigocreativo.mobile.features.modelo.Modelo
import com.codigocreativo.mobile.features.modelo.ModeloApiService
import com.codigocreativo.mobile.features.paises.Pais
import com.codigocreativo.mobile.features.paises.PaisApiService
import com.codigocreativo.mobile.features.proveedores.Proveedor
import com.codigocreativo.mobile.features.proveedores.ProveedoresApiService
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipo
import com.codigocreativo.mobile.features.tipoEquipo.TipoEquipoApiService
import com.codigocreativo.mobile.features.ubicacion.Ubicacion
import com.codigocreativo.mobile.features.ubicacion.UbicacionesApiService
import com.codigocreativo.mobile.network.DataRepository
import com.codigocreativo.mobile.network.RetrofitClient
import com.codigocreativo.mobile.utils.Estado
import com.codigocreativo.mobile.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class DetalleEquipoFragment(
    private val equipo: Equipo,
    private val onEdit: (Equipo) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var nombreInput: TextInputEditText
    private lateinit var identificacionInternaInput: TextInputEditText
    private lateinit var nroSerieInput: TextInputEditText
    private lateinit var garantiaInput: TextInputEditText
    private lateinit var fechaAdquisicionInput: TextInputEditText
    private lateinit var tipoEquipoInput: TextInputEditText
    private lateinit var marcaInput: TextInputEditText
    private lateinit var modeloInput: TextInputEditText
    private lateinit var paisInput: TextInputEditText
    private lateinit var proveedorInput: TextInputEditText
    private lateinit var ubicacionInput: TextInputEditText
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var estadoSpinner: Spinner
    private lateinit var imagenEquipo: ImageView

    // Variables para almacenar las entidades seleccionadas
    private var selectedTipoEquipo: TipoEquipo? = equipo.idTipo
    private var selectedMarca: Marca? = equipo.idModelo?.idMarca
    private var selectedModelo: Modelo? = equipo.idModelo
    private var selectedPais: Pais? = equipo.idPais
    private var selectedProveedor: Proveedor? = equipo.idProveedor
    private var selectedUbicacion: Ubicacion? = equipo.idUbicacion

    private val dataRepository = DataRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_equipo, container, false)

        // Inicializar las vistas
        nombreInput = view.findViewById(R.id.nombreInput)
        identificacionInternaInput = view.findViewById(R.id.identificacionInternaInput)
        nroSerieInput = view.findViewById(R.id.nroSerieInput)
        garantiaInput = view.findViewById(R.id.garantiaInput)
        fechaAdquisicionInput = view.findViewById(R.id.fechaAdquisicionInput)
        tipoEquipoInput = view.findViewById(R.id.tipoEquipoInput)
        marcaInput = view.findViewById(R.id.marcaInput)
        modeloInput = view.findViewById(R.id.modeloInput)
        paisInput = view.findViewById(R.id.paisInput)
        proveedorInput = view.findViewById(R.id.proveedorInput)
        ubicacionInput = view.findViewById(R.id.ubicacionInput)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        estadoSpinner = view.findViewById(R.id.estadoSpinner)
        imagenEquipo = view.findViewById(R.id.imagenEquipo)

        // Populate fields with data from the equipo object
        nombreInput.setText(equipo.nombre ?: "")
        identificacionInternaInput.setText(equipo.idInterno ?: "")
        nroSerieInput.setText(equipo.nroSerie ?: "")
        garantiaInput.setText(equipo.garantia ?: "")
        fechaAdquisicionInput.setText(equipo.fechaAdquisicion ?: "")
        
        // Mostrar nombres en lugar de IDs para los campos de relación
        tipoEquipoInput.setText(equipo.idTipo?.nombreTipo ?: "")
        marcaInput.setText(equipo.idModelo?.idMarca?.nombre ?: "")
        modeloInput.setText(equipo.idModelo?.nombre ?: "")
        paisInput.setText(equipo.idPais?.nombre ?: "")
        proveedorInput.setText(equipo.idProveedor?.nombre ?: "")
        ubicacionInput.setText(equipo.idUbicacion?.nombre ?: "")

        // Configurar click listeners para los campos de selección
        setupSelectorClickListeners()

        // Cargar la imagen del equipo
        cargarImagenEquipo()

        // Configurar click en la imagen para ver en pantalla completa
        view.findViewById<View>(R.id.imagenEquipo).parent.let { parent ->
            if (parent is View) {
                parent.setOnClickListener {
                    mostrarImagenPantallaCompleta()
                }
            }
        }

        // Populate estadoSpinner with Estado enum values
        val estadoAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Estado.entries.toTypedArray()
        )
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = estadoAdapter
        equipo.estado?.let {
            estadoSpinner.setSelection(Estado.entries.indexOf(it))
        }

        // Configurar el botón de confirmar
        btnConfirmar.setOnClickListener {
            val nuevoNombre = nombreInput.text.toString().trim()
            val nuevoIdentificacionInterna = identificacionInternaInput.text.toString().trim()
            val nuevoNroSerie = nroSerieInput.text.toString().trim()
            val nuevaGarantia = garantiaInput.text.toString().trim()
            val nuevaFechaAdquisicion = fechaAdquisicionInput.text.toString().trim()
            val nuevoModelo = selectedModelo
            val nuevoPais = selectedPais
            val nuevoTipoEquipo = selectedTipoEquipo
            val nuevoProveedor = selectedProveedor
            val nuevaUbicacion = selectedUbicacion

            if (nuevoNombre.isEmpty() || nuevoIdentificacionInterna.isEmpty() || nuevoNroSerie.isEmpty() || nuevaGarantia.isEmpty() || nuevaFechaAdquisicion.isEmpty() || nuevoModelo == null || nuevoPais == null || nuevoTipoEquipo == null || nuevoProveedor == null || nuevaUbicacion == null) {
                Snackbar.make(view, "Todos los campos son obligatorios", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val nuevoEstado = Estado.entries[estadoSpinner.selectedItemPosition]

            val nuevoEquipo = Equipo(
                equiposUbicaciones = equipo.equiposUbicaciones,
                estado = nuevoEstado,
                fechaAdquisicion = nuevaFechaAdquisicion,
                garantia = nuevaGarantia,
                id = equipo.id,
                idInterno = nuevoIdentificacionInterna,
                idModelo = nuevoModelo,
                idPais = nuevoPais,
                idProveedor = nuevoProveedor,
                idTipo = nuevoTipoEquipo,
                idUbicacion = nuevaUbicacion,
                imagen = equipo.imagen,
                nombre = nuevoNombre,
                nroSerie = nuevoNroSerie
            )
            onEdit(nuevoEquipo)
            dismiss()
        }

        return view
    }

    private fun setupSelectorClickListeners() {
        // Tipo de Equipo
        tipoEquipoInput.setOnClickListener {
            mostrarSelectorTipoEquipo()
        }

        // Marca
        marcaInput.setOnClickListener {
            mostrarSelectorMarca()
        }

        // Modelo
        modeloInput.setOnClickListener {
            mostrarSelectorModelo()
        }

        // País
        paisInput.setOnClickListener {
            mostrarSelectorPais()
        }

        // Proveedor
        proveedorInput.setOnClickListener {
            mostrarSelectorProveedor()
        }

        // Ubicación
        ubicacionInput.setOnClickListener {
            mostrarSelectorUbicacion()
        }
    }

    private fun mostrarSelectorTipoEquipo() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(TipoEquipoApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    apiService.listarTipoEquipos("Bearer $token")
                }

                result.onSuccess { tiposEquipo ->
                    mostrarDialogoSeleccion(
                        "Seleccionar Tipo de Equipo",
                        tiposEquipo.map { it.nombreTipo },
                        tiposEquipo
                    ) { tipoEquipo ->
                        selectedTipoEquipo = tipoEquipo
                        tipoEquipoInput.setText(tipoEquipo.nombreTipo)
                    }
                }.onFailure { exception ->
                    Snackbar.make(requireView(), "Error al cargar tipos de equipo", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarSelectorMarca() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(MarcaApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    apiService.listarMarcas("Bearer $token")
                }

                result.onSuccess { marcas ->
                    mostrarDialogoSeleccion(
                        "Seleccionar Marca",
                        marcas.map { it.nombre },
                        marcas
                    ) { marca ->
                        selectedMarca = marca
                        marcaInput.setText(marca.nombre)
                        // Reset modelo cuando se cambia la marca
                        selectedModelo = null
                        modeloInput.setText("")
                    }
                }.onFailure { exception ->
                    Snackbar.make(requireView(), "Error al cargar marcas", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarSelectorModelo() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(ModeloApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    apiService.listarModelos("Bearer $token")
                }

                result.onSuccess { modelos ->
                    mostrarDialogoSeleccion(
                        "Seleccionar Modelo",
                        modelos.map { it.nombre },
                        modelos
                    ) { modelo ->
                        selectedModelo = modelo
                        modeloInput.setText(modelo.nombre)
                        // Actualizar marca cuando se selecciona un modelo
                        selectedMarca = modelo.idMarca
                        marcaInput.setText(modelo.idMarca?.nombre ?: "")
                    }
                }.onFailure { exception ->
                    Snackbar.make(requireView(), "Error al cargar modelos", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarSelectorPais() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(PaisApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    apiService.listarPaises("Bearer $token")
                }

                result.onSuccess { paises ->
                    mostrarDialogoSeleccion(
                        "Seleccionar País",
                        paises.map { it.nombre },
                        paises
                    ) { pais ->
                        selectedPais = pais
                        paisInput.setText(pais.nombre)
                    }
                }.onFailure { exception ->
                    Snackbar.make(requireView(), "Error al cargar países", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarSelectorProveedor() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(ProveedoresApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    apiService.listarProveedores("Bearer $token")
                }

                result.onSuccess { proveedores ->
                    mostrarDialogoSeleccion(
                        "Seleccionar Proveedor",
                        proveedores.map { it.nombre },
                        proveedores
                    ) { proveedor ->
                        selectedProveedor = proveedor
                        proveedorInput.setText(proveedor.nombre)
                    }
                }.onFailure { exception ->
                    Snackbar.make(requireView(), "Error al cargar proveedores", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarSelectorUbicacion() {
        val token = SessionManager.getToken(requireContext())
        if (token != null) {
            val retrofit = RetrofitClient.getClient(token)
            val apiService = retrofit.create(UbicacionesApiService::class.java)

            lifecycleScope.launch {
                val result = dataRepository.obtenerDatos(token) {
                    apiService.listar("Bearer $token")
                }

                result.onSuccess { ubicaciones ->
                    mostrarDialogoSeleccion(
                        "Seleccionar Ubicación",
                        ubicaciones.map { it.nombre },
                        ubicaciones
                    ) { ubicacion ->
                        selectedUbicacion = ubicacion
                        ubicacionInput.setText(ubicacion.nombre)
                    }
                }.onFailure { exception ->
                    Snackbar.make(requireView(), "Error al cargar ubicaciones", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun <T> mostrarDialogoSeleccion(
        titulo: String,
        opciones: List<String>,
        items: List<T>,
        onSeleccion: (T) -> Unit
    ) {
        val dialog = Dialog(requireContext())
        dialog.setTitle(titulo)
        dialog.setCancelable(true)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, opciones)
        val listView = android.widget.ListView(requireContext())
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            onSeleccion(items[position])
            dialog.dismiss()
        }

        dialog.setContentView(listView)
        dialog.show()
    }

    private fun cargarImagenEquipo() {
        // Si el equipo tiene una imagen válida, cargarla con Glide
        if (!equipo.imagen.isNullOrBlank() && equipo.imagen != "null") {
            Glide.with(requireContext())
                .load(equipo.imagen)
                .placeholder(R.drawable.equipos)
                .error(R.drawable.equipos)
                .centerCrop()
                .into(imagenEquipo)
        } else {
            // Si no hay imagen, mostrar la imagen por defecto
            imagenEquipo.setImageResource(R.drawable.equipos)
        }
    }

    private fun mostrarImagenPantallaCompleta() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        val imageView = ImageView(requireContext())
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Cargar la imagen en pantalla completa
        if (!equipo.imagen.isNullOrBlank() && equipo.imagen != "null") {
            Glide.with(requireContext())
                .load(equipo.imagen)
                .placeholder(R.drawable.equipos)
                .error(R.drawable.equipos)
                .fitCenter()
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.equipos)
        }

        dialog.setContentView(imageView)
        dialog.show()
    }
}
