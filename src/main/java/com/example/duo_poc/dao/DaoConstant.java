package com.example.duo_poc.dao;

public class DaoConstant {

    public final static String GET_USER_AUTH_BY_EMAIL = "{CALL get_user_details_by_email_id(?)}";

    public final static String INSERT_USER = "{CALL insert_user_details( ?, ?, ?, ?, ?, ?)}";

    public final static String UPDATE_USER = "{CALL update_user_details( ?, ?, ?, ?, ?, ?, ?)}";

    public final static String CHANGE_PASSWORD = "{CALL change_password( ?, ?, ?)}";

    public final static String VERIFY_USER = "{CALL verify_user(?)}";

    public final static String UNVERIFY_USER = "{CALL unverify_user(?)}";

    public final static String IS_VERIFIED_USER = "{CALL is_user_verified(?)}";

    public final static String GET_SECRET_KEY = "{CALL get_secret_key(?)}";

    public final static String INSERT_SECRET_KEY = "{CALL insert_secret_key(?, ?)}";

}