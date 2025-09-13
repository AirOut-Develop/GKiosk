package com.jwlryk.gkiosk.data;

import com.jwlryk.gkiosk.Global;

/**
 * Singleton holding this kiosk's immutable-ish metadata and runtime flags.
 * Keep it small and in-memory. Persist outside if needed (e.g., SharedPreferences/DB).
 */
public final class KioskInfo {

    // Store basics
    private String storeCode;            // 매장 코드
    private String storeName;            // 매장 이름
    private String storeCompanyNumber;   // 사업자등록번호

    // Kiosk basics
    private String kioskCode;            // 키오스크 코드 (설치 단말 식별)
    private String kioskName;            // 키오스크 표시 이름

    // Devices: Card (PG) Terminal
    private boolean deviceCardEnable;        // 카드 단말 사용 여부
    private String deviceCardCompanyType;    // 카드사/밴사 타입(문자코드)
    private String deviceCardTID;            // 카드 단말 TID

    // Devices: Vending (Serial/USB 등)
    private boolean deviceVenEnable;     // 자판기/외부장치 연동 사용 여부
    private String deviceVenType;        // 연동 타입 (예: MDB, RS232 등 텍스트)
    private String deviceVenPort;        // 포트명 (예: COM3, /dev/ttyUSB0)

    // Devices: Auth / Scanner / Pass
    private boolean deviceAuthEnable;        // 인증장치 사용 여부(예: 직원/관리자 카드)
    private boolean deviceScannerEnable;     // 스캐너 사용 여부
    private String deviceScannerCameraPort;  // 카메라/스캐너 포트 또는 ID
    private boolean devicePassEnable;        // 패스(교통/모바일 등) 결제 사용 여부

    // Optional environment flags kept from previous version
    private String languageTag; // e.g., "ko", "en", or "system"
    private Global.ThemeMode themeMode = Global.ThemeMode.LIGHT;
    private long lastSyncEpochMs;

    // Backend/config
    private String apiBaseUrl;          // REST base URL
    private String wsEndpoint;          // WebSocket endpoint
    private String configVersion;       // remote config version
    private String appVersion;          // app version name/code summary
    private String firmwareVersion;     // device firmware version (if applicable)

    // Operational flags
    private boolean maintenanceMode;    // 유지보수 모드 (판매/결제 비활성화)
    private boolean refundEnable;       // 환불 가능 여부
    private boolean partialCancelEnable;// 부분 취소 가능 여부
    private boolean offlineModeEnable;  // 오프라인 판매 허용

    private KioskInfo() {
    }

    private static class Holder {
        private static final KioskInfo INSTANCE = new KioskInfo();
    }

    public static KioskInfo getInstance() {
        return Holder.INSTANCE;
    }

    // Fluent setters for bootstrap; prefer calling from Application or init flow.
    public KioskInfo setStoreCode(String storeCode) { this.storeCode = storeCode; return this; }
    public KioskInfo setStoreName(String storeName) { this.storeName = storeName; return this; }
    public KioskInfo setStoreCompanyNumber(String storeCompanyNumber) { this.storeCompanyNumber = storeCompanyNumber; return this; }

    public KioskInfo setKioskCode(String kioskCode) { this.kioskCode = kioskCode; return this; }
    public KioskInfo setKioskName(String kioskName) { this.kioskName = kioskName; return this; }

    public KioskInfo setDeviceCardEnable(boolean deviceCardEnable) { this.deviceCardEnable = deviceCardEnable; return this; }
    public KioskInfo setDeviceCardCompanyType(String deviceCardCompanyType) { this.deviceCardCompanyType = deviceCardCompanyType; return this; }
    public KioskInfo setDeviceCardTID(String deviceCardTID) { this.deviceCardTID = deviceCardTID; return this; }

    public KioskInfo setDeviceVenEnable(boolean deviceVenEnable) { this.deviceVenEnable = deviceVenEnable; return this; }
    public KioskInfo setDeviceVenType(String deviceVenType) { this.deviceVenType = deviceVenType; return this; }
    public KioskInfo setDeviceVenPort(String deviceVenPort) { this.deviceVenPort = deviceVenPort; return this; }

    public KioskInfo setDeviceAuthEnable(boolean deviceAuthEnable) { this.deviceAuthEnable = deviceAuthEnable; return this; }
    public KioskInfo setDeviceScannerEnable(boolean deviceScannerEnable) { this.deviceScannerEnable = deviceScannerEnable; return this; }
    public KioskInfo setDeviceScannerCameraPort(String deviceScannerCameraPort) { this.deviceScannerCameraPort = deviceScannerCameraPort; return this; }
    public KioskInfo setDevicePassEnable(boolean devicePassEnable) { this.devicePassEnable = devicePassEnable; return this; }

    public KioskInfo setLanguageTag(String languageTag) {
        this.languageTag = languageTag;
        return this;
    }

    public KioskInfo setThemeMode(Global.ThemeMode themeMode) {
        this.themeMode = (themeMode == null) ? Global.ThemeMode.LIGHT : themeMode;
        return this;
    }

    public KioskInfo setLastSyncEpochMs(long lastSyncEpochMs) {
        this.lastSyncEpochMs = lastSyncEpochMs;
        return this;
    }

    public KioskInfo setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; return this; }
    public KioskInfo setWsEndpoint(String wsEndpoint) { this.wsEndpoint = wsEndpoint; return this; }
    public KioskInfo setConfigVersion(String configVersion) { this.configVersion = configVersion; return this; }
    public KioskInfo setAppVersion(String appVersion) { this.appVersion = appVersion; return this; }
    public KioskInfo setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; return this; }
    public KioskInfo setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; return this; }
    public KioskInfo setRefundEnable(boolean refundEnable) { this.refundEnable = refundEnable; return this; }
    public KioskInfo setPartialCancelEnable(boolean partialCancelEnable) { this.partialCancelEnable = partialCancelEnable; return this; }
    public KioskInfo setOfflineModeEnable(boolean offlineModeEnable) { this.offlineModeEnable = offlineModeEnable; return this; }

    // Getters
    public String getStoreCode() { return storeCode; }
    public String getStoreName() { return storeName; }
    public String getStoreCompanyNumber() { return storeCompanyNumber; }

    public String getKioskCode() { return kioskCode; }
    public String getKioskName() { return kioskName; }

    public boolean isDeviceCardEnable() { return deviceCardEnable; }
    public String getDeviceCardCompanyType() { return deviceCardCompanyType; }
    public String getDeviceCardTID() { return deviceCardTID; }

    public boolean isDeviceVenEnable() { return deviceVenEnable; }
    public String getDeviceVenType() { return deviceVenType; }
    public String getDeviceVenPort() { return deviceVenPort; }

    public boolean isDeviceAuthEnable() { return deviceAuthEnable; }
    public boolean isDeviceScannerEnable() { return deviceScannerEnable; }
    public String getDeviceScannerCameraPort() { return deviceScannerCameraPort; }
    public boolean isDevicePassEnable() { return devicePassEnable; }

    public String getLanguageTag() {
        return languageTag;
    }

    public Global.ThemeMode getThemeMode() {
        return themeMode;
    }

    public long getLastSyncEpochMs() {
        return lastSyncEpochMs;
    }

    public String getApiBaseUrl() { return apiBaseUrl; }
    public String getWsEndpoint() { return wsEndpoint; }
    public String getConfigVersion() { return configVersion; }
    public String getAppVersion() { return appVersion; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public boolean isMaintenanceMode() { return maintenanceMode; }
    public boolean isRefundEnable() { return refundEnable; }
    public boolean isPartialCancelEnable() { return partialCancelEnable; }
    public boolean isOfflineModeEnable() { return offlineModeEnable; }
}
