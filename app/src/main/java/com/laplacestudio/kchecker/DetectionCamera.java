package com.laplacestudio.kchecker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DetectionCamera {

    private static final String tag = "Detection Camera";
    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private Context context;
    private Handler handler;
    // Camera settings
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private int mCameraOrientation = 0;
    private Surface mPreviewSurface;
    private TextureView previewTextureView;
    private ImageReader captureAndSaveImageReader;
    private ImageReader singleImageReader;
    private ImageReader flowImageReader;
    public ImageReader.OnImageAvailableListener captureListener;
    public ImageReader.OnImageAvailableListener singleImageListener;
    public ImageReader.OnImageAvailableListener flowImageListener;
    public Size previewSize;


    public DetectionCamera(Context context, Handler handler, TextureView previewTextureView) {
        this.context = context;
        this.handler = handler;
        this.previewTextureView = previewTextureView;
    }

    public void start() {
        if (previewTextureView == null) return;
        this.previewTextureView.setSurfaceTextureListener(textureListener());
    }

    public void setCaptureListener() {
        if (handler == null) return;
        if(captureListener==null)return;
        captureAndSaveImageReader = ImageReader.newInstance(
                previewSize.getWidth(),
                previewSize.getHeight(),
                ImageFormat.JPEG, 1);
        captureAndSaveImageReader.setOnImageAvailableListener(captureListener, handler);
    }

    public void setSingleImageListener() {
        if (handler == null) return;
        if(singleImageListener==null)return;
        singleImageReader = ImageReader.newInstance(
                previewSize.getWidth(),
                previewSize.getHeight(),
                ImageFormat.JPEG, 1);
        singleImageReader.setOnImageAvailableListener(singleImageListener, handler);
    }

    public void setFlowImageListener() {
        if (handler == null) return;
        if(flowImageListener==null)return;
        flowImageReader = ImageReader.newInstance(
                previewSize.getWidth(),
                previewSize.getHeight(),
                ImageFormat.JPEG, 1);
        flowImageReader.setOnImageAvailableListener(flowImageListener, handler);
    }

    private TextureView.SurfaceTextureListener textureListener() {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(tag, "Preview surface texture view size: width:" + width + ", height:" + height);
                setupCamera(width, height);
                setupTextureView(width, height);
                setCaptureListener();
                setSingleImageListener();
                setFlowImageListener();
                open();
                Log.i(tag,"Read preview size:"+ previewSize.toString());
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
//                setupTextureView(width,height);
                setupTextureView(width,height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

            }
        };
    }

    private void setupCamera(int width, int height) {
        // 获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 遍历所有摄像头
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                // 默认打开后置摄像头 - 忽略前置摄像头
                if (characteristics.getKeys().contains(CameraCharacteristics.LENS_FACING) &&
                        characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                // 获取StreamConfigurationMap，得到摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                previewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
//                int orientation = context.getResources().getConfiguration().orientation;
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    previewTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                } else {
//                    previewTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//                }
                mCameraId = cameraId;
                Log.i(tag, "Got camera: " + mCameraId);
                break;
            }
        } catch (CameraAccessException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void open() {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //检查权限
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开相机，第一个参数指示打开哪个摄像头，
            // 第二个参数stateCallback为相机的状态回调接口，
            // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            manager.openCamera(mCameraId, stateCallback(), null);
        } catch (CameraAccessException e) {
            Log.e(tag, "Open camera failed.");
            e.printStackTrace();
        }
    }

    public void close() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != captureAndSaveImageReader) {
            captureAndSaveImageReader.close();
            captureAndSaveImageReader = null;
        }
    }

    private void setupTextureView(int viewWidth, int viewHeight) {
        if (null == previewTextureView || null == previewSize) {
            return;
        }
        WindowManager manager=(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mCameraOrientation = manager.getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == mCameraOrientation || Surface.ROTATION_270 == mCameraOrientation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (mCameraOrientation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == mCameraOrientation) {
            matrix.postRotate(180, centerX, centerY);
        }
        previewTextureView.setTransform(matrix);
    }

    private void startPreview() {
//        SurfaceTexture mSurfaceTexture = previewTextureView.getSurfaceTexture();
//        if (mSurfaceTexture == null) return;
//        //设置TextureView的缓冲区大小
//        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        mPreviewSurface = new Surface(mSurfaceTexture);
        //获取Surface显示预览数据
        SurfaceTexture surfaceTexture=previewTextureView.getSurfaceTexture();
        if(surfaceTexture==null)return;
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(),previewSize.getHeight());
        mPreviewSurface=new Surface(surfaceTexture);
        List<Surface> surfaces = Arrays.asList(mPreviewSurface,
                captureAndSaveImageReader.getSurface(),
                singleImageReader.getSurface(),
                flowImageReader.getSurface());
        try {
            getPreviewRequestBuilder();
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，
            // 第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，
            // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCaptureSession = session;
                    repeatPreview();
                    Log.i(tag, "Configure done.");
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(tag, "Configure failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void repeatPreview() {
        mPreviewRequestBuilder.setTag(tag);
        CaptureRequest mPreviewRequest = mPreviewRequestBuilder.build();
        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {

                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    private void getPreviewRequestBuilder() {
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //设置预览的显示界面
        mPreviewRequestBuilder.addTarget(mPreviewSurface);
        MeteringRectangle[] meteringRectangles = mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_REGIONS);
        if (meteringRectangles != null && meteringRectangles.length > 0) {
            Log.d(tag, "PreviewRequestBuilder: AF_REGIONS=" + meteringRectangles[0].getRect().toString());
        }
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
    }

    public void captureAndSave(){
        try {
            // 首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureBuilder.addTarget(mPreviewSurface);
            mCaptureBuilder.addTarget(captureAndSaveImageReader.getSurface());
            // 设置拍照方向
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(mCameraOrientation));

            // 停止预览
            mCaptureSession.stopRepeating();
            // 开始拍照，然后回调上面的接口重启预览，
            // 因为mCaptureBuilder设置ImageReader作为target，
            // 所以会自动回调ImageReader的onImageAvailable()方法保存图片
            mCaptureSession.capture(mCaptureBuilder.build(), captureCallback(), handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void runSingleDetection() {
        try {
            // 首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureBuilder.addTarget(mPreviewSurface);
            mCaptureBuilder.addTarget(singleImageReader.getSurface());
            // 设置拍照方向
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(mCameraOrientation));

            // 停止预览
            mCaptureSession.stopRepeating();
            // 开始拍照，然后回调上面的接口重启预览，
            // 因为mCaptureBuilder设置singleImageReader作为target，
            // 所以会自动回调singleImageReader的onImageAvailable()方法保存图片
            mCaptureSession.capture(mCaptureBuilder.build(), captureCallback(), handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void runFlowDetection() {
        try {
            // 首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureBuilder.addTarget(mPreviewSurface);
            mCaptureBuilder.addTarget(flowImageReader.getSurface());
            // 设置拍照方向
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(mCameraOrientation));
            // 开始拍照，然后回调上面的接口重启预览，
            // 因为mCaptureBuilder设置flowImageReader作为target，
            // 所以会自动回调flowImageReader的onImageAvailable()方法保存图片
            mCaptureSession.capture(mCaptureBuilder.build(), captureCallback(), handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback stateCallback() {
        return new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                Log.i(tag, "Starting preview...");
                mCameraDevice = camera;
                //开启预览
                startPreview();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.i(tag, "CameraDevice Disconnected");
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                Log.e(tag, "CameraDevice Error");
            }
        };
    }

    private CameraCaptureSession.CaptureCallback captureCallback() {
        return new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                repeatPreview();
            }
        };
    }

    private CameraCaptureSession.CaptureCallback previewCallback(){
        return new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                super.onCaptureProgressed(session, request, partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
            }

            @NonNull
            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }

            @NonNull
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }
}
