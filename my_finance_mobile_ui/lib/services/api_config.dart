import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;

class ApiConfig {
  // Configured to point to the Spring Cloud Gateway (port 8080)
  static String get baseUrl {
    if (kIsWeb) {
      return 'http://localhost:8080';
    }
    try {
      if (Platform.isAndroid) {
        // Android emulator needs 10.0.2.2 to refer to the host machine's localhost
        return 'http://10.0.2.2:8080';
      }
    } catch (_) {}
    return 'http://localhost:8080';
  }
}
