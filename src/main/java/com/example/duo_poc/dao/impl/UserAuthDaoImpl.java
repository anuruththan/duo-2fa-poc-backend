package com.example.duo_poc.dao.impl;

import com.example.duo_poc.dto.request.user.InsertUserDto;
import com.example.duo_poc.dto.request.user.PasswordChangeDto;
import com.example.duo_poc.dto.response.GeneralResponse;
import com.example.duo_poc.dto.response.user.UserAuthResponseDto;
import com.example.duo_poc.dto.response.user.CreateUserResponseDto;
import com.example.duo_poc.dao.DaoConstant;
import com.example.duo_poc.dao.UserAuthDao;
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

        GeneralResponse generalResponse = new GeneralResponse();
        CreateUserResponseDto createUserResponseDto = new CreateUserResponseDto();
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource())); CallableStatement callableStatement = connection.prepareCall(DaoConstant.INSERT_USER)) {

            callableStatement.setObject(1, insertUserDto.getFirstName(), Types.VARCHAR);
            callableStatement.setObject(2, insertUserDto.getLastName(), Types.VARCHAR);
            callableStatement.setInt(3, insertUserDto.getUserRoleId());
            callableStatement.setObject(4, insertUserDto.getEmail(), Types.VARCHAR);
            callableStatement.setObject(5, insertUserDto.getPhoneNumber(), Types.VARCHAR);
            callableStatement.setObject(6,insertUserDto.getPassword(), Types.VARCHAR);

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
    public UserAuthResponseDto findByEmail(String email){
        UserAuthResponseDto userAuthResponseDto = null;

        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.GET_USER_AUTH_BY_EMAIL)) {

            callableStatement.setString(1, email);

            ResultSet resultSet = callableStatement.executeQuery();
            userAuthResponseDto = new UserAuthResponseDto();
            if (resultSet.next()) {
                userAuthResponseDto.setEmail(resultSet.getString("lemail"));
                userAuthResponseDto.setPassword(resultSet.getString("lpassword"));
                userAuthResponseDto.setRoleId(resultSet.getInt("luserroleid"));
                userAuthResponseDto.setFullName(resultSet.getString("lfullname"));
                userAuthResponseDto.setUserRoleName(resultSet.getString("luserrole"));
                userAuthResponseDto.setMobileNumber(resultSet.getString("lmobilenumber"));
                userAuthResponseDto.setUserId(resultSet.getInt("luserid"));
                log.info(userAuthResponseDto.getEmail());

            }
            log.info("This is the email: {}",userAuthResponseDto.getEmail());

        }

        catch (SQLException e) {
            log.error("The is an issue occur while getting the user details by email id: {}",e.getMessage());
        }

        return userAuthResponseDto;
    }

    @Override
    public GeneralResponse changePassword(PasswordChangeDto passwordChangeDto){
        GeneralResponse generalResponse = new GeneralResponse();
        try (Connection connection = DataSourceUtils.getConnection(Objects.requireNonNull(jdbcTemplate.getDataSource()));
             CallableStatement callableStatement = connection.prepareCall(DaoConstant.CHANGE_PASSWORD)) {
            callableStatement.setObject(1, passwordChangeDto.getEmailId(), Types.VARCHAR);
            callableStatement.setObject(2, passwordChangeDto.getOldPassword(), Types.VARCHAR);
            callableStatement.setObject(3, passwordChangeDto.getNewPassword(), Types.VARCHAR);

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