package com.sithappens.sithappens;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sithappens.sithappens.model.Booking;
import com.sithappens.sithappens.model.Pet;
import com.sithappens.sithappens.model.Review;
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

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        user.setActive(true);

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
            if (user.getEmail().equals(email)
                    && encoder.matches(password, user.getPasswordHash())
                    && user.isActive()) {

                session.setAttribute("loggedInUser", user);
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("error", "Invalid login");
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String password,
                                Model model) {

        User user = userRepository.findByEmail(email);

        if (user != null) {
            user.setPasswordHash(encoder.encode(password));
            userRepository.save(user);
            return "redirect:/login";
        }

        model.addAttribute("error", "Email not found");
        return "forgot-password";
    }

    // Dashboard

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        List<Booking> allBookings = bookingRepository.findAll();
        List<Booking> userBookings = new ArrayList<>();

        for (Booking booking : allBookings) {

            if ("OWNER".equals(user.getRole())) {
                if (booking.getOwner() != null
                        && booking.getOwner().getId().equals(user.getId())) {
                    userBookings.add(booking);
                }
            }

            if ("SITTER".equals(user.getRole())) {
                if (booking.getSitter() != null
                        && booking.getSitter().getId().equals(user.getId())) {
                    userBookings.add(booking);
                }
            }
        }

        model.addAttribute("bookings", userBookings);

        List<Long> reviewedBookingIds = new ArrayList<>();

        for (Booking booking : userBookings) {
            if (booking.getOwner() != null && booking.getSitter() != null) {

                User currentUser = user;
                User otherUser;

                if ("OWNER".equals(user.getRole())) {
                    otherUser = booking.getSitter();
                } else {
                    otherUser = booking.getOwner();
                }

                Review existingReview = reviewRepository.findByBookingIdAndReviewerIdAndRevieweeId(
                        booking.getId(),
                        currentUser.getId(),
                        otherUser.getId()
                );

                if (existingReview != null) {
                    reviewedBookingIds.add(booking.getId());
                }
            }
        }

    model.addAttribute("reviewedBookingIds", reviewedBookingIds);

        List<Pet> allPets = petRepository.findAll();
        List<Pet> userPets = new ArrayList<>();

        for (Pet pet : allPets) {
            if (pet.getOwner() != null
                    && pet.getOwner().getId().equals(user.getId())) {
                userPets.add(pet);
            }
        }

        model.addAttribute("pets", userPets);

        if ("OWNER".equals(user.getRole())) {
            return "owner-dashboard";
        } else {
            return "sitter-dashboard";
        }
    }

    // Pet feature

    @GetMapping("/add-pet")
    public String showAddPetPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

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

    // Sitters list

    @GetMapping("/sitters")
    public String viewSitters(Model model, HttpSession session) {

        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

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

    // New real booking request flow

    @GetMapping("/request-booking/{id}")
    public String showBookingRequestForm(@PathVariable Long id,
                                         HttpSession session,
                                         Model model) {

        User owner = (User) session.getAttribute("loggedInUser");

        if (owner == null) {
            return "redirect:/login";
        }

        User sitter = userRepository.findById(id).orElse(null);

        if (sitter == null) {
            return "error";
        }

        List<Pet> allPets = petRepository.findAll();
        List<Pet> ownerPets = new ArrayList<>();

        for (Pet pet : allPets) {
            if (pet.getOwner() != null
                    && pet.getOwner().getId().equals(owner.getId())) {
                ownerPets.add(pet);
            }
        }

        model.addAttribute("sitter", sitter);
        model.addAttribute("pets", ownerPets);

        return "request-booking";
    }

    @PostMapping("/request-booking")
    public String requestBooking(@RequestParam Long sitterId,
                                 @RequestParam Long petId,
                                 @RequestParam String serviceType,
                                 @RequestParam String startDate,
                                 @RequestParam String endDate,
                                 @RequestParam String requestMessage,
                                 HttpSession session) {

        User owner = (User) session.getAttribute("loggedInUser");

        if (owner == null) {
            return "redirect:/login";
        }

        User sitter = userRepository.findById(sitterId).orElse(null);
        Pet pet = petRepository.findById(petId).orElse(null);

        if (sitter == null || pet == null) {
            return "error";
        }

        Booking booking = new Booking();
        booking.setOwner(owner);
        booking.setSitter(sitter);
        booking.setPet(pet);
        booking.setServiceType(serviceType);
        booking.setStartDate(LocalDate.parse(startDate));
        booking.setEndDate(LocalDate.parse(endDate));
        booking.setRequestMessage(requestMessage);
        booking.setStatus("REQUESTED");

        bookingRepository.save(booking);

        return "redirect:/dashboard";
    }

    // Bookings page filtered to logged-in user

    @GetMapping("/bookings")
    public String getBookings(Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        List<Booking> allBookings = bookingRepository.findAll();
        List<Booking> userBookings = new ArrayList<>();

        for (Booking booking : allBookings) {

            if ("OWNER".equals(user.getRole())) {
                if (booking.getOwner() != null
                        && booking.getOwner().getId().equals(user.getId())) {
                    userBookings.add(booking);
                }
            }

            if ("SITTER".equals(user.getRole())) {
                if (booking.getSitter() != null
                        && booking.getSitter().getId().equals(user.getId())) {
                    userBookings.add(booking);
                }
            }
        }

        model.addAttribute("bookings", userBookings);
        return "bookings";
    }

    @GetMapping("/accept-booking/{id}")
    public String acceptBooking(@PathVariable Long id, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return "error";
        }

        if (booking.getSitter() == null
                || !booking.getSitter().getId().equals(user.getId())) {
            return "error";
        }

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        return "redirect:/dashboard";
    }

    @PostMapping("/decline-booking")
    public String declineBooking(@RequestParam Long bookingId,
                                 @RequestParam String message,
                                 HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);

        if (booking == null) {
            return "error";
        }

        if (booking.getSitter() == null
                || !booking.getSitter().getId().equals(user.getId())) {
            return "error";
        }

        booking.setStatus("DECLINED");
        booking.setDeclineMessage(message);

        bookingRepository.save(booking);

        return "redirect:/dashboard";
    }

    @GetMapping("/complete-booking/{id}")
    public String completeBooking(@PathVariable Long id, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return "error";
        }

        // only sitter can mark completed
        if (booking.getSitter() == null || !booking.getSitter().getId().equals(user.getId())) {
            return "error";
        }

        // only confirmed bookings can become completed
        if (!"CONFIRMED".equals(booking.getStatus())) {
            return "error";
        }

        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);

        return "redirect:/dashboard";
    }

    @GetMapping("/leave-review/{bookingId}/{revieweeId}")
    public String showReviewForm(@PathVariable Long bookingId,
                                @PathVariable Long revieweeId,
                                HttpSession session,
                                Model model) {

        User reviewer = (User) session.getAttribute("loggedInUser");

        if (reviewer == null) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        User reviewee = userRepository.findById(revieweeId).orElse(null);

        if (booking == null || reviewee == null) {
            return "error";
        }

        if (!"COMPLETED".equals(booking.getStatus())) {
            return "error";
        }

        // reviewer must be part of this booking
        boolean isOwner = booking.getOwner() != null && booking.getOwner().getId().equals(reviewer.getId());
        boolean isSitter = booking.getSitter() != null && booking.getSitter().getId().equals(reviewer.getId());

        if (!isOwner && !isSitter) {
            return "error";
        }

        // prevent duplicate review
        Review existing = reviewRepository.findByBookingIdAndReviewerIdAndRevieweeId(
                bookingId, reviewer.getId(), revieweeId
        );

        if (existing != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("reviewee", reviewee);

        return "review-form";
    }

    @PostMapping("/submit-review")
    public String submitReview(@RequestParam Long bookingId,
                            @RequestParam Long revieweeId,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            HttpSession session,
                            Model model) {

        User reviewer = (User) session.getAttribute("loggedInUser");

        if (reviewer == null) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        User reviewee = userRepository.findById(revieweeId).orElse(null);

        if (booking == null || reviewee == null) {
            model.addAttribute("error", "Booking or review user not found.");
            return "error";
        }

        if (!"COMPLETED".equals(booking.getStatus())) {
            model.addAttribute("error", "Reviews can only be submitted after a completed booking.");
            return "error";
        }

        if (rating < 1 || rating > 5) {
            model.addAttribute("error", "Rating must be between 1 and 5.");
            return "error";
        }

        boolean reviewerIsOwner = booking.getOwner() != null
                && booking.getOwner().getId().equals(reviewer.getId());

        boolean reviewerIsSitter = booking.getSitter() != null
                && booking.getSitter().getId().equals(reviewer.getId());

        boolean revieweeIsOwner = booking.getOwner() != null
                && booking.getOwner().getId().equals(reviewee.getId());

        boolean revieweeIsSitter = booking.getSitter() != null
                && booking.getSitter().getId().equals(reviewee.getId());

        if ((!reviewerIsOwner && !reviewerIsSitter) || (!revieweeIsOwner && !revieweeIsSitter)) {
            model.addAttribute("error", "This review is not valid for this booking.");
            return "error";
        }

        // prevent duplicate review
        Review existing = reviewRepository.findByBookingIdAndReviewerIdAndRevieweeId(
                bookingId, reviewer.getId(), revieweeId
        );

        if (existing != null) {
            return "redirect:/dashboard";
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(rating);
        review.setComment(comment);

        if (reviewerIsOwner && revieweeIsSitter) {
            review.setReviewType("OWNER_TO_SITTER");
        } else if (reviewerIsSitter && revieweeIsOwner) {
            review.setReviewType("SITTER_TO_OWNER");
        } else {
            model.addAttribute("error", "Invalid review direction.");
            return "error";
        }

        reviewRepository.save(review);

        return "redirect:/dashboard";
    }

    @GetMapping("/admin")
    public String admin(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        return "admin";
    }

    @GetMapping("/sitter-rating/{id}")
    public String getSitterRating(@PathVariable Long id, Model model) {

        User sitter = userRepository.findById(id).orElse(null);

        if (sitter == null) {
            model.addAttribute("error", "Sitter not found.");
            return "error";
        }

        Double avg = reviewRepository.getAverageRatingForUser(id);

        model.addAttribute("sitter", sitter);
        model.addAttribute("averageRating", avg);

        return "sitter-rating";
    }

    

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


