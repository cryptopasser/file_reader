package com.snakeway.file_reader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.snakeway.file_reader.databinding.ActivityLoadingBinding;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

/**
 * @author snakeway
 * @description:
 * @date :2021/3/8 17:18
 */
public class LoadingActivity extends BaseActivity<ActivityLoadingBinding> {
    private String fileKey;
    private String fileName;
    private String filePath;
    private String fileType;
    private String filePassword;
    private String annotation;
    private boolean isReadOnly;
    private boolean openAutoSpace;
    private int page;
    private String menu;
    private String extra;

    public static void openLoadingActivity(Context context, String fileKey, String fileName, String filePath, String fileType, String filePassword, String annotation, boolean isReadOnly,boolean openAutoSpace,int page, String menu, String extra) {
        Intent intent = new Intent(context, LoadingActivity.class);
        intent.putExtra(AppConfig.FILE_KEY, fileKey);
        intent.putExtra(AppConfig.FILE_NAME_KEY, fileName);
        intent.putExtra(AppConfig.FILE_PATH_KEY, filePath);
        intent.putExtra(AppConfig.FILE_TYPE_KEY, fileType);
        intent.putExtra(AppConfig.FILE_PASSWORD_KEY, filePassword);
        intent.putExtra(AppConfig.FILE_ANNOTATION_KEY, annotation);
        intent.putExtra(AppConfig.IS_READ_ONLY_KEY, isReadOnly);
        intent.putExtra(AppConfig.OPEN_AUTO_SPACE_KEY, openAutoSpace);
        intent.putExtra(AppConfig.PAGE_KEY, page);
        intent.putExtra(AppConfig.MENU, menu);
        intent.putExtra(AppConfig.EXTRA, extra);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        fileKey = intent.getStringExtra(AppConfig.FILE_KEY);
        fileName = intent.getStringExtra(AppConfig.FILE_NAME_KEY);
        filePath = intent.getStringExtra(AppConfig.FILE_PATH_KEY);
        fileType = intent.getStringExtra(AppConfig.FILE_TYPE_KEY);
        filePassword = intent.getStringExtra(AppConfig.FILE_PASSWORD_KEY);
        annotation = intent.getStringExtra(AppConfig.FILE_ANNOTATION_KEY);
        isReadOnly = intent.getBooleanExtra(AppConfig.IS_READ_ONLY_KEY, false);
        openAutoSpace = intent.getBooleanExtra(AppConfig.OPEN_AUTO_SPACE_KEY, true);
        page = intent.getIntExtra(AppConfig.PAGE_KEY, 0);
        menu = intent.getStringExtra(AppConfig.MENU);
        extra = intent.getStringExtra(AppConfig.EXTRA);
        initAll();
    }

    @Override
    protected ActivityLoadingBinding getViewBinder() {
        return ActivityLoadingBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initHandler() {
        handler = new Handler();
    }

    @Override
    public void initUi() {

    }

    @Override
    public void initConfigUi() {

    }

    @Override
    public void initHttp() {

    }

    @Override
    public void initOther() {
        requestPermissions();
    }

    private void requestPermissions() {
        if (!EasyPermissions.hasPermissions(this, AppConfig.BASE_X5_PERMISSIONS)) {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, AppConfig.REQUEST_X5_PERMISSIONS, AppConfig.BASE_X5_PERMISSIONS)
                            .setRationale(getString(R.string.permission_request_rationale))
                            .setPositiveButtonText(getString(R.string.permission_request_ok))
                            .setNegativeButtonText(getString(R.string.permission_request_cancel))
                            .build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, new EasyPermissions.PermissionCallbacks() {
            @Override
            public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            }

            @Override
            public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
                switch (requestCode) {
                    case AppConfig.REQUEST_X5_PERMISSIONS:
                        if (perms.size() == AppConfig.BASE_X5_PERMISSIONS.length) {
                            if (AppConfig.isNeedInitX5()) {
                                AppConfig.initX5Web(LoadingActivity.this);
                                openFile();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
                if (EasyPermissions.somePermissionPermanentlyDenied(LoadingActivity.this, perms)) {
                    showAppSettingDialog();
                }
            }
        });
    }

    private void openFile() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AppConfig.openFile(LoadingActivity.this, fileKey, fileName, filePath, fileType, filePassword, annotation, isReadOnly,openAutoSpace,page,menu, extra);
                finish();
            }
        }, 1200);
    }

    private void showAppSettingDialog() {
        new AppSettingsDialog.Builder(LoadingActivity.this).setRationale(getString(R.string.permission_request_open_setting_tips)).build().show();
    }

    @Override
    public void doBack() {
        super.doBack();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

