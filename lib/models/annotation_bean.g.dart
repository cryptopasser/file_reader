// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_bean.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationBean _$AnnotationBeanFromJson(Map<String, dynamic> json) {
  return AnnotationBean()
    ..id = json['id'] as String?
    ..page = json['page'] as int?
    ..penType = json['penType'] as String?
    ..data = json['data'];
}

Map<String, dynamic> _$AnnotationBeanToJson(AnnotationBean instance) =>
    <String, dynamic>{
      'id': instance.id,
      'page': instance.page,
      'penType': instance.penType,
      'data': instance.data,
    };
