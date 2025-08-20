package com.example.duo_poc.dao.impl;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.response.user.UserVerificationResponseDto;
import com.example.duo_poc.dto.response.user.CreateUserResponseDto;
import com.example.duo_poc.dao.DaoConstant;
import com.example.duo_poc.dao.UserAuthDao;
import com.example.duo_poc.util.PasswordUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Objects;

@Slf4j
@Repository
public class UserAuthDaoImpl implements UserAuthDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public GeneralResponse insertNewUser(InsertUserDto insertUserDto) {
        String password;
        GeneralResponse generalResponse = new GeneralResponse();
        CreateUserResponseDto createUserResponseDto = new CreateUserResponseDto();
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource())); CallableStatement callableStatement = connection.prepareCall(DaoConstant.INSERT_USER)) {

            callableStatement.setObject(1, insertUserDto.getFirstName(), Types.VARCHAR);
            callableStatement.setObject(2, insertUserDto.getLastName(), Types.VARCHAR);
            callableStatement.setInt(3, insertUserDto.getUserRoleId());
            callableStatement.setObject(4, insertUserDto.getEmail(), Types.VARCHAR);
            callableStatement.setObject(5, insertUserDto.getPhoneNumber(), Types.VARCHAR);

            password = PasswordUtils.hashSHA256(insertUserDto.getPassword());  // Password is hashing here with sha256

            callableStatement.setObject(6, password, Types.VARCHAR);

            ResultSet resultSet = callableStatement.executeQuery();

            if (resultSet.next()) {
                createUserResponseDto.setDataInsertedDetails(resultSet.getString("rDataUpdated"));
            }
//            generalResponse.setData(createUserResponseDto.getDataInsertedDetails());
            if (Objects.equals(createUserResponseDto.getDataInsertedDetails(), "User Details Successfully Inserted")) {
                generalResponse.setData(createUserResponseDto.getDataInsertedDetails());
                generalResponse.setMsg("Successfully data inserted");
                generalResponse.setStatusCode(201);
                generalResponse.setRes(true);
            } else {
                generalResponse.setData(null);
                generalResponse.setMsg(createUserResponseDto.getDataInsertedDetails());
                generalResponse.setStatusCode(409);
                generalResponse.setRes(false);
            }

        } catch (SQLException e) {
            log.error("---------------------------------------------{}", e.getMessage());
            generalResponse.setRes(false);
            generalResponse.setData("Input valid data");
            generalResponse.setMsg(e.getMessage());
            generalResponse.setStatusCode(409);
        }
        return generalResponse;

    }

    @Override
    public UserVerificationResponseDto findByEmail(String email){
        UserVerificationResponseDto userVerificationResponseDto = null;

        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.GET_USER_AUTH_BY_EMAIL)) {

            callableStatement.setString(1, email);

            ResultSet resultSet = callableStatement.executeQuery();
            userVerificationResponseDto = new UserVerificationResponseDto();
            if (resultSet.next()) {
                userVerificationResponseDto.setEmail(resultSet.getString("lemail"));
                userVerificationResponseDto.setPassword(resultSet.getString("lpassword"));
                userVerificationResponseDto.setRoleId(resultSet.getInt("luserroleid"));
                userVerificationResponseDto.setFullName(resultSet.getString("lfullname"));
                userVerificationResponseDto.setUserRoleName(resultSet.getString("luserrole"));
                userVerificationResponseDto.setMobileNumber(resultSet.getString("lmobilenumber"));
                userVerificationResponseDto.setUserId(resultSet.getInt("luserid"));
                log.info(userVerificationResponseDto.getEmail());

            }
            log.info("This is the email: {}", userVerificationResponseDto.getEmail());

        }

        catch (SQLException e) {
            log.error("The is an issue occur while getting the user details by email id: {}",e.getMessage());
        }

        return userVerificationResponseDto;
    }

    @Override
    public void verifyUser(String email){
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.VERIFY_USER)) {
            callableStatement.setString(1,email);
            callableStatement.executeQuery();
        }
        catch (SQLException e){
            log.error("verifying the user error:{}",e.getMessage());
        }
    }

    @Override
    public void unverifyUser(String email){
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.UNVERIFY_USER)) {
            callableStatement.setString(1,email);
            callableStatement.executeQuery();
        }
        catch (SQLException e){
            log.error("Unverifying the user error:{}",e.getMessage());
        }
    }

    @Override
    public boolean isUserVerified(String email){
        boolean userVerified = false;
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.IS_VERIFIED_USER)) {
            callableStatement.setString(1,email);

            ResultSet resultSet = callableStatement.executeQuery();

            if (resultSet.next()) {
                userVerified = resultSet.getBoolean(1);
            }

        }
        catch (SQLException e){
            log.error("Unable to get user verification: {}",e.getMessage());
        }

        return userVerified;
    }

    @Override
    public String getSecretKey(String email){

        String secretKey = null;
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.GET_SECRET_KEY)) {
            callableStatement.setString(1,email);
            ResultSet resultSet = callableStatement.executeQuery();
            if (resultSet.next()) {
                secretKey = resultSet.getString("rSecretKey");
            }
        }
        catch (SQLException e){
            log.error("Error occur in getting the secret key of the user:{}",e.getMessage());
        }

        return secretKey;
    }

    @Override
    public void insertSecretKey(String email, String secretKey){
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.INSERT_SECRET_KEY)) {
            callableStatement.setString(1, email);
            callableStatement.setString(2, secretKey);
            callableStatement.executeQuery();
        }
        catch (SQLException e){
            log.error("Error occur while inserting the secret key for the user: {}",e.getMessage());
        }
    }

    @Override
    public GeneralResponse changePassword(PasswordChangeDto passwordChangeDto){
        GeneralResponse generalResponse = new GeneralResponse();
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.CHANGE_PASSWORD)) {

            callableStatement.setObject(1, passwordChangeDto.getEmailId(), Types.VARCHAR);
            callableStatement.setObject(2, PasswordUtils.hashSHA256(passwordChangeDto.getOldPassword()), Types.VARCHAR);
            callableStatement.setObject(3, PasswordUtils.hashSHA256(passwordChangeDto.getNewPassword()), Types.VARCHAR);

            ResultSet resultSet = callableStatement.executeQuery();

            if (resultSet.next()) {
                generalResponse.setData(resultSet.getString(1));
                generalResponse.setMsg("Successfully changed password");
                generalResponse.setStatusCode(200);
                generalResponse.setRes(true);
            }

            else {
                generalResponse.setData(null);
                generalResponse.setMsg("Error in inserting the sales Details");

            }
        }
        catch (SQLException e) {
            log.error("---------------------------------------------{}", e.getMessage());
            generalResponse.setRes(false);
            generalResponse.setData("Input valid data");
            generalResponse.setMsg(e.getMessage());
            generalResponse.setStatusCode(409);
        }

        return generalResponse;

    }

}