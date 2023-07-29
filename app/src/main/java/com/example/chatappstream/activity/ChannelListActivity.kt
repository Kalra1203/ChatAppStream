package com.example.chatappstream.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.chatappstream.viewModel.ChannelListViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@AndroidEntryPoint
class ChannelListActivity : ComponentActivity() {
    val viewModel: ChannelListViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        subscribeToEvent()
        setContent {
            ChatTheme {

                var showDialog: Boolean by remember {
                    mutableStateOf(false)
                }
                if (showDialog) {
                    CreateChannelDialog(
                        dismiss = { channelName ->
                            viewModel.createChannel(channelName)
                            showDialog = false

                        }
                    )
                }
                ChannelsScreen(
                    filters = Filters.`in`(
                        fieldName = "type",
                        values = listOf("messaging")

                    ),
                    title = "Kalra Channel List",
                    isShowingSearch = true,
                    onItemClick = { channel ->
                        startActivity(
                            MessageActivity.getIntent(this, channelId = channel.cid)
                        )

                    },
                    onBackPressed = {
                        finish()
                    },
                    onHeaderActionClick = {
                        showDialog = true


                    },
                    onHeaderAvatarClick = {
                        viewModel.logOut()
                        finish()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CreateChannelDialog(dismiss: (String) -> Unit) {
        var channelName by remember {
            mutableStateOf("")
        }

        AlertDialog(
            title = {
                Text(text = "Enter Channel Name")
            },
            text = {
                TextField(value = channelName, onValueChange = { channelName = it })
            },

            onDismissRequest = { dismiss(channelName) },
            confirmButton = {
                Button(
                    onClick = { dismiss(channelName) }
                ) {
                    Text(text = "Create Chanel")

                }
            }
        )
    }

    private fun subscribeToEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.createChannelEvent.collect { event ->
                when (event) {
                    is ChannelListViewModel.CreateChannelEvent.Error -> {
                        val error = event.error
                        showToast(error)
                    }

                    is ChannelListViewModel.CreateChannelEvent.Success -> {
                        showToast("Channel Created Successfully")

                    }
                }

            }
        }

    }

    private fun showToast(msg: String) {
        Toast.makeText(this@ChannelListActivity, msg, Toast.LENGTH_SHORT).show()

    }

}