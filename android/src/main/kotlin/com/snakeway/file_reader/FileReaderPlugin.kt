package com.snakeway.file_reader

import androidx.annotation.NonNull
import android.content.Intent
import android.app.Activity
import android.os.Looper

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.BinaryMessenger

import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import pub.devrel.easypermissions.EasyPermissions
import com.snakeway.file_reader.listeners.MarkChangeListener
import android.util.Log


/** FileReaderPlugin */
class FileReaderPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private val PLUGIN_NAMESPACE = "com.snakeway.file_reader"
    private val REQUEST_CODE = 100
    private val IS_SCREEN_CUSTOM = "isScreenCustom"
    private val MARK_INFO_TYPE = "type"
    private val MARK_INFO_KEY = "key"
    private val MARK_INFO_DATA = "data"
    private val EXTRA = "extra"

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var result: Result? = null
    private var activity: Activity? = null

    private val handler: android.os.Handler = android.os.Handler(Looper.getMainLooper())

    private var pdfMarkListenerSink: EventSink? = null;

    private val streamHandler = object : StreamHandler {
        override fun onListen(obj: Any?, sink: EventSink?) {
            pdfMarkListenerSink = sink
        }

        override fun onCancel(obj: Any?) {
            cancelPdfMarkListener()
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "$PLUGIN_NAMESPACE/file_reader")
        channel.setMethodCallHandler(this)
        initEventChannel(flutterPluginBinding.binaryMessenger)
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val binaryMessenger: BinaryMessenger = registrar.messenger()
            val channel = MethodChannel(binaryMessenger, "com.snakeway.file_reader/file_reader")
            val plugin: FileReaderPlugin = FileReaderPlugin()
            plugin.activity = registrar.activity()
            channel.setMethodCallHandler(plugin)
            plugin.initEventChannel(binaryMessenger)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        this.result = result
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "isPdfActivityOpen") {
            result.success(PdfViewerActivity.isPdfViewerActivityOpen())
        } else if (call.method == "openFile") {
            val fileKey: String = call.argument<String>("fileKey") ?: ""
            val fileName: String = call.argument<String>("fileName") ?: ""
            val fileUrl: String = call.argument<String>("fileUrl") ?: ""
            val fileType: String = call.argument<String>("fileType") ?: ""
            val filePassword: String = call.argument<String>("filePassword") ?: ""
            val annotation: String = call.argument<String>("annotation") ?: ""
            val isReadOnly: Boolean = call.argument<Boolean>("isReadOnly") ?: false
            val openAutoSpace: Boolean = call.argument<Boolean>("openAutoSpace") ?: false
            val page: Int = call.argument<Int>("page") ?: 0
            val menu: String = call.argument<String>("menu") ?: ""
            val extra: String = call.argument<String>("extra") ?: ""
            openLoadingActivity(fileKey, fileName, fileUrl, fileType, filePassword, annotation, isReadOnly,openAutoSpace,page,menu,extra)
            result.success("success")
        } else if (call.method == "updatePage") {
            val page: String = call.argument<String>("page") ?: "0"
            val pdfViewerActivity: PdfViewerActivity = PdfViewerActivity.getPdfViewerActivity();
            if (pdfViewerActivity != null) {
                pdfViewerActivity.updatePage(page.toInt());
            }
            result.success(true)
        } else if (call.method == "addAnnotation") {
            val annotation: String = call.argument<String>("annotation") ?: ""
            val pdfViewerActivity: PdfViewerActivity = PdfViewerActivity.getPdfViewerActivity();
            if (pdfViewerActivity != null) {
                pdfViewerActivity.addRemoveAnnotation(annotation, true);
            }
            result.success(true)
        } else if (call.method == "addAnnotations") {
            val annotations: String = call.argument<String>("annotations") ?: ""
            val pdfViewerActivity: PdfViewerActivity = PdfViewerActivity.getPdfViewerActivity();
            if (pdfViewerActivity != null) {
                pdfViewerActivity.addRemoveAnnotations(annotations, true);
            }
            result.success(true)
        } else if (call.method == "removeAnnotation") {
            val annotation: String = call.argument<String>("annotation") ?: ""
            val pdfViewerActivity: PdfViewerActivity = PdfViewerActivity.getPdfViewerActivity();
            if (pdfViewerActivity != null) {
                pdfViewerActivity.addRemoveAnnotation(annotation, false);
            }
            result.success(true)
        } else if (call.method == "removeAnnotations") {
            val annotations: String = call.argument<String>("annotations") ?: ""
            val pdfViewerActivity: PdfViewerActivity = PdfViewerActivity.getPdfViewerActivity();
            if (pdfViewerActivity != null) {
                pdfViewerActivity.addRemoveAnnotations(annotations, false);
            }
            result.success(true)
        } else if (call.method == "closePdfActivity") {
            val pdfViewerActivity: PdfViewerActivity? = PdfViewerActivity.getPdfViewerActivity();
            if (pdfViewerActivity != null) {
                pdfViewerActivity.closePdfActivity();
            }
            result.success(true)
        } else if (call.method == "initPdfMarkListener") {
            result.success(true)
        } else {
            result.notImplemented()
        }
    }


    fun openLoadingActivity(fileKey: String?, fileName: String?, fileUrl: String?, fileType: String?, filePassword: String?, annotation: String?, isReadOnly: Boolean?,openAutoSpace: Boolean?,page: Int?, menu: String?, extra: String?) {
        AppConfig.init(activity);
        if (!EasyPermissions.hasPermissions(activity!!, *AppConfig.BASE_X5_PERMISSIONS)) {
            val intent = Intent(activity, LoadingActivity::class.java)
            intent.putExtra(AppConfig.FILE_KEY, fileKey)
            intent.putExtra(AppConfig.FILE_NAME_KEY, fileName)
            intent.putExtra(AppConfig.FILE_PATH_KEY, fileUrl)
            intent.putExtra(AppConfig.FILE_TYPE_KEY, fileType)
            intent.putExtra(AppConfig.FILE_PASSWORD_KEY, filePassword)
            intent.putExtra(AppConfig.FILE_ANNOTATION_KEY, annotation)
            intent.putExtra(AppConfig.IS_READ_ONLY_KEY, isReadOnly)
            intent.putExtra(AppConfig.OPEN_AUTO_SPACE_KEY, openAutoSpace)
            intent.putExtra(AppConfig.PAGE_KEY, page)
            intent.putExtra(AppConfig.MENU, menu)
            intent.putExtra(AppConfig.EXTRA, extra)
            activity!!.startActivity(intent)
        } else {
            AppConfig.openFile(activity, fileKey, fileName, fileUrl, fileType, filePassword, annotation, isReadOnly,openAutoSpace,page,menu,extra);
        }
        if (AppConfig.isPdf(activity, fileUrl, fileType)) {
            PdfViewerActivity.setPdfViewerActivityOpen(true);
        }
    }

    fun initEventChannel(binaryMessenger: BinaryMessenger) {
        AppConfig.setMarkChangeListener(object : MarkChangeListener {
            override fun updatePage(key: String?, page: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 1
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = page
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }

            override fun addAnnotation(key: String?, data: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 2
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = data
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }

            override fun updateAnnotation(key: String?, data: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 3
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = data
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }

            override fun removeAnnotation(key: String?, data: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 4
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = data
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }

            override fun annotationPageRemove(key: String?, data: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 5
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = data
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }


            override fun annotationAllRemove(key: String?, data: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 6
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = data
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }

            override fun close(key: String?, data: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 7
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = data
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }


            override fun updateInfo(key: String?, data: String?, isScreenCustom: Boolean, extra: String?) {
                val resultMap = mutableMapOf<String, Any?>()
                resultMap[MARK_INFO_TYPE] = 8
                resultMap[MARK_INFO_KEY] = key
                resultMap[MARK_INFO_DATA] = data
                resultMap[IS_SCREEN_CUSTOM] = isScreenCustom
                resultMap[EXTRA] = extra
                pdfMarkListenerSink?.success(resultMap);
            }
        });
        val eventChannel = EventChannel(binaryMessenger, "$PLUGIN_NAMESPACE/pdf_mark_listener")
        eventChannel.setStreamHandler(streamHandler)
    }

    fun cancelPdfMarkListener() {
        if (pdfMarkListenerSink != null) {
            pdfMarkListenerSink!!.endOfStream()
            pdfMarkListenerSink = null;
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}
