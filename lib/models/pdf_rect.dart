import 'package:json_annotation/json_annotation.dart';

part 'pdf_rect.g.dart';

@JsonSerializable()
class PdfRect {
  double? width;
  double? height;

  PdfRect();

  factory PdfRect.fromJson(Map<String, dynamic> json) =>
      _$PdfRectFromJson(json);

  Map<String, dynamic> toJson() => _$PdfRectToJson(this);
}
