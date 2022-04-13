// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_pen.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationPen _$AnnotationPenFromJson(Map<String, dynamic> json) {
  return AnnotationPen()
    ..annotationType = json['annotationType'] as String?
    ..page = json['page'] as int?
    ..pageSize = json['pageSize'] == null
        ? null
        : PdfRect.fromJson(json['pageSize'] as Map<String, dynamic>)
    ..pen = json['pen'] as String?
    ..data = (json['data'] as List<dynamic>?)
        ?.map((e) => PdfRect.fromJson(e as Map<String, dynamic>))
        .toList()
    ..id = json['id'] as String?;
}

Map<String, dynamic> _$AnnotationPenToJson(AnnotationPen instance) =>
    <String, dynamic>{
      'annotationType': instance.annotationType,
      'page': instance.page,
      'pageSize': instance.pageSize,
      'pen': instance.pen,
      'data': instance.data,
      'id': instance.id,
    };
