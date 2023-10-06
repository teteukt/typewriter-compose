package br.com.typewriter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import br.com.typewriter.ui.theme.TypewriterTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TypewriterTheme {
                Content()
            }
        }
    }
}

@Composable
fun Content() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Pager(
            listOf(
                "Olá!",
                "Bem-vindo(a) ao Typewriter",
                "Queremos ouvir suas histórias!",
                "Primeiramente, gostariamos de te conhecer",
                "Podemos começar, %s?"
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Pager(texts: List<String>) {
    val pagerState = rememberPagerState()
    val coroutines = rememberCoroutineScope()
    var nameTextFieldState by remember { mutableStateOf("") }
    var nameTextFieldError by remember {
        mutableStateOf<String?>(null)
    }

    fun getCurrentPage() = pagerState.currentPage
    fun isFirstPage() = getCurrentPage() == 0
    fun isInLastPage() = getCurrentPage() == texts.size - 1
    fun getAbsoluteOffset() = abs(pagerState.currentPageOffsetFraction)
    fun getNormalizedOffset() = getAbsoluteOffset() * 2
    fun getCurrentText() = texts[pagerState.currentPage].format(nameTextFieldState)
    fun getTextLength() = getCurrentText().length
    fun getCurrentTextLengthBasedOnOffset() = (getTextLength() * getNormalizedOffset()).toInt()
    fun getTextSubstring() =
        getCurrentText().substring(0, getTextLength() - getCurrentTextLengthBasedOnOffset())

    fun isNextButtonEnabled() = !isInLastPage()

    val colorMap = mapOf(
        0 to Color(0xFFDAD7CD),
        1 to Color(0xFFfaedcd),
        2 to Color(0xFFc7f9cc),
        3 to Color(0xFFade8f4),
    )
    val animation = animateColorAsState(
        targetValue = colorMap[getCurrentPage()] ?: MaterialTheme.colorScheme.background,
        label = "color"
    )

    fun performScrollToPreviousPage() {
        coroutines.launch {
            if (!isFirstPage())
                pagerState.animateScrollToPage(page = getCurrentPage() - 1)
        }
    }

    fun performScrollToNextPage() {
        if (getCurrentPage() == 3) {
            if (nameTextFieldState.isBlank()) {
                nameTextFieldError = "Por favor, preencha seu nome!"
                return
            }
        }
        coroutines.launch {
            if (!isInLastPage())
                pagerState.animateScrollToPage(page = getCurrentPage() + 1)
        }
    }

    HorizontalPager(
        pageCount = texts.size,
        modifier = Modifier
            .fillMaxSize()
            .background(animation.value),
        state = pagerState
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (it) {
                0 -> {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 18.dp)
                            .offset(y = (120).dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(
                            text = "Por que não experimenta arrastar seu dedo na tela para esquerda?",
                            fontSize = 12.sp,
                            lineHeight = 12.sp
                        )
                    }
                }

                3 -> {
                    OutlinedTextField(
                        modifier = Modifier
                            .offset(y = (120).dp),
                        value = nameTextFieldState,
                        onValueChange = {
                            nameTextFieldState = it
                        },
                        label = { Text(text = "Qual o seu nome?") },
                        supportingText = { Text(text = nameTextFieldError.orEmpty()) },
                        singleLine = true,
                        isError = nameTextFieldError != null
                    )
                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = getTextSubstring(),
            fontFamily = FontFamily.Monospace,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            lineHeight = 28.sp
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Row {
            List(texts.size) { index ->
                val drawable =
                    if (index <= pagerState.currentPage) R.drawable.baseline_circle_24 else R.drawable.outline_circle_24
                Icon(
                    painter = painterResource(id = drawable),
                    contentDescription = null
                )
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.Bottom, modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { performScrollToPreviousPage() }, enabled = !isFirstPage()) {
                Text(text = "Voltar")
            }

            Button(
                onClick = { performScrollToNextPage() },
                enabled = isNextButtonEnabled()
            ) {
                Text(text = "Seguir")
            }
        }
    }
}