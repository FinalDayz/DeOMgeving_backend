package org.example.service;

import org.example.AppConfiguration;
import org.example.model.Credentials;
import org.example.model.LoginCredentials;
import org.example.model.RegisterCredentials;
import org.example.model.User;
import org.example.persistence.DAOFactory;
import org.example.persistence.UserDAO;
import org.example.util.CryptographicUtils;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Optional;

public class AuthService {

    private JsonWebTokenService jwtService;

    private final String passwordHashKey;
    private UserDAO userDAO;

    @Inject
    public AuthService(DAOFactory factory, AppConfiguration config, JsonWebTokenService jwtService) {
        this.jwtService = jwtService;
        this.userDAO = factory.onDemand(UserDAO.class);
        this.passwordHashKey = config.getPasswordHash();
    }

    public Response createUser(RegisterCredentials credentials) {
        Optional<User> optionalUser = getUserByEmail(credentials);

        if (optionalUser.isPresent())
            return Response.status(Response.Status.CONFLICT).build();

        String salt = CryptographicUtils.generateSalt();
        userDAO.create(credentials, salt, passwordHashKey);

        userDAO.close();
        return Response.ok().build();
    }

    public Response onLogin(LoginCredentials credentials) {
        Optional<User> optionalUser = getUserByEmail(credentials);

        if (!optionalUser.isPresent()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        User user = optionalUser.get();

        String salt = user.getSalt();

        if (user.getRole().equalsIgnoreCase("UNIDENTIFIED") || !this.checkUserPassword(credentials, salt)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String token = jwtService.createJwt(user);
        userDAO.close();
        return JWTResponse(token);
    }

    private boolean checkUserPassword(Credentials credentials, String salt) {
        return userDAO.userExistWithCredentials(credentials, salt, passwordHashKey);
    }

    private Optional<User> getUserByEmail(Credentials credentials) {
        User user = userDAO.findUserByEmail(credentials.getEmail());
        return Optional.ofNullable(user);
    }

    private Response JWTResponse(String token) {
        HashMap<String, String> ResponseEntity = new HashMap<>();
        ResponseEntity.put("jwtToken", token);
        return Response.ok().entity(ResponseEntity).build();
    }


    public Response refreshToken(HttpHeaders headers) {
        if (!jwtService.hasJWTHeader(headers)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String oldJWT = headers.getRequestHeader("Authorization").get(0);
        oldJWT = oldJWT.replace("Bearer ", "");

        if (!jwtService.isValidWithLeeway(oldJWT)) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        String newJWT = jwtService.createJWTFromInvalidJWT(oldJWT);

        return JWTResponse(newJWT);
    }

}
