package com.tripflow.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tripflow.core.model.UiState
import com.tripflow.core.ui.theme.Dimens
import com.tripflow.core.ui.theme.TripFlowColors

@Composable
fun <T> StateHost(
    state: UiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    emptyTitle: String = "Non c'è ancora niente",
    emptyActionLabel: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { LoadingList() },
    empty: @Composable (String?) -> Unit = { msg ->
        EmptyState(
            title = emptyTitle,
            message = msg,
            actionLabel = emptyActionLabel,
            onAction = onEmptyAction,
        )
    },
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            is UiState.Loading -> loading()
            is UiState.Empty -> empty(state.message)
            is UiState.Error -> ErrorState(
                message = state.message,
                onRetry = onRetry.takeIf { state.retryable },
            )
            is UiState.Success -> content(state.data)
        }
    }
}

@Composable
fun LoadingList(
    modifier: Modifier = Modifier,
    itemCount: Int = 3,
    itemHeight: Dp = 236.dp,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.gapL),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenPadding, vertical = Dimens.gapL),
    ) {
        repeat(itemCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(TripFlowColors.Surface2, RoundedCornerShape(Dimens.radiusCard)),
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(TripFlowColors.Surface2, RoundedCornerShape(24.dp)),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TripFlowColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.gapXL),
        )
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TripFlowColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Dimens.gapS),
            )
        }
        if (actionLabel != null && onAction != null) {
            Box(modifier = Modifier.padding(top = Dimens.gapXL)) {
                PrimaryButton(text = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
    ) {
        Text(
            text = "Qualcosa è andato storto",
            style = MaterialTheme.typography.titleLarge,
            color = TripFlowColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TripFlowColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.gapS),
        )
        if (onRetry != null) {
            Box(modifier = Modifier.padding(top = Dimens.gapXL)) {
                SecondaryButton(text = "Riprova", onClick = onRetry)
            }
        }
    }
}
