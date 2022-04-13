// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_mark.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationMark _$AnnotationMarkFromJson(Map<String, dynamic> json) {
  return AnnotationMark()
    ..annotationType = json['annotationType'] as String?
    ..page = json['page'] as int?
    ..pageSize = json['pageSize'] == null
        ? null
        : PdfRect.fromJson(json['pageSize'] as Map<String, dynamic>)
    ..pen = json['pen'] as String?
    ..data = (json['data'] as List<dynamic>?)
        ?.map((e) => PdfRect.fromJson(e as Map<String, dynamic>))
        .toList()
    ..id = json['id'] as String?
    ..startIndex = json['startIndex'] as int?
    ..endIndex = json['endIndex'] as int?
    ..markAreaType = json['markAreaType'] as String?;
}

Map<String, dynamic> _$AnnotationMarkToJson(AnnotationMark instance) =>
    <String, dynamic>{
      'annotationType': instance.annotationType,
      'page': instance.page,
      'pageSize': instance.pageSize,
      'pen': instance.pen,
      'data': instance.data,
      'id': instance.id,
      'startIndex': instance.startIndex,
      'endIndex': instance.endIndex,
      'markAreaType': instance.markAreaType,
    };
