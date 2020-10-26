package com.swein.shexoplayerdemo.framework.module.basicpermission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionManager {

    public final static int PERMISSION_REQUEST_CAMERA_CODE = 11;
    public final static int PERMISSION_REQUEST_LOCATION = 12;
    public final static int PERMISSION_REQUEST_CONTACT = 13;

    private final static String ALERT_TITLE = "권한 설정";
    private final static String ALERT_MESSAGE = "권한 설정화면에서 권한 허용 해 주세요";
    private final static String ALERT_CONFIRM = "확인";

    @SuppressLint("StaticFieldLeak")
    public static PermissionManager instance = new PermissionManager();

    private Activity activity;
    private List<String> currentPermissionsList = new ArrayList<>();

    private boolean shouldAutoExecuteMethodAfterGrantedPermission = true;

    private PermissionManager() {

    }

    private boolean hasPermission(@NonNull Activity activity, @NonNull String... permissions) {

        this.activity = activity;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.activity = null;
            return true;
        }

        for(String permission: permissions) {
            if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public boolean requestPermission(@NonNull Activity activity, boolean shouldAutoExecuteMethodAfterGrantedPermission, int requestCode, @NonNull String... permissions) {

        if(hasPermission(activity, permissions)) {
            return true;
        }

        this.shouldAutoExecuteMethodAfterGrantedPermission = shouldAutoExecuteMethodAfterGrantedPermission;

        currentPermissionsList.clear();
        currentPermissionsList.addAll(Arrays.asList(permissions));

        requestRuntimePermission(activity, permissions, requestCode);
        return false;
    }

    private void requestRuntimePermission(Activity activity, String[] permissions, int requestCode) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            this.activity = null;
            return;
        }

        List<String> permissionList = new ArrayList<>();
        for(String permission : permissions) {

            if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if(!permissionList.isEmpty()) {
            String[] permissionArray = new String[permissionList.size()];
            for(int i = 0; i < permissionList.size(); i++) {
                permissionArray[i] = permissionList.get(i);
            }
            ActivityCompat.requestPermissions(activity, permissionArray, requestCode);
        }
    }

    void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(grantResults.length > 0) {

            List<String> deniedPermissionList = new ArrayList<>();
            for(int i = 0; i < grantResults.length; i++) {

                boolean showDialogAgain = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]);

                int grantResult = grantResults[i];
                String permission = permissions[i];

                if(grantResult != PackageManager.PERMISSION_GRANTED) {

                    if(showDialogAgain) {
                        deniedPermissionList.add(permission);
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(ALERT_TITLE);
                        builder.setMessage(ALERT_MESSAGE);
                        builder.setPositiveButton(ALERT_CONFIRM, (dialog, which) -> {

                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                            intent.setData(uri);
                            activity.startActivityForResult(intent, requestCode);
                        });
                        builder.create();
                        builder.show();

                    }

                    return;
                }
            }

            if(deniedPermissionList.isEmpty()) {

                if(shouldAutoExecuteMethodAfterGrantedPermission) {
                    executeMethodWithRequestCode(activity, requestCode);
                }

                activity = null;
            }
        }
    }

    void onActivityResult(int requestCode, int resultCode) {

        if(activity == null || currentPermissionsList == null) {
            return;
        }

        List<String> deniedPermissionList = new ArrayList<>();

        if(resultCode == Activity.RESULT_CANCELED) {

            for(int i = 0; i < currentPermissionsList.size(); i++) {

                int grantResult = ContextCompat.checkSelfPermission(activity, currentPermissionsList.get(i));
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(currentPermissionsList.get(i));
                }
            }

            if(deniedPermissionList.isEmpty()) {

                if(shouldAutoExecuteMethodAfterGrantedPermission) {
                    executeMethodWithRequestCode(activity, requestCode);
                }

                activity = null;
                currentPermissionsList.clear();
            }
        }
    }

    private void executeMethodWithRequestCode(Activity activity, int requestCode){
        Class<? extends Activity> clazz = activity.getClass();

        Method[] methods = clazz.getDeclaredMethods();

        for(Method method: methods) {

            if(method.isAnnotationPresent(RequestPermission.class)) {

                RequestPermission requestPermission = method.getAnnotation(RequestPermission.class);
                if(requestPermission.permissionCode() == requestCode) {

                    Type returnType = method.getGenericReturnType();

                    if(!"void".equals(returnType.toString())) {
                        throw new RuntimeException(method.getName() + "method must return void");
                    }

                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if(parameterTypes.length > 0) {
                        throw new RuntimeException(method.getName() + "method must has empty parameters");
                    }

                    if(!method.isAccessible()) {
                        method.setAccessible(true);
                    }

                    try {
                        method.invoke(activity);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
