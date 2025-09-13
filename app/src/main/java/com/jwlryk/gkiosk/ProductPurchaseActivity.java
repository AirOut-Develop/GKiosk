package com.jwlryk.gkiosk;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.provider.MediaStore;
import android.os.Build;
import android.graphics.Rect;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.concurrent.ExecutionException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

public class ProductPurchaseActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "product_id";
    public static final String EXTRA_CODE = "product_code";
    public static final String EXTRA_TITLE = "product_title";
    public static final String EXTRA_PRICE = "product_price";
    public static final String EXTRA_IMAGE = "product_image";
    public static final String EXTRA_BRAND = "product_brand";
    public static final String EXTRA_DESC = "product_desc";
    public static final String EXTRA_VERIFY_MODE = "verify_mode"; // values: operation|development

    private enum VerifyMode { OPERATION, DEVELOPMENT }
    private VerifyMode verifyMode = VerifyMode.DEVELOPMENT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_purchase);
        UiUtil.enableImmersiveMode(this);

        // Camera preview for identity verification
        previewView = findViewById(R.id.pp_preview);
        // Determine verification mode
        String m = getIntent().getStringExtra(EXTRA_VERIFY_MODE);
        verifyMode = (m != null && m.equalsIgnoreCase("operation")) ? VerifyMode.OPERATION : VerifyMode.DEVELOPMENT;
        statusView = findViewById(R.id.pp_status);
        updateStatus("모드: " + verifyMode.name());
        if (hasCameraPermission()) startCamera(); else requestCameraPermission();

        TextView tvBrand = findViewById(R.id.pp_brand);
        TextView tvTitle = findViewById(R.id.pp_title);
        TextView tvPrice = findViewById(R.id.pp_price);
        TextView tvDesc = findViewById(R.id.pp_desc);
        ImageView iv = findViewById(R.id.pp_image);

        String brand = getIntent().getStringExtra(EXTRA_BRAND);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        long price = getIntent().getLongExtra(EXTRA_PRICE, 0);
        String img = getIntent().getStringExtra(EXTRA_IMAGE);
        String desc = getIntent().getStringExtra(EXTRA_DESC);

        if (tvBrand != null) tvBrand.setText(brand != null ? brand : "");
        if (tvTitle != null) tvTitle.setText(title != null ? title : "");
        if (tvPrice != null) tvPrice.setText(formatPrice(price));
        if (tvDesc != null) tvDesc.setText(desc != null && !desc.isEmpty() ? desc : getString(R.string.vat_included));
        if (iv != null && img != null && (img.startsWith("http://") || img.startsWith("https://"))) {
            Glide.with(this).load(img).placeholder(R.drawable.grad_01).centerCrop().into(iv);
        }

        findViewById(R.id.pp_btn_back).setOnClickListener(v -> finish());
    }

    private String formatPrice(long price) {
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.KOREA);
        return nf.format(price);
    }

    private static final int REQ_CAMERA = 1001;
    private PreviewView previewView;
    private TextView statusView;
    // fields defined above (single declarations kept)
    private ImageCapture imageCapture;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;
    private boolean finished = false;
    private long startTs = 0L;
    private static final long FALLBACK_MS = 5000L;

    private final FaceDetector detector = FaceDetection.getClient(
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .enableTracking()
                    .build()
    );

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onGalleryPicked);

    private void updateStatus(String message) {
        runOnUiThread(() -> {
            if (statusView != null && message != null) {
                statusView.setText(message);
            }
        });
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        updateStatus("카메라 권한 요청");
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQ_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA) {
            if (hasCameraPermission()) { updateStatus("카메라 권한 허용됨 → 시작"); startCamera(); }
            else { updateStatus("카메라 권한 거부됨"); Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show(); }
        }
    }

    private void startCamera() {
        if (previewView == null) return;
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new androidx.camera.core.ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector selector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
                updateStatus("카메라 바인딩 완료");
                if (task != null && !task.isCancelled()) task.cancel(true);
                finished = false;
                if (verifyMode == VerifyMode.OPERATION) {
                    // 운영모드: 5초 딜레이 루핑(최대 3회)
                    attempts = 0;
                    task = scheduler.scheduleAtFixedRate(() -> {
                        if (finished) return;
                        if (attempts >= MAX_ATTEMPTS) {
                            finished = true; stopTask();
                            runOnUiThread(() -> showResultDialog(false, "인증 실패: 3회 미검출"));
                            return;
                        }
                        attempts++;
                        updateStatus("촬영 시도 " + attempts + "/" + MAX_ATTEMPTS);
                        takeOneShotAndDetect();
                    }, 5, 5, java.util.concurrent.TimeUnit.SECONDS);
                } else {
                    // 개발모드: 5초 후 1회 촬영, 실패 시 갤러리로
                    updateStatus("5초 후 1회 촬영(개발모드)");
                    task = scheduler.schedule(this::takeOneShotAndDetect, 5, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                e.printStackTrace();
                openGalleryFallback("카메라 초기화 실패");
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void stopTask() { if (task != null) { task.cancel(true); task = null; } }
    private static final int MAX_ATTEMPTS = 3;
    private int attempts = 0;

    private void takeOneShotAndDetect() {
        if (imageCapture == null || finished) return;
        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                updateStatus("촬영 성공, 분석 시작");
                analyzeTwoRotations(imageProxy);
            }
            @Override public void onError(@NonNull ImageCaptureException exception) {
                updateStatus("촬영 실패: " + exception.getMessage());
                openGalleryFallback("촬영 실패: " + exception.getMessage());
            }
        });
    }

    private void analyzeTwoRotations(@NonNull ImageProxy imageProxy) {
        try {
            if (imageProxy.getImage() == null) { openGalleryFallback("유효하지 않은 프레임"); return; }
            final int baseRot = imageProxy.getImageInfo().getRotationDegrees();
            final int imgW = imageProxy.getWidth();
            InputImage img0 = InputImage.fromMediaImage(imageProxy.getImage(), baseRot);
            detector.process(img0).addOnSuccessListener(faces0 -> {
                if (faces0 != null && !faces0.isEmpty()) {
                    Face main = pickLargest(faces0);
                    onClassified(main, imgW, false);
                    imageProxy.close();
                } else {
                    InputImage img180 = InputImage.fromMediaImage(imageProxy.getImage(), (baseRot + 180) % 360);
                    detector.process(img180).addOnSuccessListener(faces180 -> {
                        if (faces180 != null && !faces180.isEmpty()) {
                            Face main = pickLargest(faces180);
                            onClassified(main, imgW, true);
                        } else {
                            openGalleryFallback("얼굴 미검출: 갤러리에서 다시 시도합니다.");
                        }
                    }).addOnFailureListener(e -> openGalleryFallback("검출 실패(180°): " + e.getMessage()))
                      .addOnCompleteListener(t -> imageProxy.close());
                }
            }).addOnFailureListener(e -> { openGalleryFallback("검출 실패(0°): " + e.getMessage()); imageProxy.close(); });
        } catch (Exception ex) { imageProxy.close(); openGalleryFallback("분석 오류: " + ex.getMessage()); }
    }

    private Face pickLargest(java.util.List<Face> faces) {
        Face best = faces.get(0); int bestArea = area(best.getBoundingBox());
        for (int i = 1; i < faces.size(); i++) { int a = area(faces.get(i).getBoundingBox()); if (a > bestArea) { best = faces.get(i); bestArea = a; } }
        return best;
    }
    private int area(android.graphics.Rect r) { return Math.max(0, r.width()) * Math.max(0, r.height()); }

    private void onClassified(Face face, int imgWidth, boolean isFlipped) {
        if (finished) return;
        int cx = face.getBoundingBox().centerX(); boolean isLeft = cx < (imgWidth / 2);
        String result = (!isFlipped ? (isLeft ? "운전면허증" : "신분증") : (isLeft ? "신분증" : "운전면허증"));
        finished = true; stopTask();
        runOnUiThread(() -> {
            TextView tvTitle = findViewById(R.id.pp_title);
            if (tvTitle != null) tvTitle.setText("분류 결과: " + result);
            showResultDialog(true, result);
        });
    }

    private void openGalleryFallback(String reason) {
        if (finished) return;
        if (verifyMode == VerifyMode.OPERATION) {
            // 운영모드: 갤러리 사용하지 않고 실패 누적
            runOnUiThread(() -> Toast.makeText(this, reason, Toast.LENGTH_SHORT).show());
            // do nothing else; next loop attempt (if any) will run, or fail at MAX_ATTEMPTS
            return;
        }
        // 개발모드: 갤러리로 전환
        runOnUiThread(() -> Toast.makeText(this, reason, Toast.LENGTH_SHORT).show());
        pickImageLauncher.launch("image/*"); finished = true; stopTask();
    }

    private void onGalleryPicked(Uri uri) {
        if (uri == null) { runOnUiThread(() -> Toast.makeText(this, "갤러리 선택 취소", Toast.LENGTH_SHORT).show()); return; }
        // Re-enable processing for gallery path
        finished = false;
        updateStatus("갤러리 이미지 분석 중...");
        try {
            // Use fromFilePath to respect EXIF orientation (common cause of poor detection)
            InputImage image = InputImage.fromFilePath(this, uri);
            final int imgWidth = image.getWidth();
            detector.process(image).addOnSuccessListener(faces -> {
                if (faces != null && !faces.isEmpty()) {
                    // 개발모드: 검출 결과를 그려서 프리뷰로 보여줌
                    if (verifyMode == VerifyMode.DEVELOPMENT) {
                        try {
                            Bitmap oriented = loadOrientedBitmap(uri, 1600);
                            if (oriented != null) {
                                Bitmap annotated = drawFaces(oriented, faces, 5f);
                                runOnUiThread(() -> showImageDialog(annotated));
                            }
                        } catch (Exception ignore) { /* no-op visual aid */ }
                    }
                    Face main = pickLargest(faces);
                    onClassified(main, imgWidth, false);
                } else {
                    runOnUiThread(() -> showResultDialog(false, "얼굴이 감지되지 않았습니다."));
                }
            }).addOnFailureListener(e -> runOnUiThread(() -> Toast.makeText(this, "갤러리 검출 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "갤러리 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private Bitmap loadBitmapFromUri(Uri uri) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= 28) { ImageDecoder.Source src = ImageDecoder.createSource(getContentResolver(), uri); return ImageDecoder.decodeBitmap(src, (decoder, info, s) -> decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE)); }
        else { return MediaStore.Images.Media.getBitmap(getContentResolver(), uri); }
    }

    private Bitmap loadOrientedBitmap(Uri uri, int maxEdge) throws Exception {
        Bitmap raw = loadBitmapFromUri(uri);
        if (raw == null) return null;
        // Read EXIF orientation and rotate if necessary
        int rotation = 0;
        try (java.io.InputStream in = getContentResolver().openInputStream(uri)) {
            if (in != null) {
                ExifInterface exif = new ExifInterface(in);
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90: rotation = 90; break;
                    case ExifInterface.ORIENTATION_ROTATE_180: rotation = 180; break;
                    case ExifInterface.ORIENTATION_ROTATE_270: rotation = 270; break;
                    default: rotation = 0; break;
                }
            }
        } catch (Exception ignored) {}

        Bitmap rotated = (rotation != 0) ? rotateBitmap(raw, rotation) : raw;
        if (rotated != raw) { try { raw.recycle(); } catch (Exception ignored) {} }
        return scaleToLongEdge(rotated, maxEdge);
    }

    private Bitmap rotateBitmap(Bitmap src, float degrees) {
        Matrix m = new Matrix();
        m.postRotate(degrees);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
    }

    private Bitmap scaleToLongEdge(Bitmap src, int maxEdge) {
        if (src == null) return null;
        int w = src.getWidth(); int h = src.getHeight();
        int longEdge = Math.max(w, h);
        if (longEdge <= maxEdge) return src;
        float scale = maxEdge / (float) longEdge;
        int newW = Math.round(w * scale); int newH = Math.round(h * scale);
        Bitmap out = Bitmap.createScaledBitmap(src, newW, newH, true);
        try { if (out != src) src.recycle(); } catch (Exception ignored) {}
        return out;
    }

    private Bitmap drawFaces(Bitmap src, java.util.List<Face> faces, float strokePx) {
        if (src == null) return null;
        Bitmap out = src.copy(Bitmap.Config.ARGB_8888, true);
        android.graphics.Canvas canvas = new android.graphics.Canvas(out);
        android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        p.setStyle(android.graphics.Paint.Style.STROKE);
        p.setStrokeWidth(strokePx);
        p.setColor(android.graphics.Color.RED);

        android.graphics.Paint text = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        text.setColor(android.graphics.Color.RED);
        text.setTextSize(36f);

        if (faces != null) {
            for (int i = 0; i < faces.size(); i++) {
                android.graphics.Rect r = faces.get(i).getBoundingBox();
                canvas.drawRect(r, p);
                canvas.drawText("Face " + (i + 1), r.left, Math.max(0, r.top - 8), text);
            }
        }
        return out;
    }

    private void showImageDialog(Bitmap bmp) {
        if (bmp == null) return;
        android.widget.ImageView iv = new android.widget.ImageView(this);
        iv.setAdjustViewBounds(true);
        iv.setImageBitmap(bmp);
        int pad = (int) getResources().getDimension(R.dimen.space_m);
        iv.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(this)
                .setTitle("검출 미리보기")
                .setView(iv)
                .setPositiveButton("확인", (d, w) -> d.dismiss())
                .show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { detector.close(); } catch (Exception ignored) {}
        scheduler.shutdownNow();
    }

    private void showResultDialog(boolean success, String payload) {
        String title = success ? "인증 완료" : "인증 실패";
        String msg = success ? ("분류 결과: " + payload) : (payload == null ? "인증에 실패했습니다." : payload);
        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg);
        if (success) {
            b.setPositiveButton("카드결제로 이동", (d, w) -> navigateToCardPayment())
             .setNegativeButton("닫기", (d,w)-> d.dismiss());
        } else {
            b.setPositiveButton("확인", (d, w) -> d.dismiss());
        }
        b.show();
    }

    private void navigateToCardPayment() {
        android.content.Intent intent = new android.content.Intent(this, CardPaymentActivity.class);
        intent.putExtra(EXTRA_TITLE, getIntent().getStringExtra(EXTRA_TITLE));
        intent.putExtra(EXTRA_PRICE, getIntent().getLongExtra(EXTRA_PRICE, 0));
        intent.putExtra(EXTRA_IMAGE, getIntent().getStringExtra(EXTRA_IMAGE));
        startActivity(intent);
    }
}
