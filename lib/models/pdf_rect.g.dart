// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pdf_rect.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PdfRect _$PdfRectFromJson(Map<String, dynamic> json) {
  return PdfRect()
    ..width = (json['width'] as num?)?.toDouble()
    ..height = (json['height'] as num?)?.toDouble();
}

Map<String, dynamic> _$PdfRectToJson(PdfRect instance) => <String, dynamic>{
      'width': instance.width,
      'height': instance.height,
    };
