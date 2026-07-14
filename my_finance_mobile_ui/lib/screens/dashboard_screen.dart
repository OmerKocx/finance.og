import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/models.dart';
import '../providers/app_state.dart';
import '../widgets/theme.dart';
import '../widgets/wallet_operations_dialog.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  Currency _selectedCurrency = Currency.TRY;
  final _initialBalanceController = TextEditingController(text: '0');
  final _walletFormKey = GlobalKey<FormState>();

  @override
  void dispose() {
    _initialBalanceController.dispose();
    super.dispose();
  }

  // Format currency symbols helper
  String _getCurrencySymbol(Currency currency) {
    switch (currency) {
      case Currency.TRY:
        return '₺';
      case Currency.USD:
        return '\$';
      case Currency.EUR:
        return '€';
    }
  }

  Future<void> _handleCreateWallet() async {
    if (!_walletFormKey.currentState!.validate()) return;
    
    final initialBalance = double.tryParse(_initialBalanceController.text.trim()) ?? 0.0;
    final appState = AppStateProvider.of(context, listen: false);
    
    final success = await appState.createWallet(_selectedCurrency, initialBalance);
    
    if (!mounted) return;

    if (success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Cüzdanınız başarıyla oluşturuldu!'),
          backgroundColor: PremiumTheme.successGreen,
        ),
      );
    } else {
      showDialog(
        context: context,
        builder: (context) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          backgroundColor: PremiumTheme.cardBg,
          title: const Text('Hata', style: TextStyle(color: PremiumTheme.errorRed)),
          content: Text(appState.errorMessage ?? 'Cüzdan oluşturulurken bir hata oluştu.'),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
                appState.clearError();
              },
              child: const Text('Kapat', style: TextStyle(color: PremiumTheme.deepPurple)),
            ),
          ],
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final appState = AppStateProvider.of(context);
    final wallet = appState.currentWallet;

    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Welcome Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Hoş Geldiniz,',
                    style: TextStyle(
                      fontSize: 14,
                      color: PremiumTheme.textSecondary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    appState.userName ?? 'Değerli Üye',
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                ],
              ),
              IconButton(
                icon: const Icon(Icons.refresh, color: Colors.white),
                onPressed: () => appState.loadWalletDetails(),
              ),
            ],
          ),
          const SizedBox(height: 28),

          if (wallet == null) ...[
            // Prompt for wallet creation
            Form(
              key: _walletFormKey,
              child: GlassContainer(
                padding: const EdgeInsets.all(24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const Center(
                      child: Icon(
                        Icons.account_balance_wallet_outlined,
                        size: 48,
                        color: PremiumTheme.neonTeal,
                      ),
                    ),
                    const SizedBox(height: 16),
                    const Center(
                      child: Text(
                        'Henüz cüzdanınız bulunmuyor',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Center(
                      child: Text(
                        'Hemen cüzdan oluşturarak işlemlere başlayabilirsiniz.',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 13,
                          color: PremiumTheme.textSecondary,
                        ),
                      ),
                    ),
                    const SizedBox(height: 24),

                    // Currency Selector Dropdown
                    DropdownButtonFormField<Currency>(
                      value: _selectedCurrency,
                      dropdownColor: PremiumTheme.cardBg,
                      style: const TextStyle(color: Colors.white),
                      decoration: PremiumTheme.inputDecoration(
                        labelText: 'Para Birimi',
                        prefixIcon: Icons.currency_exchange,
                      ),
                      items: Currency.values.map((Currency cur) {
                        return DropdownMenuItem<Currency>(
                          value: cur,
                          child: Text('${cur.name} (${_getCurrencySymbol(cur)})'),
                        );
                      }).toList(),
                      onChanged: (Currency? value) {
                        if (value != null) {
                          setState(() {
                            _selectedCurrency = value;
                          });
                        }
                      },
                    ),
                    const SizedBox(height: 16),

                    // Initial balance input
                    TextFormField(
                      controller: _initialBalanceController,
                      style: const TextStyle(color: Colors.white),
                      keyboardType: const TextInputType.numberWithOptions(
                        decimal: true,
                        signed: false,
                      ),
                      decoration: PremiumTheme.inputDecoration(
                        labelText: 'Başlangıç Bakiyesi',
                        prefixIcon: Icons.monetization_on_outlined,
                      ),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return 'Lütfen başlangıç bakiyesini girin';
                        }
                        final amount = double.tryParse(value.trim());
                        if (amount == null || amount < 0) {
                          return 'Geçerli bir miktar girin';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 24),

                    // Create Button
                    ElevatedButton(
                      onPressed: appState.isLoading ? null : _handleCreateWallet,
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 14),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                        backgroundColor: Colors.transparent,
                        shadowColor: Colors.transparent,
                        foregroundColor: Colors.white,
                      ).copyWith(
                        backgroundColor: WidgetStateProperty.resolveWith<Color?>((states) {
                          return Colors.transparent;
                        }),
                      ),
                      child: Container(
                        width: double.infinity,
                        height: 50,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          gradient: PremiumTheme.primaryGradient,
                          borderRadius: BorderRadius.circular(16),
                          boxShadow: PremiumTheme.neonShadow(PremiumTheme.luxuryIndigo),
                        ),
                        child: appState.isLoading
                            ? const SizedBox(
                                width: 20,
                                height: 20,
                                child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2),
                              )
                            : const Text(
                                'Cüzdan Oluştur',
                                style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold),
                              ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ] else ...[
            // Premium Wallet Glass Card
            Container(
              width: double.infinity,
              height: 200,
              decoration: BoxDecoration(
                gradient: PremiumTheme.primaryGradient,
                borderRadius: BorderRadius.circular(24),
                boxShadow: PremiumTheme.neonShadow(PremiumTheme.luxuryIndigo),
              ),
              child: Stack(
                children: [
                  // Decorative shapes inside card
                  Positioned(
                    right: -20,
                    bottom: -20,
                    child: Container(
                      width: 140,
                      height: 140,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: Colors.white.withOpacity(0.06),
                      ),
                    ),
                  ),
                  Positioned(
                    right: 40,
                    top: -10,
                    child: Container(
                      width: 100,
                      height: 100,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: Colors.white.withOpacity(0.04),
                      ),
                    ),
                  ),
                  
                  Padding(
                    padding: const EdgeInsets.all(24.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              'Cüzdan Hesabım (#${wallet.id})',
                              style: TextStyle(
                                color: Colors.white.withOpacity(0.8),
                                fontSize: 14,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                              decoration: BoxDecoration(
                                color: wallet.status == WalletStatus.ACTIVE
                                    ? PremiumTheme.successGreen.withOpacity(0.2)
                                    : PremiumTheme.errorRed.withOpacity(0.2),
                                borderRadius: BorderRadius.circular(10),
                                border: Border.all(
                                  color: wallet.status == WalletStatus.ACTIVE
                                      ? PremiumTheme.successGreen.withOpacity(0.4)
                                      : PremiumTheme.errorRed.withOpacity(0.4),
                                  width: 1,
                                ),
                              ),
                              child: Text(
                                wallet.status.name,
                                style: TextStyle(
                                  color: wallet.status == WalletStatus.ACTIVE
                                      ? PremiumTheme.successGreen
                                      : PremiumTheme.errorRed,
                                  fontSize: 10,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ],
                        ),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Toplam Bakiye',
                              style: TextStyle(
                                color: Colors.white70,
                                fontSize: 13,
                              ),
                            ),
                            const SizedBox(height: 6),
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.baseline,
                              textBaseline: TextBaseline.alphabetic,
                              children: [
                                Text(
                                  _getCurrencySymbol(wallet.currency),
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 26,
                                    fontWeight: FontWeight.w300,
                                  ),
                                ),
                                const SizedBox(width: 4),
                                Text(
                                  wallet.balance.toStringAsFixed(2),
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 34,
                                    fontWeight: FontWeight.bold,
                                    letterSpacing: 0.5,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                        Text(
                          wallet.createdDate != null
                              ? 'Hesap Açılışı: ${DateFormat('dd.MM.yyyy').format(wallet.createdDate!)}'
                              : '',
                          style: TextStyle(
                            color: Colors.white.withOpacity(0.6),
                            fontSize: 11,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 28),

            // Quick Actions Title
            const Text(
              'Hızlı İşlemler',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 16),

            // Quick Actions Buttons Row
            Row(
              children: [
                Expanded(
                  child: _buildQuickActionButton(
                    icon: Icons.add_circle_outline_rounded,
                    label: 'Yatır',
                    onTap: () => WalletOperationsDialog.show(context, 'DEPOSIT', wallet),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildQuickActionButton(
                    icon: Icons.remove_circle_outline_rounded,
                    label: 'Çek',
                    onTap: () => WalletOperationsDialog.show(context, 'WITHDRAW', wallet),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildQuickActionButton(
                    icon: Icons.send_rounded,
                    label: 'Gönder',
                    onTap: () => WalletOperationsDialog.show(context, 'TRANSFER', wallet),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 32),

            // Recent Transactions Title
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Son İşlemler',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                if (appState.transactions.isNotEmpty)
                  const Text(
                    'Tümü',
                    style: TextStyle(
                      fontSize: 12,
                      color: PremiumTheme.neonTeal,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 16),

            // Recent Transactions List
            if (appState.transactions.isEmpty) ...[
              Center(
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 24.0),
                  child: Column(
                    children: [
                      Icon(Icons.history_toggle_off_rounded, color: Colors.white24, size: 36),
                      const SizedBox(height: 8),
                      const Text(
                        'Henüz işlem kaydı bulunmuyor.',
                        style: TextStyle(color: PremiumTheme.textSecondary, fontSize: 13),
                      ),
                    ],
                  ),
                ),
              )
            ] else ...[
              ListView.separated(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: appState.transactions.length > 5 ? 5 : appState.transactions.length,
                separatorBuilder: (context, index) => const SizedBox(height: 12),
                itemBuilder: (context, index) {
                  final tx = appState.transactions[index];
                  return _buildTransactionItem(tx, wallet.currency);
                },
              ),
            ],
          ]
        ],
      ),
    );
  }

  // Quick Action Button Widget
  Widget _buildQuickActionButton({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: GlassContainer(
        borderRadius: 16,
        padding: const EdgeInsets.symmetric(vertical: 16),
        child: Column(
          children: [
            Icon(icon, color: PremiumTheme.neonTeal, size: 24),
            const SizedBox(height: 8),
            Text(
              label,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
          ],
        ),
      ),
    );
  }

  // Transaction Item Tile
  Widget _buildTransactionItem(Transaction tx, Currency currency) {
    IconData icon;
    Color color;
    String title = '';
    String sign = '';

    switch (tx.type) {
      case TransactionType.DEPOSIT:
        icon = Icons.south_west_rounded;
        color = PremiumTheme.successGreen;
        title = 'Para Yatırma';
        sign = '+';
        break;
      case TransactionType.WITHDRAW:
        icon = Icons.north_east_rounded;
        color = PremiumTheme.errorRed;
        title = 'Para Çekme';
        sign = '-';
        break;
      case TransactionType.TRANSFER_IN:
        icon = Icons.south_west_rounded;
        color = PremiumTheme.successGreen;
        title = 'Gelen Transfer';
        sign = '+';
        break;
      case TransactionType.TRANSFER_OUT:
        icon = Icons.north_east_rounded;
        color = PremiumTheme.errorRed;
        title = 'Giden Transfer';
        sign = '-';
        break;
    }

    final formattedDate = tx.createdDate != null
        ? DateFormat('dd MMM yyyy, HH:mm').format(tx.createdDate!)
        : '';

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: PremiumTheme.cardBg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.04), width: 1),
      ),
      child: Row(
        children: [
          // Icon Box
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color, size: 18),
          ),
          const SizedBox(width: 14),
          // Info Column
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  formattedDate,
                  style: const TextStyle(
                    fontSize: 11,
                    color: PremiumTheme.textSecondary,
                  ),
                ),
              ],
            ),
          ),
          // Amount Column
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '$sign${_getCurrencySymbol(currency)}${tx.amount.toStringAsFixed(2)}',
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.bold,
                  color: color,
                ),
              ),
              if (tx.description.isNotEmpty) ...[
                const SizedBox(height: 2),
                Text(
                  tx.description,
                  style: const TextStyle(
                    fontSize: 10,
                    color: PremiumTheme.textSecondary,
                  ),
                ),
              ],
            ],
          )
        ],
      ),
    );
  }
}
