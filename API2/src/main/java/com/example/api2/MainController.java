package com.example.api2;

import com.sun.tools.javac.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller // This means that this class is a Controller
@RequestMapping(path="/demo") // This means URL's start with /demo (after Application path)
public class MainController {
    // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepo userRepository;

    @Autowired
    private UserService userService;

    public MainController() {

    }
    @Autowired
    public MainController(UserService userService, UserRepo userRepository) {

        this.userRepository = userRepository;
        this.userService = userService;
    }


@PostMapping("/addusers")
public ResponseEntity<String> addUser(@RequestBody User user) {
    User savedUser = userService.saveUser(user);
    if (savedUser != null) {
        return ResponseEntity.status(HttpStatus.OK).body("{\"message\": \"User added successfully\"}");
    }
     else {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"Failed to add user\"}");
    }
}


    @GetMapping(path="/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        // This returns a JSON or XML with the users
        return userRepository.findAll();
    }

    @GetMapping(path="/delete")
    public ResponseEntity<String> deleteUsers(@RequestParam("id")  long id) {
        // This returns a JSON or XML with the users
        if (!userService.isUserExists(id)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No user");
        }
        userService.deleteUser(id);
        if (userService.isUserExists(id)){
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Failed to remove user");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Removed");
    }

    @GetMapping(path="/alll")
    public ResponseEntity<String> ss() {
        return ResponseEntity.ok("Hello");
    }
}