package com.app.easypharma_backend.application.auth.mapper;

import com.app.easypharma_backend.application.auth.dto.request.RegisterRequest;
import com.app.easypharma_backend.application.auth.dto.request.UpdateUserRequest;
import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.domain.auth.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-14T10:02:00+0100",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.email( user.getEmail() );
        userResponse.firstName( user.getFirstName() );
        userResponse.lastName( user.getLastName() );
        userResponse.phone( user.getPhone() );
        userResponse.role( user.getRole() );
        userResponse.address( user.getAddress() );
        userResponse.city( user.getCity() );
        userResponse.latitude( user.getLatitude() );
        userResponse.longitude( user.getLongitude() );
        userResponse.isActive( user.getIsActive() );
        userResponse.isVerified( user.getIsVerified() );
        userResponse.createdAt( user.getCreatedAt() );

        return userResponse.build();
    }

    @Override
    public List<UserResponse> toResponseList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserResponse> list = new ArrayList<UserResponse>( users.size() );
        for ( User user : users ) {
            list.add( toResponse( user ) );
        }

        return list;
    }

    @Override
    public User toEntity(RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( request.getEmail() );
        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );
        user.phone( request.getPhone() );
        user.role( request.getRole() );
        user.address( request.getAddress() );
        user.city( request.getCity() );
        user.latitude( request.getLatitude() );
        user.longitude( request.getLongitude() );

        user.isActive( true );
        user.isVerified( false );

        return user.build();
    }

    @Override
    public void updateEntityFromRequest(RegisterRequest request, User user) {
        if ( request == null ) {
            return;
        }

        if ( request.getEmail() != null ) {
            user.setEmail( request.getEmail() );
        }
        if ( request.getFirstName() != null ) {
            user.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            user.setLastName( request.getLastName() );
        }
        if ( request.getPhone() != null ) {
            user.setPhone( request.getPhone() );
        }
        if ( request.getRole() != null ) {
            user.setRole( request.getRole() );
        }
        if ( request.getAddress() != null ) {
            user.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            user.setCity( request.getCity() );
        }
        if ( request.getLatitude() != null ) {
            user.setLatitude( request.getLatitude() );
        }
        if ( request.getLongitude() != null ) {
            user.setLongitude( request.getLongitude() );
        }
    }

    @Override
    public void updateEntityFromRequest(UpdateUserRequest request, User user) {
        if ( request == null ) {
            return;
        }

        if ( request.getEmail() != null ) {
            user.setEmail( request.getEmail() );
        }
        if ( request.getFirstName() != null ) {
            user.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            user.setLastName( request.getLastName() );
        }
        if ( request.getPhone() != null ) {
            user.setPhone( request.getPhone() );
        }
        if ( request.getAddress() != null ) {
            user.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            user.setCity( request.getCity() );
        }
        if ( request.getLatitude() != null ) {
            user.setLatitude( request.getLatitude() );
        }
        if ( request.getLongitude() != null ) {
            user.setLongitude( request.getLongitude() );
        }
    }
}
