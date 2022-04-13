class MarkInfo {
  final bool isScreenCustom;
  final int type;
  final String key;
  final String data;
  final String extra;

  MarkInfo(this.isScreenCustom, this.type, this.key, this.data, this.extra);

  factory MarkInfo.fromMap(Map map) {
    return MarkInfo(
        map["isScreenCustom"], map["type"], map["key"], map["data"], map["extra"]);
  }
}
