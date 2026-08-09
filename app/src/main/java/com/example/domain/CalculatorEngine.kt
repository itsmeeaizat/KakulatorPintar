package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Stack

sealed class CalculationResult {
    data class Success(
        val firstOperand: String,
        val operator: String,
        val secondOperand: String,
        val expression: String,
        val result: String
    ) : CalculationResult()

    data class Error(val errorMessage: String) : CalculationResult()
}

object CalculatorEngine {
    private const val MAX_OPERAND_LENGTH = 25
    private val mathContext = MathContext(16, RoundingMode.HALF_UP)

    fun calculate(
        firstOperandStr: String,
        operator: String,
        secondOperandStr: String
    ): CalculationResult {
        val op1Clean = firstOperandStr.trim()
        val op2Clean = secondOperandStr.trim()

        if (op1Clean.isEmpty() || op2Clean.isEmpty()) {
            return CalculationResult.Error("Operan tidak boleh kosong.")
        }

        val expression = "$op1Clean $operator $op2Clean"
        return evaluateExpression(expression, fallbackOp1 = op1Clean, fallbackOp = operator, fallbackOp2 = op2Clean)
    }

    /**
     * Evaluasi ekspresi matematika menggunakan Shunting-Yard Algorithm
     * Mengikuti standar urutan operasi matematika PEMDAS / BODMAS (perkalian dan pembagian didahulukan).
     */
    fun evaluateExpression(
        expressionStr: String,
        fallbackOp1: String = "",
        fallbackOp: String = "",
        fallbackOp2: String = ""
    ): CalculationResult {
        val cleanExpr = expressionStr.trim()
        if (cleanExpr.isEmpty()) {
            return CalculationResult.Error("Ekspresi tidak boleh kosong.")
        }

        return try {
            val tokens = tokenize(cleanExpr)
            if (tokens.isEmpty()) {
                return CalculationResult.Error("Ekspresi matematika tidak valid.")
            }

            val resultDecimal = evaluateTokensShuntingYard(tokens)
            val formattedResult = resultDecimal.stripTrailingZeros().toPlainString()

            val op1 = if (fallbackOp1.isNotEmpty()) fallbackOp1 else tokens.firstOrNull() ?: ""
            val op = if (fallbackOp.isNotEmpty()) fallbackOp else ""
            val op2 = if (fallbackOp2.isNotEmpty()) fallbackOp2 else tokens.lastOrNull() ?: ""

            CalculationResult.Success(
                firstOperand = op1,
                operator = op,
                secondOperand = op2,
                expression = cleanExpr,
                result = formattedResult
            )
        } catch (e: ArithmeticException) {
            CalculationResult.Error(e.message ?: "Kesalahan perhitungan matematika.")
        } catch (e: IllegalArgumentException) {
            CalculationResult.Error(e.message ?: "Format ekspresi tidak valid.")
        } catch (e: Exception) {
            CalculationResult.Error("Gagal menghitung ekspresi: ${e.message}")
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        while (i < len) {
            val ch = expr[i]
            when {
                ch.isWhitespace() -> {
                    i++
                }
                ch == '+' || ch == '-' || ch == '−' || ch == '*' || ch == '×' || ch == '/' || ch == '÷' || ch == '%' || ch == '(' || ch == ')' -> {
                    val isUnaryMinus = (ch == '-' || ch == '−') && (tokens.isEmpty() || isOperatorToken(tokens.last()) || tokens.last() == "(")
                    if (isUnaryMinus) {
                        val start = i
                        i++
                        while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                            i++
                        }
                        val numStr = expr.substring(start, i).replace('−', '-')
                        if (numStr == "-" || numStr == "−") {
                            tokens.add("-1")
                            tokens.add("*")
                        } else {
                            tokens.add(numStr)
                        }
                    } else {
                        val normalizedOp = when (ch) {
                            '×' -> "*"
                            '÷' -> "/"
                            '−' -> "-"
                            else -> ch.toString()
                        }
                        tokens.add(normalizedOp)
                        i++
                    }
                }
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    while (i < len && (expr[i].isDigit() || expr[i] == '.')) {
                        i++
                    }
                    tokens.add(expr.substring(start, i))
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private fun isOperatorToken(token: String): Boolean {
        return token == "+" || token == "-" || token == "*" || token == "/" || token == "%"
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/", "%" -> 2
            else -> 0
        }
    }

    private fun evaluateTokensShuntingYard(tokens: List<String>): BigDecimal {
        val values = Stack<BigDecimal>()
        val operators = Stack<String>()

        var index = 0
        while (index < tokens.size) {
            val token = tokens[index]

            when {
                token.toDoubleOrNull() != null -> {
                    values.push(BigDecimal(token))
                }
                token == "%" && (index == tokens.lastIndex || isOperatorToken(tokens.getOrNull(index + 1) ?: "")) -> {
                    if (values.isEmpty()) throw IllegalArgumentException("Format persentase tidak valid.")
                    val valTop = values.pop()
                    values.push(valTop.divide(BigDecimal(100), mathContext))
                }
                isOperatorToken(token) -> {
                    while (operators.isNotEmpty() && isOperatorToken(operators.peek()) && precedence(operators.peek()) >= precedence(token)) {
                        applyTopOperator(values, operators)
                    }
                    operators.push(token)
                }
                token == "(" -> {
                    operators.push(token)
                }
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        applyTopOperator(values, operators)
                    }
                    if (operators.isNotEmpty() && operators.peek() == "(") {
                        operators.pop()
                    } else {
                        throw IllegalArgumentException("Tanda kurung tidak seimbang.")
                    }
                }
                else -> {
                    throw IllegalArgumentException("Simbol '$token' tidak dikenal.")
                }
            }
            index++
        }

        while (operators.isNotEmpty()) {
            if (operators.peek() == "(" || operators.peek() == ")") {
                throw IllegalArgumentException("Tanda kurung tidak seimbang.")
            }
            applyTopOperator(values, operators)
        }

        if (values.size != 1) {
            throw IllegalArgumentException("Format ekspresi tidak valid.")
        }

        return values.pop()
    }

    private fun applyTopOperator(values: Stack<BigDecimal>, operators: Stack<String>) {
        if (values.size < 2) {
            throw IllegalArgumentException("Jumlah angka kurang untuk operasi '${operators.peek()}'")
        }
        val b = values.pop()
        val a = values.pop()
        val op = operators.pop()

        val result = when (op) {
            "+" -> a.add(b)
            "-" -> a.subtract(b)
            "*" -> a.multiply(b)
            "/" -> {
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw ArithmeticException("Kesalahan: Tidak dapat membagi dengan angka nol (Division by Zero).")
                }
                a.divide(b, mathContext)
            }
            "%" -> {
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw ArithmeticException("Kesalahan: Operasi modulus dengan angka nol tidak valid.")
                }
                a.remainder(b)
            }
            else -> throw IllegalArgumentException("Operator '$op' tidak dikenal.")
        }
        values.push(result)
    }
}

