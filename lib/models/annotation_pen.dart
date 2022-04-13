import 'package:file_reader/models/pdf_rect.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_pen.g.dart';

@JsonSerializable()
class AnnotationPen {
  String? annotationType;
  int? page;
  PdfRect? pageSize;
  String? pen;
  List<PdfRect>? data;
  String? id;

  AnnotationPen();

  factory AnnotationPen.fromJson(Map<String, dynamic> json) =>
      _$AnnotationPenFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationPenToJson(this);

  static List<List<double>> convertPdfRect(List<PdfRect> data) {
    List<List<double>> result = [];
    data.reversed.forEach((item) {
      List<double> rect = [];
      rect.add(item.width ?? 0);
      rect.add(item.height ?? 0);
      result.add(rect);
    });
    return result;
  }
}
