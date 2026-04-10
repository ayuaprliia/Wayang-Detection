package com.example.wayang_detection.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wayang_detection.data.model.WayangCategory
import com.example.wayang_detection.data.repository.WayangRepository
import com.example.wayang_detection.ui.components.CharacterCard
import com.example.wayang_detection.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun EncyclopediaScreen(
    onCharacterClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<WayangCategory?>(null) }

    // Debounce
    var debouncedQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery
    }

    //  FILTER DI BACKGROUND THREAD
    val characters by produceState(
        initialValue = emptyList(),
        debouncedQuery,
        selectedCategory
    ) {
        value = withContext(Dispatchers.Default) {
            val searched = WayangRepository.search(debouncedQuery)
            if (selectedCategory != null) {
                searched.filter { it.category == selectedCategory }
            } else {
                searched
            }
        }
    }

    val gridState = rememberLazyGridState()

    val filterOptions = remember {
        listOf(
            null to "Semua",
            WayangCategory.DEWA to "Dewa",
            WayangCategory.PROTAGONIS to "Protagonis",
            WayangCategory.ANTAGONIS to "Antagonis",
            WayangCategory.PUNAKAWAN to "Punakawan"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ensiklopedia Wayang",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔍 SEARCH
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text("Cari tokoh wayang...", color = TextMuted)
            },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = GoldPrimary)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 FILTER CHIPS (optimized)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { (category, label) ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${characters.size} karakter ditemukan",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔥 GRID SUPER OPTIMIZED
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 100.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = characters,
                key = { it.id },
                contentType = { "character" }
            ) { character ->

                CharacterCard(
                    character = character,
                    onClick = { onCharacterClick(character.id) }
                )
            }
        }
    }
}