package com.devmastercrack.finia.presentation

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.util.Log
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

@Composable
fun ImageLabelingScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiModel = remember { 
        // Actualizado a gemini-2.0-flash debido a la jubilación de los modelos 1.5
        Firebase.ai.generativeModel("gemini-2.0-flash")
    }
    
    // Estados
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedAmount by remember { mutableStateOf<String?>(null) }
    var detectedCategory by remember { mutableStateOf<String?>(null) }
    var merchantName by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }

    // Controlador de Cámara
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }

    // Permisos de Cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCamera = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showCamera) {
            CameraPreview(
                controller = cameraController,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Button(
                        onClick = {
                            isProcessing = true
                            takePhoto(
                                context = context,
                                controller = cameraController,
                                onPhotoTaken = { bitmap ->
                                    capturedBitmap = bitmap
                                    showCamera = false
                                    // Análisis IA
                                    processFullAnalysis(bitmap, geminiModel, scope) { amount, category, merchant ->
                                        extractedAmount = amount
                                        detectedCategory = category
                                        merchantName = merchant
                                        isProcessing = false
                                    }
                                }
                            )
                        },
                        modifier = Modifier.size(80.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Foto", color = Color.Black)
                    }
                }
            }

            TextButton(
                onClick = { showCamera = false },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text("Cerrar", style = MaterialTheme.typography.titleLarge)
            }

        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Finia Smart Scan",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                capturedBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Foto capturada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    )
                } ?: Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Toma una foto de tu ticket")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Escanear con IA")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sugerencia de Gemini
                if (merchantName != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Detección Inteligente:", style = MaterialTheme.typography.titleMedium)
                            Text("Comercio: $merchantName", style = MaterialTheme.typography.bodyLarge)
                            Text("Categoría: $detectedCategory", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Total: $extractedAmount", 
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Button(
                                onClick = { /* Guardar en Firestore */ },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Text("Guardar Transacción")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onPhotoTaken: (Bitmap) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
                val bitmap = image.toBitmap()
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                onPhotoTaken(rotatedBitmap)
                image.close()
            }
            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}

private fun processFullAnalysis(
    bitmap: Bitmap,
    geminiModel: com.google.firebase.ai.GenerativeModel,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (String?, String?, String?) -> Unit
) {
    scope.launch {
        // Escalar el bitmap para evitar errores de tamaño (max 1024px)
        val scaledBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth = if (ratio > 1) 1024 else (1024 * ratio).toInt()
            val newHeight = if (ratio > 1) (1024 / ratio).toInt() else 1024
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        try {
            val prompt = """
                Analiza este ticket de compra.
                Responde ÚNICAMENTE en JSON:
                {
                  "merchant": "nombre del lugar",
                  "total": "monto",
                  "category": "Comida|Despensa|Transporte|Salud|Otros"
                }
            """.trimIndent()

            val response = geminiModel.generateContent(
                content {
                    image(scaledBitmap)
                    text(prompt)
                }
            )

            val responseText = response.text
            if (responseText == null) {
                processLocalAnalysis(scaledBitmap, onResult)
                return@launch
            }

            val jsonString = responseText.trim().removeSurrounding("```json", "```").trim()
            val jsonObject = JSONObject(jsonString)
            
            onResult(
                jsonObject.optString("total", "0.00"),
                jsonObject.optString("category", "Otros"),
                jsonObject.optString("merchant", "Desconocido")
            )
        } catch (e: Exception) {
            Log.e("GeminiError", "Error detectado, iniciando fallback local: ${e.message}")
            // Si hay error de cuota o red, usamos ML Kit local
            processLocalAnalysis(scaledBitmap, onResult)
        }
    }
}

private fun processLocalAnalysis(
    bitmap: Bitmap,
    onResult: (String?, String?, String?) -> Unit
) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(bitmap, 0)
    
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val fullText = visionText.text
            val amount = extractTotal(fullText) ?: "No detectado"
            val category = classifyCategory(fullText, emptyList())
            val merchant = extractMerchant(fullText)
            onResult(amount, category, merchant)
        }
        .addOnFailureListener { e ->
            Log.e("LocalScanError", "Error en escaneo local: ${e.message}")
            onResult("Error Local", "Error", "Error")
        }
}

private fun extractMerchant(text: String): String {
    val lines = text.split("\n").filter { it.isNotBlank() }
    return if (lines.isNotEmpty()) lines[0].trim().take(20) else "Ticket Local"
}


private fun extractTotal(text: String): String? {
    // Buscamos patrones comunes de precios (ej: 120.00, $500, Total: 15.50)
    val lines = text.split("\n")
    var totalCandidate: String? = null
    
    // Intentamos buscar la línea que diga "TOTAL"
    lines.forEach { line ->
        if (line.contains("TOTAL", ignoreCase = true)) {
            val amountMatch = Regex("\\d+[.,]\\d{2}").find(line)
            if (amountMatch != null) return amountMatch.value
        }
    }
    
    // Si no encontramos "TOTAL", buscamos el número con decimales más abajo en el ticket
    val matches = Regex("\\d+[.,]\\d{2}").findAll(text).toList()
    if (matches.isNotEmpty()) {
        totalCandidate = matches.last().value
    }
    
    return totalCandidate
}

private fun classifyCategory(text: String, labels: List<String>): String {
    val content = (text + labels.joinToString(" ")).lowercase()
    
    return when {
        content.contains("restaurante") || content.contains("comida") || content.contains("food") || content.contains("burger") || content.contains("pizza") -> "Comer afuera 🍔"
        content.contains("super") || content.contains("market") || content.contains("abarrotes") || content.contains("walmart") -> "Despensa 🛒"
        content.contains("gas") || content.contains("combustible") || content.contains("uber") || content.contains("did") -> "Transporte 🚗"
        content.contains("farmacia") || content.contains("salud") || content.contains("medico") -> "Salud 💊"
        content.contains("cine") || content.contains("netflix") || content.contains("game") -> "Diversión 🎮"
        else -> "Otros 📦"
    }
}
