import 'package:file_reader/models/pdf_rect.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_base.g.dart';

@JsonSerializable()
class AnnotationBase {
  String? annotationType;
  int? page;
  PdfRect? pageSize;
  String? pen;
  dynamic? data;
  String? id;

  AnnotationBase();

  factory AnnotationBase.fromJson(Map<String, dynamic> json) =>
      _$AnnotationBaseFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationBaseToJson(this);
}
