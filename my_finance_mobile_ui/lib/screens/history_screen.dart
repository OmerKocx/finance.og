import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/models.dart';
import '../providers/app_state.dart';
import '../widgets/theme.dart';

class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  String _activeFilter = 'ALL'; // 'ALL', 'DEPOSIT', 'WITHDRAW', 'TRANSFER'

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

  Future<void> _handleRefresh() async {
    await AppStateProvider.of(context, listen: false).fetchHistory();
  }

  List<Transaction> _filterTransactions(List<Transaction> transactions) {
    if (_activeFilter == 'ALL') return transactions;
    
    return transactions.where((tx) {
      if (_activeFilter == 'DEPOSIT') {
        return tx.type == TransactionType.DEPOSIT;
      }
      if (_activeFilter == 'WITHDRAW') {
        return tx.type == TransactionType.WITHDRAW;
      }
      if (_activeFilter == 'TRANSFER') {
        return tx.type == TransactionType.TRANSFER_IN || tx.type == TransactionType.TRANSFER_OUT;
      }
      return true;
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final appState = AppStateProvider.of(context);
    final wallet = appState.currentWallet;
    
    if (wallet == null) {
      return const Center(
        child: Text(
          'İşlem geçmişini görüntülemek için önce cüzdan oluşturun.',
          style: TextStyle(color: PremiumTheme.textSecondary),
        ),
      );
    }

    final filteredList = _filterTransactions(appState.transactions);
    final currency = wallet.currency;

    return RefreshIndicator(
      onRefresh: _handleRefresh,
      color: PremiumTheme.deepPurple,
      backgroundColor: PremiumTheme.cardBg,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header / Filter Chips Row
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _buildFilterChip('Tümü', 'ALL'),
                  const SizedBox(width: 8),
                  _buildFilterChip('Para Girişi', 'DEPOSIT'),
                  const SizedBox(width: 8),
                  _buildFilterChip('Para Çıkışı', 'WITHDRAW'),
                  const SizedBox(width: 8),
                  _buildFilterChip('Transferler', 'TRANSFER'),
                ],
              ),
            ),
          ),

          // Transactions List
          Expanded(
            child: filteredList.isEmpty
                ? ListView(
                    children: const [
                      SizedBox(height: 100),
                      Center(
                        child: Column(
                          children: [
                            Icon(Icons.history_toggle_off_rounded, color: Colors.white24, size: 48),
                            SizedBox(height: 12),
                            Text(
                              'Aranan kriterde işlem bulunamadı.',
                              style: TextStyle(color: PremiumTheme.textSecondary, fontSize: 14),
                            ),
                          ],
                        ),
                      ),
                    ],
                  )
                : ListView.separated(
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                    itemCount: filteredList.length,
                    separatorBuilder: (context, index) => const SizedBox(height: 12),
                    itemBuilder: (context, index) {
                      final tx = filteredList[index];
                      return _buildHistoryItem(tx, currency);
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(String label, String filterValue) {
    final isSelected = _activeFilter == filterValue;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (selected) {
        if (selected) {
          setState(() {
            _activeFilter = filterValue;
          });
        }
      },
      selectedColor: PremiumTheme.deepPurple,
      backgroundColor: PremiumTheme.cardBg,
      labelStyle: TextStyle(
        color: isSelected ? Colors.white : PremiumTheme.textSecondary,
        fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
        fontSize: 12,
      ),
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 6),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: BorderSide(
          color: isSelected ? Colors.transparent : Colors.white.withOpacity(0.06),
        ),
      ),
    );
  }

  Widget _buildHistoryItem(Transaction tx, Currency currency) {
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
        ? DateFormat('dd.MM.yyyy - HH:mm').format(tx.createdDate!)
        : '';

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      decoration: BoxDecoration(
        color: PremiumTheme.cardBg,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.white.withOpacity(0.04), width: 1),
      ),
      child: Row(
        children: [
          // Icon Box
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color, size: 20),
          ),
          const SizedBox(width: 16),
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
                const SizedBox(height: 6),
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
          // Amount & Description Column
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '$sign${_getCurrencySymbol(currency)}${tx.amount.toStringAsFixed(2)}',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: color,
                ),
              ),
              if (tx.description.isNotEmpty) ...[
                const SizedBox(height: 4),
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
