// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_base.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationBase _$AnnotationBaseFromJson(Map<String, dynamic> json) {
  return AnnotationBase()
    ..annotationType = json['annotationType'] as String?
    ..page = json['page'] as int?
    ..pageSize = json['pageSize'] == null
        ? null
        : PdfRect.fromJson(json['pageSize'] as Map<String, dynamic>)
    ..pen = json['pen'] as String?
    ..data = json['data']
    ..id = json['id'] as String?;
}

Map<String, dynamic> _$AnnotationBaseToJson(AnnotationBase instance) =>
    <String, dynamic>{
      'annotationType': instance.annotationType,
      'page': instance.page,
      'pageSize': instance.pageSize,
      'pen': instance.pen,
      'data': instance.data,
      'id': instance.id,
    };
