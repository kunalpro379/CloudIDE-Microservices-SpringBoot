package com.example.codeeditorservice.services;
import com.exmample.codeeditorservice.Dto.AuthenticationRequestDTO;
import com.example.codeeditorservice.Dto.RegisterRequestDTO;
import com.example.codeeditorservice.entities.User;
import com.example.codeeditorservice.mapper.AuthenticationDTOUserMapper;
import com.example.codeeditorservice.mapper.UserMapper;
import com.example.codeeditorservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class DocAuthorizationServiceImpl implements DocAuthorizationService DocAuthorizationService{
    @Autowired
    UserRepository userRepository;
    private User getCurrentUser(){
        String username=SecurityUtil.getCurrentUsername();
        /*
        This Authentication object is stored in a thread-local container called the SecurityContext, which is accessible like this:
SecurityContextHolder.getContext().getAuthentication();
*/
         */
        User user=userRepository.findById(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return user;
    }

    @Override
    public boolean canAccess(String docId){
        User user =getCurrentUser();
        return user.getAccessDocs().stream().anyMatch(doc->doc.getDoc().getId().equals(docId));

    }
    @Override
    public boolean canEdit(String username, Doc doc) {
        return doc.getOwner().getUsername().equals(username) || doc.getSharedWith().stream()
                .anyMatch(userDoc -> userDoc.getUser().getUsername().equals(username) && userDoc.getPermission().equals(Permission.EDIT));
    }

    public boolean fullAccess(String username, Doc doc) {
        return doc.getOwner().getUsername().equals(username);
    }

}