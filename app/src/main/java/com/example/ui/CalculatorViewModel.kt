package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.CalculationHistoryEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.CalculatorRepository
import com.example.domain.CalculationResult
import com.example.domain.CalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class CalculatorUiState(
    val currentUser: UserEntity? = null,
    val firstOperand: String = "",
    val operator: String = "",
    val secondOperand: String = "",
    val expressionDisplay: String = "0",
    val liveResult: String = "",
    val errorMessage: String? = null,
    val historyList: List<CalculationHistoryEntity> = emptyList(),
    val isHistoryOpen: Boolean = false,
    val isNewCalculationStarted: Boolean = true
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CalculatorRepository(database.calculationHistoryDao(), database.userDao())

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        loadUserAndHistory()
    }

    private fun loadUserAndHistory() {
        viewModelScope.launch {
            val user = repository.ensureDefaultUser()
            _uiState.update { it.copy(currentUser = user) }

            repository.getHistoryForUser(user.id).collectLatest { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }
    }

    fun onDigitClick(digit: String) {
        _uiState.update { currentState ->
            val cleanState = currentState.copy(errorMessage = null)

            if (cleanState.isNewCalculationStarted && cleanState.operator.isEmpty()) {
                val newOp1 = if (digit == ".") "0." else digit
                updateStateWithOperands(cleanState, newOp1, "", "")
            } else if (cleanState.operator.isEmpty()) {
                if (digit == "." && cleanState.firstOperand.contains(".")) return@update cleanState
                if (cleanState.firstOperand.length >= 15) {
                    return@update cleanState.copy(errorMessage = "Maksimal 15 digit angka.")
                }
                val currentOp1 = cleanState.firstOperand
                val newOp1 = when {
                    currentOp1 == "0" && digit != "." -> digit
                    currentOp1 == "" -> if (digit == ".") "0." else digit
                    else -> currentOp1 + digit
                }
                updateStateWithOperands(cleanState, newOp1, "", "")
            } else {
                if (digit == "." && cleanState.secondOperand.contains(".")) return@update cleanState
                if (cleanState.secondOperand.length >= 15) {
                    return@update cleanState.copy(errorMessage = "Maksimal 15 digit angka.")
                }
                val currentOp2 = cleanState.secondOperand
                val newOp2 = when {
                    currentOp2 == "0" && digit != "." -> digit
                    currentOp2 == "" -> if (digit == ".") "0." else digit
                    else -> currentOp2 + digit
                }
                updateStateWithOperands(cleanState, cleanState.firstOperand, cleanState.operator, newOp2)
            }
        }
    }

    fun onOperatorClick(op: String) {
        _uiState.update { currentState ->
            val cleanState = currentState.copy(errorMessage = null)

            if (cleanState.firstOperand.isNotEmpty() && cleanState.operator.isNotEmpty() && cleanState.secondOperand.isNotEmpty()) {
                val calc = CalculatorEngine.calculate(cleanState.firstOperand, cleanState.operator, cleanState.secondOperand)
                if (calc is CalculationResult.Success) {
                    cleanState.copy(
                        firstOperand = calc.result,
                        operator = op,
                        secondOperand = "",
                        expressionDisplay = "${calc.result} $op",
                        liveResult = calc.result,
                        isNewCalculationStarted = false
                    )
                } else {
                    cleanState
                }
            } else if (cleanState.firstOperand.isNotEmpty()) {
                cleanState.copy(
                    operator = op,
                    isNewCalculationStarted = false,
                    expressionDisplay = "${cleanState.firstOperand} $op"
                )
            } else {
                cleanState.copy(
                    firstOperand = "0",
                    operator = op,
                    isNewCalculationStarted = false,
                    expressionDisplay = "0 $op"
                )
            }
        }
    }

    fun onEqualsClick() {
        val currentState = _uiState.value
        val user = currentState.currentUser ?: return

        if (currentState.firstOperand.isEmpty() || currentState.operator.isEmpty() || currentState.secondOperand.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val result = repository.processAndSaveCalculation(
                userId = user.id,
                firstOperand = currentState.firstOperand,
                operator = currentState.operator,
                secondOperand = currentState.secondOperand
            )

            when (result) {
                is CalculationResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            firstOperand = result.result,
                            operator = "",
                            secondOperand = "",
                            expressionDisplay = result.expression,
                            liveResult = result.result,
                            errorMessage = null,
                            isNewCalculationStarted = true
                        )
                    }
                }
                is CalculationResult.Error -> {
                    _uiState.update { state ->
                        state.copy(errorMessage = result.errorMessage)
                    }
                }
            }
        }
    }

    fun onClearClick() {
        _uiState.update { state ->
            state.copy(
                firstOperand = "",
                operator = "",
                secondOperand = "",
                expressionDisplay = "0",
                liveResult = "",
                errorMessage = null,
                isNewCalculationStarted = true
            )
        }
    }

    fun onBackspaceClick() {
        _uiState.update { currentState ->
            val cleanState = currentState.copy(errorMessage = null)

            if (cleanState.secondOperand.isNotEmpty()) {
                val newOp2 = cleanState.secondOperand.dropLast(1)
                updateStateWithOperands(cleanState, cleanState.firstOperand, cleanState.operator, newOp2)
            } else if (cleanState.operator.isNotEmpty()) {
                cleanState.copy(
                    operator = "",
                    expressionDisplay = cleanState.firstOperand.ifEmpty { "0" }
                )
            } else if (cleanState.firstOperand.isNotEmpty()) {
                val newOp1 = cleanState.firstOperand.dropLast(1)
                updateStateWithOperands(cleanState, newOp1, "", "")
            } else {
                cleanState.copy(expressionDisplay = "0")
            }
        }
    }

    fun onPercentClick() {
        _uiState.update { currentState ->
            if (currentState.secondOperand.isNotEmpty()) {
                try {
                    val val2 = BigDecimal(currentState.secondOperand).divide(BigDecimal(100))
                    updateStateWithOperands(currentState, currentState.firstOperand, currentState.operator, val2.stripTrailingZeros().toPlainString())
                } catch (e: Exception) {
                    currentState
                }
            } else if (currentState.firstOperand.isNotEmpty()) {
                try {
                    val val1 = BigDecimal(currentState.firstOperand).divide(BigDecimal(100))
                    updateStateWithOperands(currentState, val1.stripTrailingZeros().toPlainString(), "", "")
                } catch (e: Exception) {
                    currentState
                }
            } else {
                currentState
            }
        }
    }

    fun onToggleSignClick() {
        _uiState.update { currentState ->
            if (currentState.secondOperand.isNotEmpty()) {
                val negated = if (currentState.secondOperand.startsWith("-")) {
                    currentState.secondOperand.removePrefix("-")
                } else {
                    "-${currentState.secondOperand}"
                }
                updateStateWithOperands(currentState, currentState.firstOperand, currentState.operator, negated)
            } else if (currentState.firstOperand.isNotEmpty()) {
                val negated = if (currentState.firstOperand.startsWith("-")) {
                    currentState.firstOperand.removePrefix("-")
                } else {
                    "-${currentState.firstOperand}"
                }
                updateStateWithOperands(currentState, negated, "", "")
            } else {
                currentState
            }
        }
    }

    fun onHistoryItemSelect(item: CalculationHistoryEntity) {
        _uiState.update { state ->
            state.copy(
                firstOperand = item.result,
                operator = "",
                secondOperand = "",
                expressionDisplay = item.expression,
                liveResult = item.result,
                errorMessage = null,
                isNewCalculationStarted = true
            )
        }
    }

    fun onDeleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun onClearAllHistory() {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            repository.clearHistory(user.id)
        }
    }

    private fun updateStateWithOperands(
        currentState: CalculatorUiState,
        op1: String,
        op: String,
        op2: String
    ): CalculatorUiState {
        val displayExpr = buildString {
            if (op1.isEmpty()) append("0") else append(op1)
            if (op.isNotEmpty()) append(" ").append(op)
            if (op2.isNotEmpty()) append(" ").append(op2)
        }

        var liveRes = ""
        if (op1.isNotEmpty() && op.isNotEmpty() && op2.isNotEmpty()) {
            val calc = CalculatorEngine.calculate(op1, op, op2)
            if (calc is CalculationResult.Success) {
                liveRes = calc.result
            }
        }

        return currentState.copy(
            firstOperand = op1,
            operator = op,
            secondOperand = op2,
            expressionDisplay = displayExpr,
            liveResult = liveRes,
            isNewCalculationStarted = false,
            errorMessage = null
        )
    }
}
