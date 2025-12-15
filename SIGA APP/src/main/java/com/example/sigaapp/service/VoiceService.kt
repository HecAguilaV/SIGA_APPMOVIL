package com.example.sigaapp.service

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.*

object VoiceService {
    
    @Composable
    fun rememberTextToSpeech(): TextToSpeech? {
        val context = LocalContext.current
        var tts by remember { mutableStateOf<TextToSpeech?>(null) }
        
        LaunchedEffect(Unit) {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS && tts != null) {
                    // Intentar español de Chile primero
                    val chileLocale = Locale("es", "CL")
                    val result = tts?.setLanguage(chileLocale)
                    
                    // Si no está disponible, intentar español mexicano (latinoamericano)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        val mexicanLocale = Locale("es", "MX")
                        tts?.setLanguage(mexicanLocale)
                    }
                }
            }
        }
        
        DisposableEffect(tts) {
            onDispose {
                tts?.stop()
                tts?.shutdown()
            }
        }
        
        return tts
    }
    
    fun speak(tts: TextToSpeech?, text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    fun stopSpeaking(tts: TextToSpeech?) {
        tts?.stop()
    }
    
    fun isSpeaking(tts: TextToSpeech?): Boolean {
        return tts?.isSpeaking == true
    }
}

