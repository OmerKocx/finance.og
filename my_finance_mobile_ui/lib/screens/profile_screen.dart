import 'package:flutter/material.dart';
import '../providers/app_state.dart';
import '../widgets/theme.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = AppStateProvider.of(context);
    final initial = appState.userName?.isNotEmpty == true
        ? appState.userName![0].toUpperCase()
        : 'U';

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const SizedBox(height: 12),
          // Profile Avatar Circle
          Center(
            child: Container(
              width: 100,
              height: 100,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: PremiumTheme.primaryGradient,
                boxShadow: PremiumTheme.neonShadow(PremiumTheme.luxuryIndigo),
              ),
              alignment: Alignment.center,
              child: Text(
                initial,
                style: const TextStyle(
                  fontSize: 40,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
            ),
          ),
          const SizedBox(height: 20),
          Center(
            child: Text(
              appState.userName ?? 'Ad Soyad Bulunamadı',
              style: const TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
                color: Colors.white,
              ),
            ),
          ),
          const SizedBox(height: 6),
          Center(
            child: Text(
              appState.userEmail ?? 'Email Bulunamadı',
              style: const TextStyle(
                fontSize: 14,
                color: PremiumTheme.textSecondary,
              ),
            ),
          ),
          const SizedBox(height: 40),

          // Details List
          const Text(
            'HESAP BİLGİLERİ',
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.bold,
              letterSpacing: 1.5,
              color: PremiumTheme.textSecondary,
            ),
          ),
          const SizedBox(height: 12),

          _buildProfileDetailItem(
            icon: Icons.person_outline,
            label: 'Ad Soyad',
            value: appState.userName ?? '-',
          ),
          const SizedBox(height: 12),
          _buildProfileDetailItem(
            icon: Icons.email_outlined,
            label: 'E-posta',
            value: appState.userEmail ?? '-',
          ),
          const SizedBox(height: 12),
          _buildProfileDetailItem(
            icon: Icons.phone_android_outlined,
            label: 'Telefon Numarası',
            value: 'Belirtilmemiş', // Phone number is stored in customer service, or default to display
          ),
          const SizedBox(height: 12),
          _buildProfileDetailItem(
            icon: Icons.admin_panel_settings_outlined,
            label: 'Hesap Rolü',
            value: 'USER',
          ),
          
          const SizedBox(height: 48),

          // Logout Button
          OutlinedButton.icon(
            onPressed: () {
              showDialog(
                context: context,
                builder: (context) => AlertDialog(
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  backgroundColor: PremiumTheme.cardBg,
                  title: const Text('Çıkış Yap', style: TextStyle(color: Colors.white)),
                  content: const Text('Hesabınızdan çıkış yapmak istediğinize emin misiniz?'),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.of(context).pop(),
                      child: const Text('Vazgeç', style: TextStyle(color: PremiumTheme.textSecondary)),
                    ),
                    TextButton(
                      onPressed: () {
                        Navigator.of(context).pop();
                        appState.logout();
                      },
                      child: const Text('Çıkış Yap', style: TextStyle(color: PremiumTheme.errorRed, fontWeight: FontWeight.bold)),
                    ),
                  ],
                ),
              );
            },
            icon: const Icon(Icons.logout_rounded, color: PremiumTheme.errorRed, size: 20),
            label: const Text('Oturumu Kapat', style: TextStyle(color: PremiumTheme.errorRed, fontWeight: FontWeight.bold)),
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
              side: BorderSide(color: PremiumTheme.errorRed.withOpacity(0.4), width: 1.5),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _buildProfileDetailItem({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
      decoration: BoxDecoration(
        color: PremiumTheme.cardBg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.04), width: 1),
      ),
      child: Row(
        children: [
          Icon(icon, color: PremiumTheme.deepPurple, size: 20),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: const TextStyle(
                    fontSize: 11,
                    color: PremiumTheme.textSecondary,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
              ],
            ),
          )
        ],
      ),
    );
  }
}
