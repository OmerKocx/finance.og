class User {
  final int id;
  final String email;
  final String role;

  User({
    required this.id,
    required this.email,
    required this.role,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id'] as int,
      email: json['email'] as String,
      role: json['role'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'email': email,
      'role': role,
    };
  }
}

class AuthResponse {
  final String token;
  final String email;
  final String name;
  final int userId;

  AuthResponse({
    required this.token,
    required this.email,
    required this.name,
    required this.userId,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      token: json['token'] as String? ?? '',
      email: json['email'] as String? ?? '',
      name: json['name'] as String? ?? '',
      userId: json['userId'] as int? ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'token': token,
      'email': email,
      'name': name,
      'userId': userId,
    };
  }
}

enum Currency { TRY, USD, EUR }

enum WalletStatus { ACTIVE, INACTIVE, BLOCKED }

class Wallet {
  final int id;
  final int userId;
  final double balance;
  final Currency currency;
  final WalletStatus status;
  final DateTime? createdDate;
  final DateTime? updatedDate;

  Wallet({
    required this.id,
    required this.userId,
    required this.balance,
    required this.currency,
    required this.status,
    this.createdDate,
    this.updatedDate,
  });

  factory Wallet.fromJson(Map<String, dynamic> json) {
    // Helper to map string to currency enum
    Currency cur = Currency.TRY;
    final currencyStr = json['currency'] as String?;
    if (currencyStr != null) {
      if (currencyStr == 'USD') cur = Currency.USD;
      if (currencyStr == 'EUR') cur = Currency.EUR;
    }

    // Helper to map string to status enum
    WalletStatus stat = WalletStatus.ACTIVE;
    final statusStr = json['status'] as String?;
    if (statusStr != null) {
      if (statusStr == 'INACTIVE') stat = WalletStatus.INACTIVE;
      if (statusStr == 'BLOCKED') stat = WalletStatus.BLOCKED;
    }

    return Wallet(
      id: json['id'] as int? ?? 0,
      userId: json['userId'] as int? ?? 0,
      balance: (json['balance'] as num? ?? 0.0).toDouble(),
      currency: cur,
      status: stat,
      createdDate: json['createdDate'] != null
          ? DateTime.tryParse(json['createdDate'] as String)
          : null,
      updatedDate: json['updatedDate'] != null
          ? DateTime.tryParse(json['updatedDate'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'userId': userId,
      'balance': balance,
      'currency': currency.name,
      'status': status.name,
      'createdDate': createdDate?.toIso8601String(),
      'updatedDate': updatedDate?.toIso8601String(),
    };
  }
}

enum TransactionType { DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT }

class Transaction {
  final int id;
  final int walletId;
  final double amount;
  final TransactionType type;
  final String description;
  final DateTime? createdDate;

  Transaction({
    required this.id,
    required this.walletId,
    required this.amount,
    required this.type,
    required this.description,
    this.createdDate,
  });

  factory Transaction.fromJson(Map<String, dynamic> json) {
    TransactionType tType = TransactionType.DEPOSIT;
    final typeStr = json['type'] as String?;
    if (typeStr != null) {
      if (typeStr == 'WITHDRAW') tType = TransactionType.WITHDRAW;
      if (typeStr == 'TRANSFER_IN') tType = TransactionType.TRANSFER_IN;
      if (typeStr == 'TRANSFER_OUT') tType = TransactionType.TRANSFER_OUT;
    }

    return Transaction(
      id: json['id'] as int? ?? 0,
      walletId: json['walletId'] as int? ?? 0,
      amount: (json['amount'] as num? ?? 0.0).toDouble(),
      type: tType,
      description: json['description'] as String? ?? '',
      createdDate: json['createdDate'] != null
          ? DateTime.tryParse(json['createdDate'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'walletId': walletId,
      'amount': amount,
      'type': type.name,
      'description': description,
      'createdDate': createdDate?.toIso8601String(),
    };
  }
}
