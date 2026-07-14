import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/models.dart';
import 'api_config.dart';

class WalletService {
  final String _walletUrl = '${ApiConfig.baseUrl}/wallets/api/v1';

  // Helper to generate Authorization headers
  Map<String, String> _getHeaders(String token) {
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  // Get Wallet by User ID
  Future<Wallet?> getWalletByUserId(int userId, String token) async {
    final response = await http.get(
      Uri.parse('$_walletUrl/user/$userId'),
      headers: _getHeaders(token),
    );

    if (response.statusCode == 200) {
      if (response.body.isEmpty) return null;
      final decodedJson = jsonDecode(utf8.decode(response.bodyBytes));
      return Wallet.fromJson(decodedJson);
    } else if (response.statusCode == 404) {
      return null;
    } else {
      throw Exception('Cüzdan bilgileri alınamadı.');
    }
  }

  // Create standard Wallet
  Future<Wallet> createWallet({
    required int userId,
    required double initialBalance,
    required Currency currency,
    required String token,
  }) async {
    final response = await http.post(
      Uri.parse('$_walletUrl/create'),
      headers: _getHeaders(token),
      body: jsonEncode({
        'userId': userId,
        'balance': initialBalance,
        'currency': currency.name,
        'status': 'ACTIVE',
      }),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      final decodedJson = jsonDecode(utf8.decode(response.bodyBytes));
      return Wallet.fromJson(decodedJson);
    } else {
      String errorMessage = 'Cüzdan oluşturulamadı.';
      try {
        final errorBody = jsonDecode(utf8.decode(response.bodyBytes));
        errorMessage = errorBody['message'] ?? errorMessage;
      } catch (_) {}
      throw Exception(errorMessage);
    }
  }

  // Deposit money into wallet
  Future<Wallet> deposit({
    required int walletId,
    required double amount,
    required String token,
  }) async {
    final response = await http.post(
      Uri.parse('$_walletUrl/$walletId/deposit?amount=$amount'),
      headers: _getHeaders(token),
    );

    if (response.statusCode == 200) {
      final decodedJson = jsonDecode(utf8.decode(response.bodyBytes));
      return Wallet.fromJson(decodedJson);
    } else {
      String errorMessage = 'Para yatırılamadı.';
      try {
        final errorBody = jsonDecode(utf8.decode(response.bodyBytes));
        errorMessage = errorBody['message'] ?? errorMessage;
      } catch (_) {}
      throw Exception(errorMessage);
    }
  }

  // Withdraw money from wallet
  Future<Wallet> withdraw({
    required int walletId,
    required double amount,
    required String token,
  }) async {
    final response = await http.post(
      Uri.parse('$_walletUrl/$walletId/withdraw?amount=$amount'),
      headers: _getHeaders(token),
    );

    if (response.statusCode == 200) {
      final decodedJson = jsonDecode(utf8.decode(response.bodyBytes));
      return Wallet.fromJson(decodedJson);
    } else {
      String errorMessage = 'Para çekilemedi.';
      try {
        final errorBody = jsonDecode(utf8.decode(response.bodyBytes));
        errorMessage = errorBody['message'] ?? errorMessage;
      } catch (_) {}
      throw Exception(errorMessage);
    }
  }

  // Transfer money to another wallet ID
  Future<void> transfer({
    required int sourceWalletId,
    required int destinationWalletId,
    required double amount,
    required String token,
  }) async {
    final response = await http.post(
      Uri.parse(
          '$_walletUrl/transfer?sourceWalletId=$sourceWalletId&destinationWalletId=$destinationWalletId&amount=$amount'),
      headers: _getHeaders(token),
    );

    if (response.statusCode != 200 && response.statusCode != 204) {
      String errorMessage = 'Transfer gerçekleştirilemedi.';
      try {
        final errorBody = jsonDecode(utf8.decode(response.bodyBytes));
        errorMessage = errorBody['message'] ?? errorMessage;
      } catch (_) {}
      throw Exception(errorMessage);
    }
  }

  // Get transaction history (returns List of Transactions from Spring Page structure)
  Future<List<Transaction>> getTransactionHistory({
    required int walletId,
    required String token,
    int page = 0,
    int size = 20,
  }) async {
    final response = await http.get(
      Uri.parse('$_walletUrl/$walletId/history?page=$page&size=$size'),
      headers: _getHeaders(token),
    );

    if (response.statusCode == 200) {
      final decodedJson = jsonDecode(utf8.decode(response.bodyBytes));
      final List<dynamic> content = decodedJson['content'] as List<dynamic>? ?? [];
      return content.map((tx) => Transaction.fromJson(tx)).toList();
    } else {
      throw Exception('İşlem geçmişi yüklenemedi.');
    }
  }
}
