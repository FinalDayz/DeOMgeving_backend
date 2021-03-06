package org.example.persistence;

import org.example.model.Credentials;
import org.example.model.User;
import org.example.persistence.mapper.UserTypeMapper;
import org.example.util.CryptographicUtils;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper(UserTypeMapper.class)
public interface UserDAO {

    @SqlQuery("SELECT true FROM user WHERE email = :email AND " +
            "password = SHA2( CONCAT(:password , :salt , :secret_key), " + CryptographicUtils.PASSWORD_HASH_LENGTH +")")
    boolean userExistWithCredentials(
            @BindBean Credentials login, @Bind("salt") String salt, @Bind("secret_key") String secret_key);

    @SqlQuery("Select * FROM user WHERE email = :email")
    @Mapper(UserTypeMapper.class)
    User findUserByEmail(@Bind("email") String email);



    @SqlUpdate("INSERT INTO user (email, password, name, salt) VALUES ( :email, SHA2( CONCAT(:password, :salt , :secret_key), "+CryptographicUtils.PASSWORD_HASH_LENGTH+ "), :name, :salt)")
    void create(@BindBean Credentials userCredentials,
                @Bind("salt") String salt, @Bind("secret_key") String secret_key);





    @SqlUpdate("UPDATE user SET password = :password WHERE token = :token")
    void updatePassword(@Bind("password") String password, @Bind("token") String token);



    @SqlUpdate("UPDATE user SET email = :email, name = :name, role = :role WHERE id = :id")
    int updateUser(@BindBean User user);



    @SqlQuery("SELECT id, name, email FROM user WHERE role = :role")
    @Mapper(UserTypeMapper.class)
    List<User> getUsersByRole(@Bind("role") String role);

    @SqlQuery("SELECT id, name, email, role FROM user ORDER BY role DESC")
    @Mapper(UserTypeMapper.class)
    List<User> getAllUsers();

    @SqlQuery("SELECT salt FROM user where email = :email")
    String getUserSalt(@Bind("email") String email);

    @SqlUpdate("DELETE FROM user WHERE id = :id")
    int removeUser(@Bind("id") int id);

    void close();
}
