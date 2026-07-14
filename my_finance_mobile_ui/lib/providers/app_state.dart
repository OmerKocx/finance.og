import 'package:flutter/material.dart';
import '../models/models.dart';
import '../services/auth_service.dart';
import '../services/wallet_service.dart';

class AppState extends ChangeNotifier {
  final AuthService _authService = AuthService();
  final WalletService _walletService = WalletService();

  bool _isLoading = false;
  String? _token;
  int? _userId;
  String? _userEmail;
  String? _userName;
  Wallet? _currentWallet;
  List<Transaction> _transactions = [];
  String? _errorMessage;

  // Getters
  bool get isLoading => _isLoading;
  String? get token => _token;
  int? get userId => _userId;
  String? get userEmail => _userEmail;
  String? get userName => _userName;
  Wallet? get currentWallet => _currentWallet;
  List<Transaction> get transactions => _transactions;
  String? get errorMessage => _errorMessage;
  bool get isLoggedIn => _token != null && _token!.isNotEmpty;

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  // Check initial session
  Future<void> checkLoginStatus() async {
    _setLoading(true);
    try {
      final isLoggedIn = await _authService.isLoggedIn();
      if (isLoggedIn) {
        _token = await _authService.getToken();
        _userId = await _authService.getUserId();
        _userEmail = await _authService.getEmail();
        _userName = await _authService.getName();

        if (_userId != null && _token != null) {
          await loadWalletDetails();
        }
      }
    } catch (e) {
      _errorMessage = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  // Authenticate user
  Future<bool> login(String email, String password) async {
    _setLoading(true);
    clearError();
    try {
      final authResponse = await _authService.login(email, password);
      _token = authResponse.token;
      _userId = authResponse.userId;
      _userEmail = authResponse.email;
      _userName = authResponse.name;

      await loadWalletDetails();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Register user
  Future<bool> register({
    required String email,
    required String password,
    required String fullName,
    required String phoneNumber,
  }) async {
    _setLoading(true);
    clearError();
    try {
      final authResponse = await _authService.register(
        email: email,
        password: password,
        fullName: fullName,
        phoneNumber: phoneNumber,
      );
      _token = authResponse.token;
      _userId = authResponse.userId;
      _userEmail = authResponse.email;
      _userName = authResponse.name;

      _currentWallet = null; // New user has no wallet initially
      _transactions = [];
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // End user session
  Future<void> logout() async {
    _setLoading(true);
    try {
      await _authService.clearSession();
      _token = null;
      _userId = null;
      _userEmail = null;
      _userName = null;
      _currentWallet = null;
      _transactions = [];
      clearError();
    } catch (e) {
      _errorMessage = e.toString();
    } finally {
      _setLoading(false);
    }
  }

  // Get current user wallet
  Future<void> loadWalletDetails() async {
    if (_userId == null || _token == null) return;
    try {
      final wallet = await _walletService.getWalletByUserId(_userId!, _token!);
      _currentWallet = wallet;
      if (wallet != null) {
        await fetchHistory();
      }
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
    }
    notifyListeners();
  }

  // Create active wallet
  Future<bool> createWallet(Currency currency, double initialBalance) async {
    if (_userId == null || _token == null) return false;
    _setLoading(true);
    clearError();
    try {
      final wallet = await _walletService.createWallet(
        userId: _userId!,
        initialBalance: initialBalance,
        currency: currency,
        token: _token!,
      );
      _currentWallet = wallet;
      await fetchHistory();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Deposit operation
  Future<bool> deposit(double amount) async {
    if (_currentWallet == null || _token == null) return false;
    _setLoading(true);
    clearError();
    try {
      final updatedWallet = await _walletService.deposit(
        walletId: _currentWallet!.id,
        amount: amount,
        token: _token!,
      );
      _currentWallet = updatedWallet;
      await fetchHistory();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Withdraw operation
  Future<bool> withdraw(double amount) async {
    if (_currentWallet == null || _token == null) return false;
    _setLoading(true);
    clearError();
    try {
      final updatedWallet = await _walletService.withdraw(
        walletId: _currentWallet!.id,
        amount: amount,
        token: _token!,
      );
      _currentWallet = updatedWallet;
      await fetchHistory();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Transfer operation
  Future<bool> transfer(int destinationWalletId, double amount) async {
    if (_currentWallet == null || _token == null) return false;
    _setLoading(true);
    clearError();
    try {
      await _walletService.transfer(
        sourceWalletId: _currentWallet!.id,
        destinationWalletId: destinationWalletId,
        amount: amount,
        token: _token!,
      );
      // Reload wallet details and transactions on successful transfer
      await loadWalletDetails();
      return true;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
      return false;
    } finally {
      _setLoading(false);
    }
  }

  // Get transactions
  Future<void> fetchHistory() async {
    if (_currentWallet == null || _token == null) return;
    try {
      final list = await _walletService.getTransactionHistory(
        walletId: _currentWallet!.id,
        token: _token!,
      );
      _transactions = list;
    } catch (e) {
      _errorMessage = e.toString().replaceAll('Exception: ', '');
    }
    notifyListeners();
  }

  static of(BuildContext context, {required bool listen}) {}
}

// InheritedNotifier to expose AppState through the widget tree
class AppStateProvider extends InheritedNotifier<AppState> {
  const AppStateProvider({
    super.key,
    required super.notifier,
    required super.child,
  });

  static AppState of(BuildContext context, {bool listen = true}) {
    if (listen) {
      final provider = context
          .dependOnInheritedWidgetOfExactType<AppStateProvider>();
      assert(provider != null, 'No AppStateProvider found in context');
      return provider!.notifier!;
    } else {
      final element = context
          .getElementForInheritedWidgetOfExactType<AppStateProvider>();
      assert(element != null, 'No AppStateProvider found in context');
      return (element!.widget as AppStateProvider).notifier!;
    }
  }
}
