import 'package:json_annotation/json_annotation.dart';

part 'annotation_bean.g.dart';

@JsonSerializable()
class AnnotationBean {
  static final PEN_TYPE_COLOR_PEN = "COLORPEN";
  static final PEN_TYPE_DELETE_LINE = "DELETELINE";
  static final PEN_TYPE_UNDER_LINE = "UNDERLINE";
  static final PEN_TYPE_UNDER_WAVELINE = "UNDERWAVELINE";
  static final PEN_TYPE_HIGHLIGHT_PEN = "HIGHLIGHTPEN";
  static final PEN_TYPE_TEXT_PEN = "TEXTPEN";

  String? id;
  int? page;
  String? penType;
  dynamic data;

  AnnotationBean();

  factory AnnotationBean.fromJson(Map<String, dynamic> json) =>
      _$AnnotationBeanFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationBeanToJson(this);
}
