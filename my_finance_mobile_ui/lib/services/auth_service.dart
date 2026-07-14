import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/models.dart';
import 'api_config.dart';

class AuthService {
  final String _authUrl = '${ApiConfig.baseUrl}/auth';

  // Perform login
  Future<AuthResponse> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$_authUrl/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    );

    if (response.statusCode == 200) {
      final decodedJson = jsonDecode(utf8.decode(response.bodyBytes));
      final authResponse = AuthResponse.fromJson(decodedJson);
      await saveSession(authResponse);
      return authResponse;
    } else {
      String errorMessage = 'Giriş yapılamadı!';
      try {
        final errorBody = jsonDecode(utf8.decode(response.bodyBytes));
        errorMessage = errorBody['message'] ?? errorMessage;
      } catch (_) {}
      throw Exception(errorMessage);
    }
  }

  // Perform registration
  Future<AuthResponse> register({
    required String email,
    required String password,
    required String fullName,
    required String phoneNumber,
    String role = 'USER',
  }) async {
    final response = await http.post(
      Uri.parse('$_authUrl/register'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
        'fullName': fullName,
        'phoneNumber': phoneNumber,
        'role': role,
      }),
    );

    if (response.statusCode == 200) {
      final decodedJson = jsonDecode(utf8.decode(response.bodyBytes));
      final authResponse = AuthResponse.fromJson(decodedJson);
      await saveSession(authResponse);
      return authResponse;
    } else {
      String errorMessage = 'Kayıt olunamadı!';
      try {
        final errorBody = jsonDecode(utf8.decode(response.bodyBytes));
        errorMessage = errorBody['message'] ?? errorMessage;
      } catch (_) {}
      throw Exception(errorMessage);
    }
  }

  // Save session info
  Future<void> saveSession(AuthResponse authResponse) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('auth_token', authResponse.token);
    await prefs.setString('auth_email', authResponse.email);
    await prefs.setString('auth_name', authResponse.name);
    await prefs.setInt('auth_user_id', authResponse.userId);
  }

  // Retrieve token
  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('auth_token');
  }

  // Retrieve email
  Future<String?> getEmail() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('auth_email');
  }

  // Retrieve name
  Future<String?> getName() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('auth_name');
  }

  // Retrieve user ID
  Future<int?> getUserId() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getInt('auth_user_id');
  }

  // Clear session (logout)
  Future<void> clearSession() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('auth_token');
    await prefs.remove('auth_email');
    await prefs.remove('auth_name');
    await prefs.remove('auth_user_id');
  }

  // Check login status
  Future<bool> isLoggedIn() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }
}
