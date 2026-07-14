import 'package:flutter/material.dart';
import 'providers/app_state.dart';
import 'screens/home_navigation_screen.dart';
import 'screens/login_screen.dart';
import 'widgets/theme.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late final AppState _appState;
  bool _initialized = false;

  @override
  void initState() {
    super.initState();
    _appState = AppState();
    _initApp();
  }

  Future<void> _initApp() async {
    // Check if user is already logged in (restores token and sessions)
    await _appState.checkLoginStatus();
    if (mounted) {
      setState(() {
        _initialized = true;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppStateProvider(
      notifier: _appState,
      child: MaterialApp(
        title: 'Aether Finance',
        debugShowCheckedModeBanner: false,
        theme: PremiumTheme.darkTheme,
        home: _initialized ? const MainAppNavigator() : const AppSplashScreen(),
      ),
    );
  }
}

// Controls whether to show Dashboard or Login screen based on auth state
class MainAppNavigator extends StatelessWidget {
  const MainAppNavigator({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = AppStateProvider.of(context);

    if (appState.isLoggedIn) {
      return const HomeNavigationScreen();
    } else {
      return const LoginScreen();
    }
  }
}

// Elegant loading splash screen shown on app launch
class AppSplashScreen extends StatelessWidget {
  const AppSplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: PremiumTheme.obsidianBg,
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 90,
              height: 90,
              decoration: BoxDecoration(
                gradient: PremiumTheme.primaryGradient,
                shape: BoxShape.circle,
                boxShadow: PremiumTheme.neonShadow(PremiumTheme.luxuryIndigo),
              ),
              child: const Icon(
                Icons.account_balance_wallet,
                size: 44,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 24),
            const Text(
              'Aether Finance',
              style: TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.bold,
                letterSpacing: 1.2,
                color: Colors.white,
              ),
            ),
            const SizedBox(height: 12),
            const SizedBox(
              width: 140,
              child: LinearProgressIndicator(
                color: PremiumTheme.neonTeal,
                backgroundColor: PremiumTheme.cardBg,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
