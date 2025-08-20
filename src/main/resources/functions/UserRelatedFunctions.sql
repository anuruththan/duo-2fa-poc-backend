--Create User Role  Table
CREATE TABLE IF NOT EXISTS "UserRole"
(
    "UserRoleId"   SERIAL PRIMARY KEY,
    "UserRoleName" VARCHAR
);

--Create User Details Table
CREATE TABLE IF NOT EXISTS "UserDetails"
(
    "UserDetailId"      SERIAL PRIMARY KEY,
    "FirstName"         VARCHAR,
    "LastName"          VARCHAR,
    "UserRoleId"        INT     NOT NULL,
    "E-mail"            VARCHAR NOT NULL UNIQUE,
    "MobileNumber"      VARCHAR NOT NULL,
    "Password"          VARCHAR NOT NULL,
    "SecretKey"         VARCHAR,
    "IsUserVerified"    BOOLEAN                     DEFAULT FALSE,
    "PasswordCreatedOn" TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    FOREIGN KEY ("UserRoleId") REFERENCES "UserRole" ("UserRoleId")
);

--Create User History
CREATE TABLE IF NOT EXISTS logging."UserPasswordHistory"
(
    "UserChangeHistoryId" SERIAL PRIMARY KEY,
    "UserDetailId"        INT,
    "Password"            VARCHAR NOT NULL,
    "CreatedOn"           TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY ("UserDetailId") REFERENCES "UserDetails" ("UserDetailId")
);

--To insert the User Role
CREATE OR REPLACE FUNCTION insert_user_role(
    IN pUserRoleName VARCHAR,
    OUT "rDataUpdated" VARCHAR
)
    LANGUAGE plpgsql
AS
'
    DECLARE
        lExists BOOLEAN;
    BEGIN
        IF pUserRoleName IS NULL THEN
            "rDataUpdated" := ''Invalid input: UserRoleName is null'';
            RETURN;
        END IF;

        SELECT EXISTS (SELECT 1
                       FROM "UserRole"
                       WHERE "UserRoleName" = pUserRoleName)
        INTO lExists;

        IF lExists THEN
            "rDataUpdated" := ''User role already exists'';
            RETURN;
        END IF;

        INSERT
            INTO "UserRole"("UserRoleName")
        VALUES (pUserRoleName);

        "rDataUpdated" := ''User Role Successfully Inserted'';
        RETURN;

    EXCEPTION
        WHEN OTHERS THEN
            "rDataUpdated" := ''Issue In Inserting The User Role'';
            RETURN;
    END;
';

drop FUNCTION IF EXISTS verify_user(pEmail VARCHAR);
CREATE OR REPLACE FUNCTION verify_user(IN pEmail VARCHAR)
    RETURNS VOID
    LANGUAGE plpgsql
AS
'
    BEGIN
        UPDATE "UserDetails"
        SET "IsUserVerified" = TRUE
        WHERE "E-mail" = pEmail;
    END;
';

DROP FUNCTION IF EXISTS unverify_user(pEmail VARCHAR);
CREATE OR REPLACE FUNCTION unverify_user(IN pEmail VARCHAR)
    RETURNS VOID
    LANGUAGE plpgsql
AS
'
    BEGIN
        UPDATE "UserDetails"
        SET "IsUserVerified" = FALSE
        WHERE "E-mail" = pEmail;
    END;
';

CREATE OR REPLACE FUNCTION insert_secret_key(IN pEmailId VARCHAR, IN pSecretKey VARCHAR)
    RETURNS VOID
    LANGUAGE plpgsql
AS
'
    BEGIN
        UPDATE "UserDetails"
        SET "SecretKey" = COALESCE(pSecretKey, "SecretKey")
        WHERE "E-mail" = pEmailId;
    END;
';

CREATE OR REPLACE FUNCTION get_secret_key(IN pEmailId VARCHAR, OUT "rSecretKey" VARCHAR)
    LANGUAGE plpgsql
AS
'
    DECLARE
        lIsVerified BOOLEAN DEFAULT FALSE;
    BEGIN
        SELECT "UserDetails"."IsUserVerified"
        INTO lIsVerified
        FROM "UserDetails"
        WHERE "E-mail" = pEmailId;
        IF lIsVerified THEN
            SELECT "SecretKey"
            INTO "rSecretKey"
            FROM "UserDetails"
            WHERE "E-mail" = pEmailId;
            RETURN;
        END IF;
    END;
';

--Insert UserDetails
CREATE OR REPLACE FUNCTION insert_user_details(
    IN pFirstName VARCHAR,
    IN pLastName VARCHAR,
    IN pUserRoleId INT,
    IN pEmail VARCHAR,
    IN pMobileNumber VARCHAR,
    IN pPassword VARCHAR,
    OUT "rDataUpdated" VARCHAR
)
    LANGUAGE plpgsql
AS
'
    DECLARE
        lExists BOOLEAN;
        lUserId INT;
    BEGIN
        IF pFirstName IS NULL OR pUserRoleId IS NULL OR pEmail IS NULL THEN
            "rDataUpdated" := ''Invalid input: Required fields are missing'';
            RETURN;
        END IF;

        SELECT EXISTS (SELECT 1
                       FROM "UserDetails"
                       WHERE "E-mail" = pEmail)
        INTO lExists;

        IF lExists THEN
            "rDataUpdated" := ''User with the given email already exists'';
            RETURN;
        END IF;

        INSERT INTO "UserDetails"("FirstName", "LastName", "UserRoleId", "E-mail", "MobileNumber",
                                  "Password")
        VALUES (pFirstName, pLastName, pUserRoleId, pEmail, pMobileNumber, pPassword);

        SELECT "UserDetailId"
        into lUserId
        FROM "UserDetails"
        WHERE "E-mail" = pEmail;
        INSERT INTO logging."UserPasswordHistory"("UserDetailId", "Password")
        VALUES (lUserId, pPassword);

        "rDataUpdated" := ''User Details Successfully Inserted'';
        RETURN;

    EXCEPTION
        WHEN OTHERS THEN
            "rDataUpdated" := ''Issue In Inserting The User Details'';
            RETURN;
    END;
';

--Update User Details
CREATE OR REPLACE FUNCTION update_user_details(
    IN pEmail VARCHAR DEFAULT NULL,
    IN pMobileNumber VARCHAR DEFAULT NULL,
    IN pFirstName VARCHAR DEFAULT NULL,
    IN pLastName VARCHAR DEFAULT NULL,
    IN pUserRoleId INT DEFAULT NULL,
    IN pLocationId INT DEFAULT NULL,
    IN pPassword VARCHAR DEFAULT NULL,
    OUT "rDataUpdated" VARCHAR
)
    LANGUAGE plpgsql
AS
'
    DECLARE
        lUserId     INT;
        lUserRoleId INT;
    BEGIN
        -- Validate at least one identifier
        IF (pEmail IS NULL OR TRIM(pEmail) = '''') AND (pMobileNumber IS NULL OR TRIM(pMobileNumber) = '''') THEN
            "rDataUpdated" := ''At least one of Email or MobileNumber must be provided'';
            RETURN;
        END IF;

        -- Identify user
        SELECT "UserDetailId"
        INTO lUserId
        FROM "UserDetails"
        WHERE ("E-mail" = pEmail OR "MobileNumber" = pMobileNumber)
        LIMIT 1;

        IF lUserId IS NULL THEN
            "rDataUpdated" := ''User not found with given Email or Mobile Number'';
            RETURN;
        END IF;


        -- Get current user role ID
        SELECT "UserRoleId"
        INTO lUserRoleId
        FROM "UserDetails"
        WHERE "UserDetailId" = lUserId;

        -- Check if the new role is valid
        IF pUserRoleId IS NOT NULL AND pUserRoleId = 1 THEN
            "rDataUpdated" := ''Invalid User Role ID'';
            RETURN;
        END IF;

        -- Prevent updating to restricted roles

        IF pUserRoleId = 1 THEN
            "rDataUpdated" := ''Cannot update user role to Supper admin'';
            RETURN;
        END IF;

        IF pPassword = '''' THEN
            pPassword := NULL;
        END IF;

        -- Perform update only for non-null fields
        UPDATE "UserDetails"
        SET "FirstName"    = COALESCE(pFirstName, "FirstName"),
            "LastName"     = COALESCE(pLastName, "LastName"),
            "UserRoleId"   = COALESCE(pUserRoleId, "UserRoleId"),
            "E-mail"       = COALESCE(pemail, "E-mail"),
            "MobileNumber" = COALESCE(pmobilenumber, "MobileNumber"),
            "Password"     =COALESCE(pPassword, "Password")
        WHERE "UserDetailId" = lUserId;

        "rDataUpdated" := ''User Details Successfully Updated'';
        RETURN;

    EXCEPTION
        WHEN OTHERS THEN
            "rDataUpdated" := ''Issue In Updating The User Details'';
            RETURN;
    END;
';


-- GET User details by email id
CREATE OR REPLACE FUNCTION get_user_details_by_email_id(
    pEmailID VARCHAR
)
    RETURNS TABLE
            (
                lUserId       INT,
                lFullName     VARCHAR,
                lMobileNumber VARCHAR,
                lUserRole     VARCHAR,
                lUserRoleId   INT,
                lpassword     VARCHAR,
                lemail        VARCHAR
            )
    LANGUAGE plpgsql
AS
'
    BEGIN
        RETURN QUERY
            SELECT ud."UserDetailId",
                   (ud."FirstName" || '' '' || ud."LastName")::VARCHAR AS lFullName,
                   ud."MobileNumber",
                   ur."UserRoleName",
                   ud."UserRoleId"::INT,
                   ud."Password",
                   ud."E-mail"
            FROM "UserDetails" ud
                     LEFT JOIN "UserRole" ur ON ur."UserRoleId" = ud."UserRoleId"
            WHERE ud."E-mail" like pEmailID || ''%'';

        IF NOT FOUND THEN
            RAISE EXCEPTION ''User Not Found in the database'';
        END IF;
    END;
';



-- Change Password
CREATE OR REPLACE FUNCTION change_password(
    pUserEmail VARCHAR,
    pOldPassword VARCHAR,
    pNewPassword VARCHAR
)
    RETURNS TABLE
            (
                "rPasswordHaveChanged" VARCHAR
            )
    LANGUAGE plpgsql
AS
'
    DECLARE
        lUserEmail        VARCHAR := null;
        lUserPassword     VARCHAR := null;
        lUserDetailId     INT     := NULL;
        lHistoryPasswords VARCHAR[];
    BEGIN

        SELECT lpassword,
               lemail,
               lUserId
        INTO lUserPassword, lUserEmail, lUserDetailId
        FROM get_user_details_by_email_id(puseremail);

        SELECT ARRAY(SELECT "Password"
                     FROM logging."UserPasswordHistory"
                     WHERE "UserDetailId" = lUserDetailId)
        INTO lHistoryPasswords;

        IF lUserEmail != puseremail OR lUserEmail IS NULL THEN
            RAISE EXCEPTION ''Invalid Email-ID'';
        END IF;
        IF lUserPassword = pnewpassword THEN
            RAISE EXCEPTION ''Old password and New password are same.'';
        END IF;
        IF lUserPassword != poldpassword THEN
            RAISE EXCEPTION ''Current password is Miss matching.'';
        END IF;

        IF pnewpassword = ANY (lHistoryPasswords) THEN
            RAISE EXCEPTION ''Can not use last 5 password.'';
        ELSE
            -- Update password
            UPDATE "UserDetails"
            SET "Password"= pnewpassword,
                "PasswordCreatedOn" = Now()
            WHERE "E-mail" = puseremail;

            INSERT INTO logging."UserPasswordHistory" ("UserDetailId", "Password")
            VALUES (lUserDetailId, pnewpassword);

            -- Delete old passwords, keep last 5
            WITH delete_history AS (SELECT "UserChangeHistoryId"
                                    FROM (SELECT "UserChangeHistoryId",
                                                 ROW_NUMBER() OVER (
                                                     PARTITION BY "UserDetailId"
                                                     ORDER BY "CreatedOn" DESC
                                                     ) AS rn
                                          FROM logging."UserPasswordHistory") deleting_change_history
                                    WHERE deleting_change_history.rn > 5)
            DELETE
            FROM logging."UserPasswordHistory"
            WHERE "UserChangeHistoryId" in (SELECT "UserChangeHistoryId"
                                            FROM delete_history);

            "rPasswordHaveChanged" := ''Password has been updated'';
            RETURN QUERY SELECT "rPasswordHaveChanged";
        END IF;
    END;
';


-- CREATE IMPORTANT ROLES AND SUPPER USER FOR ACCESSING THE APPLICATION
SELECT
FROM insert_user_role('Price');
SELECT
FROM insert_user_role('Soap');
SELECT
FROM insert_user_role('Ghost');
SELECT
FROM insert_user_role('');
