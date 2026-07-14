import 'dart:ui';
import 'package:flutter/material.dart';

class PremiumTheme {
  // Theme Color System
  static const Color obsidianBg = Color(0xFF0E0E11);
  static const Color cardBg = Color(0xFF18181F);
  static const Color textPrimary = Color(0xFFFFFFFF);
  static const Color textSecondary = Color(0xFF8E8E9F);
  
  static const Color neonTeal = Color(0xFF00E6FF);
  static const Color deepPurple = Color(0xFF6366F1);
  static const Color luxuryIndigo = Color(0xFF4F46E5);
  static const Color errorRed = Color(0xFFEF4444);
  static const Color successGreen = Color(0xFF10B981);

  // Gradients
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [luxuryIndigo, deepPurple],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient glassGradient = LinearGradient(
    colors: [
      Color(0x15FFFFFF),
      Color(0x05FFFFFF),
    ],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient neonGradient = LinearGradient(
    colors: [neonTeal, deepPurple],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  // Box Shadow
  static List<BoxShadow> neonShadow(Color color) => [
        BoxShadow(
          color: color.withOpacity(0.3),
          blurRadius: 15,
          offset: const Offset(0, 4),
        )
      ];

  static final List<BoxShadow> premiumShadow = [
    BoxShadow(
      color: Colors.black.withOpacity(0.4),
      blurRadius: 20,
      offset: const Offset(0, 8),
    )
  ];

  // Glassmorphic Card Decoration
  static BoxDecoration glassDecoration({double borderRadius = 20.0}) {
    return BoxDecoration(
      gradient: glassGradient,
      borderRadius: BorderRadius.circular(borderRadius),
      border: Border.all(
        color: Colors.white.withOpacity(0.08),
        width: 1.0,
      ),
    );
  }

  // Premium Inputs
  static InputDecoration inputDecoration({
    required String labelText,
    required IconData prefixIcon,
    Widget? suffixIcon,
  }) {
    return InputDecoration(
      labelText: labelText,
      labelStyle: const TextStyle(color: textSecondary, fontSize: 14),
      prefixIcon: Icon(prefixIcon, color: textSecondary, size: 20),
      suffixIcon: suffixIcon,
      filled: true,
      fillColor: cardBg,
      contentPadding: const EdgeInsets.symmetric(vertical: 16.0, horizontal: 20.0),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16.0),
        borderSide: BorderSide(
          color: Colors.white.withOpacity(0.05),
          width: 1.5,
        ),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16.0),
        borderSide: const BorderSide(
          color: deepPurple,
          width: 1.5,
        ),
      ),
      errorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16.0),
        borderSide: const BorderSide(
          color: errorRed,
          width: 1.5,
        ),
      ),
      focusedErrorBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16.0),
        borderSide: const BorderSide(
          color: errorRed,
          width: 2.0,
        ),
      ),
    );
  }

  // ThemeData Export
  static ThemeData get darkTheme {
    return ThemeData.dark().copyWith(
      scaffoldBackgroundColor: obsidianBg,
      primaryColor: deepPurple,
      cardColor: cardBg,
      colorScheme: const ColorScheme.dark().copyWith(
        primary: deepPurple,
        secondary: neonTeal,
        error: errorRed,
        surface: cardBg,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0.0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: textPrimary,
          fontSize: 18,
          fontWeight: FontWeight.bold,
          letterSpacing: 0.5,
        ),
        iconTheme: IconThemeData(color: textPrimary),
      ),
      textTheme: const TextTheme(
        headlineMedium: TextStyle(color: textPrimary, fontWeight: FontWeight.bold),
        bodyLarge: TextStyle(color: textPrimary),
        bodyMedium: TextStyle(color: textSecondary),
      ),
    );
  }
}

// Glassmorphism Container Widget
class GlassContainer extends StatelessWidget {
  final Widget child;
  final double borderRadius;
  final EdgeInsetsGeometry? padding;
  final double? width;
  final double? height;

  const GlassContainer({
    super.key,
    required this.child,
    this.borderRadius = 20.0,
    this.padding,
    this.width,
    this.height,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: width,
      height: height,
      decoration: PremiumTheme.glassDecoration(borderRadius: borderRadius),
      clipBehavior: Clip.antiAlias,
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
        child: Padding(
          padding: padding ?? const EdgeInsets.all(16.0),
          child: child,
        ),
      ),
    );
  }
}
