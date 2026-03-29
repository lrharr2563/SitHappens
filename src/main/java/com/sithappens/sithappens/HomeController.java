package com.sithappens.sithappens;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sithappens.sithappens.model.Booking;
import com.sithappens.sithappens.model.Pet;
import com.sithappens.sithappens.model.User;
import com.sithappens.sithappens.repository.AvailabilityRepository;
import com.sithappens.sithappens.repository.BookingRepository;
import com.sithappens.sithappens.repository.PetRepository;
import com.sithappens.sithappens.repository.ReviewRepository;
import com.sithappens.sithappens.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private PetRepository petRepository;

    // 🏠 Homepage
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "index";
    }

    // Login and Register

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String role) {

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setRole(role);

        userRepository.save(user);

        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email,
                           @RequestParam String password,
                           Model model,
                           HttpSession session) {

        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getEmail().equals(email) &&
                user.getPasswordHash().equals(password) &&
                user.isActive()) {

                session.setAttribute("loggedInUser", user);
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("error", "Invalid login");
        return "login";
    }

    // Dashboard

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        List<Booking> bookings = bookingRepository.findAll();
        List<Booking> userBookings = new ArrayList<>();

        for (Booking booking : bookings) {

            if (user.getRole().equals("OWNER")) {
                if (booking.getOwner() != null &&
                    booking.getOwner().getId().equals(user.getId())) {
                    userBookings.add(booking);
                }
            }

            if (user.getRole().equals("SITTER")) {
                if (booking.getSitter() != null &&
                    booking.getSitter().getId().equals(user.getId())) {
                    userBookings.add(booking);
                }
            }
        }

        model.addAttribute("bookings", userBookings);

        // 🔥 PETS (NEW)
        List<Pet> pets = petRepository.findAll();
        model.addAttribute("pets", pets);

        if (user.getRole().equals("OWNER")) {
            return "owner-dashboard";
        } else {
            return "sitter-dashboard";
        }
    }

    // Pet feature

    @GetMapping("/add-pet")
    public String showAddPetPage() {
        return "add-pet";
    }

    @PostMapping("/save-pet")
public String savePet(@RequestParam String name,
                     @RequestParam String type,
                     @RequestParam(required = false) Integer age,
                     @RequestParam(required = false) String breed,
                     @RequestParam(required = false) String notes,
                     HttpSession session) {

    User user = (User) session.getAttribute("loggedInUser");

    if (user == null) {
        return "redirect:/login";
    }

    Pet pet = new Pet();
    pet.setName(name);
    pet.setType(type);
    pet.setAge(age);
    pet.setBreed(breed);
    pet.setNotes(notes);
    pet.setOwner(user);

    petRepository.save(pet);

    return "redirect:/dashboard";
}

    // existing features

    @GetMapping("/sitters")
    public String viewSitters(Model model) {

        List<User> allUsers = userRepository.findAll();
        List<User> sitters = new ArrayList<>();

        for (User user : allUsers) {
            if ("SITTER".equals(user.getRole()) && user.isActive()) {
                sitters.add(user);
            }
        }

        model.addAttribute("sitters", sitters);

        return "sitters";
    }

    @PostMapping("/book-sitter/{id}")
    public String bookSitter(@PathVariable Long id, HttpSession session) {

        User owner = (User) session.getAttribute("loggedInUser");
        User sitter = userRepository.findById(id).orElse(null);

        if (owner == null || sitter == null) {
            return "redirect:/login";
        }

        Booking booking = new Booking();
        booking.setOwner(owner);
        booking.setSitter(sitter);
        booking.setStartDate(LocalDate.now());
        booking.setEndDate(LocalDate.now().plusDays(2));
        booking.setStatus("REQUESTED");

        bookingRepository.save(booking);

        return "redirect:/dashboard";
    }

    @GetMapping("/bookings")
    public String getBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "bookings";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    // deactivate user

    @GetMapping("/deactivate-user/{id}")
    public String deactivateUser(@PathVariable Long id, HttpSession session) {

        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
        }

        session.invalidate();

        return "redirect:/";
    }
}


