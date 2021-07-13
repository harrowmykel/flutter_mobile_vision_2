import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_mobile_vision_2/flutter_mobile_vision_2.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_mobile_vision_2');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await FlutterMobileVision.platformVersion, '42');
  });
}
