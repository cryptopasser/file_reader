import 'package:file_reader/models/pdf_rect.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_mark.g.dart';

@JsonSerializable()
class AnnotationMark {
  String? annotationType;
  int? page;
  PdfRect? pageSize;
  String? pen;
  List<PdfRect>? data;
  String? id;
  int? startIndex;
  int? endIndex;
  String? markAreaType;

  AnnotationMark();

  factory AnnotationMark.fromJson(Map<String, dynamic> json) =>
      _$AnnotationMarkFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationMarkToJson(this);
}
