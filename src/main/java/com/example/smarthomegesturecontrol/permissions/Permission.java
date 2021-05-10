package com.example.smarthomegesturecontrol.permissions;

public final class Permission {

    /**
     * 8.0 and above application installation permissions
     */
    public static final String REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES";

    /**
     * 6.0 and above hover window permissions
     */
    public static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";

    /**
     * Read calendar reminders
     */
    public static final String READ_CALENDAR = "android.permission.READ_CALENDAR";
    /**
     * Write calendar reminders
     */
    public static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";

    /**
     * Photo permissions
     */
    public static final String CAMERA = "android.permission.CAMERA";

    /**
     * Read the contact
     */
    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";

    /**
     * Write contact
     */
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";

    /**
     * Access Account List
     */
    public static final String GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";

    /**
     * The recording permissions
     */
    public static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";

    /**
     * Read the phone state
     */
    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";

    /**
     * Make a phone call
     */
    public static final String CALL_PHONE = "android.permission.CALL_PHONE";

    /**
     * Read the call log
     */
    public static final String READ_CALL_LOG = "android.permission.READ_CALL_LOG";

    /**
     * Write a call log
     */
    public static final String WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";

    /**
     * Add voice mail
     */
    public static final String ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL";

    /**
     * Using SIP Video
     */
    public static final String USE_SIP = "android.permission.USE_SIP";

    /**
     * Handle outgoing calls
     */
    public static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";

    /**
     * 8.0 Dangerous permission: Allows your application to receive incoming calls programmatically. To handle incoming calls in your application, you can use the AcceptTringCall () function
     */
    public static final String ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS";

    /**
     * 8.0 Dangerous Permissions: Permissions allow your application to read phone numbers stored on the device
     */
    public static final String READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS";

    /**
     * The sensor
     */
    public static final String BODY_SENSORS = "android.permission.BODY_SENSORS";

    /**
     * Send a text message
     */
    public static final String SEND_SMS = "android.permission.SEND_SMS";

    /**
     * Receive SMS
     */
    public static final String RECEIVE_SMS = "android.permission.RECEIVE_SMS";

    /**
     * Read the message
     */
    public static final String READ_SMS = "android.permission.READ_SMS";

    /**
     * Receive WAP PUSH information
     */
    public static final String RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH";

    /**
     * Receive MMS
     */
    public static final String RECEIVE_MMS = "android.permission.RECEIVE_MMS";

    /**
     * Read external storage
     */
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    /**
     * Write to external storage
     */
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static final class Group {
        /**
         * The calendar
         */
        public static final String[] CALENDAR = new String[]{
                Permission.READ_CALENDAR,
                Permission.WRITE_CALENDAR};

        /**
         * The contact
         */
        public static final String[] CONTACTS = new String[]{
                Permission.READ_CONTACTS,
                Permission.WRITE_CONTACTS,
                Permission.GET_ACCOUNTS};

        /**
         * storage
         */
        public static final String[] STORAGE = new String[]{
                Permission.READ_EXTERNAL_STORAGE,
                Permission.WRITE_EXTERNAL_STORAGE};
    }
}