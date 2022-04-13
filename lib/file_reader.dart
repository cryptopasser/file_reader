import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';

import 'mark_info.dart';

class FileReader {
  static const String plugin_namespace = "com.snakeway.file_reader";

  static const MethodChannel _channel =
      const MethodChannel("$plugin_namespace/file_reader");

  static const EventChannel _channelPdfMarkListener =
      const EventChannel("$plugin_namespace/pdf_mark_listener");

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> get isPdfActivityOpen async {
    final bool result = await _channel.invokeMethod('isPdfActivityOpen');
    return result;
  }

  static Future<String> openFile(
      String? fileKey, String? fileName, String? fileUrl,
      {String? fileType,
      String? filePassword,
      String? annotation,
      bool isReadOnly = false,
      bool openAutoSpace = false,
      bool isSameScreenCustom = false,
      int? page = 0,
      String? menu,
      String? extra,
      String? waterMark}) async {
    if (kIsWeb == true ? false : Platform.isAndroid) {
      try {
        if (extra != null) {
          dynamic data = jsonDecode(extra);
          data["extra_is_same_screen_custom"] = isSameScreenCustom;
          if(waterMark!=null){
            data["waterMark"] = waterMark;
          }
          extra = jsonEncode(data);
        } else {
          Map data = Map();
          data["extra_is_same_screen_custom"] = isSameScreenCustom;
          if(waterMark!=null){
            data["waterMark"] = waterMark;
          }
          extra = jsonEncode(data);
        }

      } catch (e) {
       print(e);
      }
      return await _channel.invokeMethod('openFile', {
        "fileKey": fileKey,
        "fileName": fileName,
        "fileUrl": fileUrl,
        "fileType": fileType,
        "filePassword": filePassword,
        "annotation": annotation,
        "isReadOnly": isReadOnly,
        "openAutoSpace": openAutoSpace,
        "page": page,
        "menu": menu,
        "extra": extra
      });
    }
    return "";
  }

  static Future<bool> updatePage(int updatePage) async {
    final bool result = await _channel
        .invokeMethod('updatePage', {"page": updatePage.toString()});
    return result;
  }

  static Future<bool> addAnnotation(String annotation) async {
    final bool result = await _channel
        .invokeMethod('addAnnotation', {"annotation": annotation});
    return result;
  }

  static Future<bool> addAnnotations(String annotations) async {
    final bool result = await _channel
        .invokeMethod('addAnnotations', {"annotations": annotations});
    return result;
  }

  static Future<bool> removeAnnotation(String annotation) async {
    final bool result = await _channel
        .invokeMethod('removeAnnotation', {"annotation": annotation});
    return result;
  }

  static Future<bool> removeAnnotations(String annotations) async {
    final bool result = await _channel
        .invokeMethod('removeAnnotations', {"annotations": annotations});
    return result;
  }

  static Future<bool> closePdfActivity() async {
    final bool result = await _channel.invokeMethod('closePdfActivity', {});
    return result;
  }

  static Stream<MarkInfo> initPdfMarkListener() async* {
    late StreamSubscription subscription;
    StreamController controller = StreamController(
      onCancel: () {
        subscription.cancel();
      },
    );
    await _channel.invokeMethod('initPdfMarkListener', {});
    subscription = _channelPdfMarkListener.receiveBroadcastStream().listen(
          controller.add,
          onError: controller.addError,
          onDone: controller.close,
        );
    yield* controller.stream.map((map) => MarkInfo.fromMap(map));
  }
}
