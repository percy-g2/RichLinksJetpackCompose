package com.devtutorial.richlinks.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.devtutorial.richlinks.model.LinkMetadata
import com.devtutorial.richlinks.model.LinkViewState
import com.devtutorial.richlinks.model.fetchMetadata
import com.devtutorial.richlinks.openLink
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.CompottieException
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.ktor.http.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import richlinks.composeapp.generated.resources.Res
import richlinks.composeapp.generated.resources.broken_image
import richlinks.composeapp.generated.resources.link_off

@Composable
fun LinkItemView(
    link: String,
    modifier: Modifier = Modifier,
) {
    var loadingState by remember { mutableStateOf<LinkViewState>(LinkViewState.Loading) }

    LaunchedEffect(link) {
        val parsedUrl = parseUrl(link) ?: kotlin.run {
            loadingState = LinkViewState.Failure(Exception("Failed to fetch URL"))
        }
        val urlBuilder = parseUrl(link)?.let { URLBuilder(it) }
        if (urlBuilder == null) {
            loadingState = LinkViewState.Failure(Exception("Failed to fetch URL"))
        }
        val builder = URLBuilder(parsedUrl as Url).build()
        loadingState = fetchMetadata(builder)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .fillMaxWidth()
            .height(90.dp)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f))
    ) {
        when (val state = loadingState) {
            is LinkViewState.Loading -> LoadingView()
            is LinkViewState.Success -> SuccessView(state.metadata, link)
            is LinkViewState.Failure -> FailureView(link)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun LoadingView() {
    val composition = rememberLottieComposition {
        LottieCompositionSpec.JsonString(Res.readBytes("files/loading.json").decodeToString())
    }

    LaunchedEffect(composition) {
        try {
            composition.await()
        } catch (t: CompottieException) {
            t.printStackTrace()
        }
    }


    val animationState by animateLottieCompositionAsState(
        composition = composition.value,
        iterations = Compottie.IterateForever
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition.value,
                progress = { animationState },
            ),
            contentDescription = "Loading Lottie animation",
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
private fun SuccessView(metadata: LinkMetadata, link: String) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clickable { openLink(link) }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val url = remember { mutableStateOf(metadata.imageUrl ?: "") }
        LaunchedEffect(metadata.imageUrl) {
            url.value = metadata.imageUrl ?: ""
        }

        MultiplatformAsyncImage(
            imageUrl = url.value,
            modifier = Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = metadata.title ?: "Untitled",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2
            )
            Text(
                text = metadata.host,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun FailureView(link: String) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.link_off),
            contentDescription = null,
            modifier = Modifier.size(25.dp),
            tint = Color.Red
        )
        Column(
            modifier = Modifier.wrapContentSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Provided link is invalid",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )
            Text(
                text = link,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MultiplatformAsyncImage(imageUrl: String, modifier: Modifier) {
    val composition = rememberLottieComposition {
        LottieCompositionSpec.JsonString(Res.readBytes("files/loading.json").decodeToString())
    }

    LaunchedEffect(composition) {
        try {
            composition.await()
        } catch (t: CompottieException) {
            t.printStackTrace()
        }
    }


    val animationState by animateLottieCompositionAsState(
        composition = composition.value,
        iterations = Compottie.IterateForever
    )

    runCatching {
        val state = remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
        SubcomposeAsyncImage(
            modifier = modifier,
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onState = {
                state.value = it
            }
        ) {
            when (state.value) {
                is AsyncImagePainter.State.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberLottiePainter(
                                composition = composition.value,
                                progress = { animationState },
                            ),
                            contentDescription = "Loading Lottie animation",
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }

                is AsyncImagePainter.State.Error -> {
                    Icon(
                        painter = painterResource(Res.drawable.broken_image),
                        contentDescription = null,
                        modifier = Modifier.size(35.dp)
                    )
                }

                else -> {
                    SubcomposeAsyncImageContent(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(15.dp))
                    )
                }
            }
        }
    }.getOrElse {
        Icon(
            painter = painterResource(Res.drawable.link_off),
            contentDescription = null,
            modifier = Modifier.size(35.dp)
        )
    }
}