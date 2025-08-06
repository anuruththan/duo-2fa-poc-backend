--Create User Role  Table
CREATE TABLE IF NOT EXISTS "UserRole"
(
    "UserRoleId"   SERIAL PRIMARY KEY,
    "UserRoleName" CHARACTER VARYING
);

CREATE EXTENSION IF NOT EXISTS pgcrypto;

--Create User Details Table
CREATE TABLE IF NOT EXISTS "UserDetails"
(
    "UserDetailId"   SERIAL PRIMARY KEY,
    "FirstName"      CHARACTER VARYING,
    "LastName"       CHARACTER VARYING,
    "UserRoleId"     INT               NOT NULL,
    "E-mail"         CHARACTER VARYING NOT NULL UNIQUE,
    "MobileNumber"   CHARACTER VARYING NOT NULL,
    "Password"       CHARACTER VARYING NOT NULL,
    FOREIGN KEY ("UserRoleId") REFERENCES "UserRole" ("UserRoleId")
    );

--To insert the User Role
CREATE OR REPLACE FUNCTION insert_user_role(
    IN pUserRoleName CHARACTER VARYING,
    OUT "rDataUpdated" CHARACTER VARYING
)
       LANGUAGE plpgsql
AS
'
    DECLARE
        lExists BOOLEAN;
    BEGIN
        IF pUserRoleName IS NULL THEN
             "rDataUpdated" :=''Invalid input: UserRoleName is null'';
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



--Insert UserDetails
CREATE OR REPLACE FUNCTION insert_user_details(
    IN pFirstName CHARACTER VARYING,
    IN pLastName CHARACTER VARYING,
    IN pUserRoleId INT,
    IN pEmail CHARACTER VARYING,
    IN pMobileNumber CHARACTER VARYING,
    IN pPassword CHARACTER VARYING,
    OUT "rDataUpdated" CHARACTER VARYING
)
         LANGUAGE plpgsql
AS
'
    DECLARE
        lExists BOOLEAN;
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
        VALUES (pFirstName, pLastName, pUserRoleId, pEmail, pMobileNumber,
                ENCODE(DIGEST(pPassword, ''sha256''), ''hex''));

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
    IN pEmail CHARACTER VARYING DEFAULT NULL,
    IN pMobileNumber CHARACTER VARYING DEFAULT NULL,
    IN pFirstName CHARACTER VARYING DEFAULT NULL,
    IN pLastName CHARACTER VARYING DEFAULT NULL,
    IN pUserRoleId INT DEFAULT NULL,
    IN pLocationId INT DEFAULT NULL,
    IN pPassword CHARACTER VARYING DEFAULT NULL,
    OUT "rDataUpdated" CHARACTER VARYING
)
            LANGUAGE plpgsql
AS
'
    DECLARE
        lUserId INT;
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
        SET "FirstName"      = COALESCE(pFirstName, "FirstName"),
            "LastName"       = COALESCE(pLastName, "LastName"),
            "UserRoleId"     = COALESCE(pUserRoleId, "UserRoleId"),
            "E-mail"         = COALESCE(pemail, "E-mail"),
            "MobileNumber"   = COALESCE(pmobilenumber, "MobileNumber"),
            "UserLocationId" =COALESCE(pLocationId, "UserLocationId"),
            "Password"       =COALESCE(ENCODE(DIGEST(pPassword, ''sha256''), ''hex''), "Password")
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
    pEmailID CHARACTER VARYING
)
    RETURNS TABLE
            (
                lUserId      INT,
                lFullName     CHARACTER VARYING,
                lMobileNumber CHARACTER VARYING,
                lUserRole     CHARACTER VARYING,
                lUserRoleId   INT,
                lpassword CHARACTER VARYING,
                lemail CHARACTER VARYING
            )
    LANGUAGE plpgsql
AS
'
    BEGIN
        RETURN QUERY
            SELECT ud."UserDetailId",
                   (ud."FirstName" || '' '' || ud."LastName")::CHARACTER VARYING AS lFullName,
                   ud."MobileNumber",
                   ur."UserRoleName",
                   ud."UserRoleId"::INT,
                   ud."Password", ud."E-mail"
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
    pUserEmail CHARACTER VARYING DEFAULT NOT NULL,
    pOldPassword CHARACTER VARYING DEFAULT NOT NULL,
    pNewPassword CHARACTER VARYING DEFAULT NOT NULL
)
    RETURNS TABLE
            (
                "rPasswordHaveChanged" CHARACTER VARYING
            )
    LANGUAGE plpgsql
AS
'
    DECLARE
        Email    CHARACTER VARYING := null;
        Password CHARACTER VARYING := null;
    BEGIN

        SELECT lpassword,
               lemail
        into Password, Email
        FROM get_user_details_by_email_id(pUserEmail);

        IF Email != puseremail OR Email IS NULL THEN
            RAISE EXCEPTION ''Invalid Email-ID'';
        END IF;

        IF Password = ENCODE(DIGEST(pNewPassword, ''sha256''), ''hex'') THEN
            RAISE EXCEPTION ''Old password and New password are same.'';
        END IF;

        IF Password != ENCODE(DIGEST(pOldPassword, ''sha256''), ''hex'') THEN
            RAISE EXCEPTION ''Current password is Miss matching.'';
        END IF;

        UPDATE "UserDetails"
        SET "Password"=ENCODE(DIGEST(pNewPassword, ''sha256''), ''hex'')
        WHERE "E-mail" = pUserEmail;

        "rPasswordHaveChanged" := ''Password has been updated'';

        RETURN QUERY SELECT "rPasswordHaveChanged";

    END;
';


-- CREATE IMPORTANT ROLES AND SUPPER USER FOR ACCESSING THE APPLICATION
SELECT FROM insert_user_role('Supper-admin');
SELECT FROM insert_user_role('Series-manager');
SELECT FROM insert_user_role('Scorer');
SELECT FROM insert_user_role('General-User');
SELECT FROM insert_user_details('Winter', 'Dass', 1, 'winterdass@gmail.com', '0779613315', '11223344');