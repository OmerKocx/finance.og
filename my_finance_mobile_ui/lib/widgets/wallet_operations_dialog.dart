import 'package:flutter/material.dart';
import '../models/models.dart';
import '../providers/app_state.dart';
import 'theme.dart';

class WalletOperationsDialog extends StatefulWidget {
  final String operationType; // 'DEPOSIT', 'WITHDRAW', 'TRANSFER'
  final Wallet currentWallet;

  const WalletOperationsDialog({
    super.key,
    required this.operationType,
    required this.currentWallet,
  });

  static void show(BuildContext context, String type, Wallet wallet) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => WalletOperationsDialog(
        operationType: type,
        currentWallet: wallet,
      ),
    );
  }

  @override
  State<WalletOperationsDialog> createState() => _WalletOperationsDialogState();
}

class _WalletOperationsDialogState extends State<WalletOperationsDialog> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _destinationController = TextEditingController();
  bool _loading = false;

  @override
  void dispose() {
    _amountController.dispose();
    _destinationController.dispose();
    super.dispose();
  }

  Future<void> _handleSubmit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _loading = true);
    final appState = AppStateProvider.of(context, listen: false);
    final amount = double.parse(_amountController.text.trim());
    bool success = false;
    String successMsg = '';

    try {
      if (widget.operationType == 'DEPOSIT') {
        success = await appState.deposit(amount);
        successMsg = '${amount.toStringAsFixed(2)} ${widget.currentWallet.currency.name} başarıyla yatırıldı.';
      } else if (widget.operationType == 'WITHDRAW') {
        success = await appState.withdraw(amount);
        successMsg = '${amount.toStringAsFixed(2)} ${widget.currentWallet.currency.name} başarıyla çekildi.';
      } else if (widget.operationType == 'TRANSFER') {
        final destId = int.parse(_destinationController.text.trim());
        success = await appState.transfer(destId, amount);
        successMsg = '${amount.toStringAsFixed(2)} ${widget.currentWallet.currency.name}, #$destId numaralı cüzdana gönderildi.';
      }

      if (!mounted) return;

      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(successMsg),
            backgroundColor: PremiumTheme.successGreen,
          ),
        );
        Navigator.of(context).pop(); // Close sheet
      } else {
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            backgroundColor: PremiumTheme.cardBg,
            title: const Text('Hata', style: TextStyle(color: PremiumTheme.errorRed)),
            content: Text(appState.errorMessage ?? 'İşlem gerçekleştirilemedi.'),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Kapat', style: TextStyle(color: PremiumTheme.deepPurple)),
              ),
            ],
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final currencyName = widget.currentWallet.currency.name;
    final balance = widget.currentWallet.balance;
    String title = 'Para Yatır';
    if (widget.operationType == 'WITHDRAW') title = 'Para Çek';
    if (widget.operationType == 'TRANSFER') title = 'Para Gönder';

    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom,
      ),
      child: Container(
        decoration: const BoxDecoration(
          color: PremiumTheme.cardBg,
          borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
          border: Border(
            top: BorderSide(color: Colors.white12, width: 1),
          ),
        ),
        padding: const EdgeInsets.all(24.0),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Bottom sheet drag handle
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Colors.white24,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close, color: PremiumTheme.textSecondary),
                    onPressed: () => Navigator.of(context).pop(),
                  )
                ],
              ),
              const SizedBox(height: 8),
              if (widget.operationType != 'DEPOSIT')
                Text(
                  'Kullanılabilir Bakiye: ${balance.toStringAsFixed(2)} $currencyName',
                  style: const TextStyle(
                    fontSize: 14,
                    color: PremiumTheme.textSecondary,
                  ),
                ),
              const SizedBox(height: 24),

              // Destination Wallet ID Input for Transfer
              if (widget.operationType == 'TRANSFER') ...[
                TextFormField(
                  controller: _destinationController,
                  style: const TextStyle(color: Colors.white),
                  keyboardType: TextInputType.number,
                  decoration: PremiumTheme.inputDecoration(
                    labelText: 'Alıcı Cüzdan ID',
                    prefixIcon: Icons.account_balance_wallet_outlined,
                  ),
                  validator: (value) {
                    if (value == null || value.trim().isEmpty) {
                      return 'Lütfen alıcı cüzdan ID girin';
                    }
                    final id = int.tryParse(value.trim());
                    if (id == null) {
                      return 'Geçerli bir cüzdan ID girin';
                    }
                    if (id == widget.currentWallet.id) {
                      return 'Kendi cüzdanınıza transfer yapamazsınız';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 16),
              ],

              // Amount Input
              TextFormField(
                controller: _amountController,
                style: const TextStyle(color: Colors.white),
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: PremiumTheme.inputDecoration(
                  labelText: 'Miktar ($currencyName)',
                  prefixIcon: Icons.attach_money,
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Lütfen miktar girin';
                  }
                  final amount = double.tryParse(value.trim());
                  if (amount == null || amount <= 0) {
                    return 'Sıfırdan büyük geçerli bir miktar girin';
                  }
                  if (widget.operationType != 'DEPOSIT' && amount > balance) {
                    return 'Yetersiz bakiye';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 32),

              // Action Button
              ElevatedButton(
                onPressed: _loading ? null : _handleSubmit,
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  backgroundColor: Colors.transparent,
                  foregroundColor: Colors.white,
                  shadowColor: Colors.transparent,
                ).copyWith(
                  backgroundColor: WidgetStateProperty.resolveWith<Color?>((states) {
                    return Colors.transparent;
                  }),
                ),
                child: Container(
                  width: double.infinity,
                  height: 56,
                  alignment: Alignment.center,
                  decoration: BoxDecoration(
                    gradient: PremiumTheme.primaryGradient,
                    borderRadius: BorderRadius.circular(16),
                    boxShadow: PremiumTheme.neonShadow(PremiumTheme.luxuryIndigo),
                  ),
                  child: _loading
                      ? const SizedBox(
                          width: 24,
                          height: 24,
                          child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                        )
                      : Text(
                          title,
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                ),
              ),
              const SizedBox(height: 12),
            ],
          ),
        ),
      ),
    );
  }
}
