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

import com.sithappens.sithappens.model.Availability;
import com.sithappens.sithappens.model.Booking;
import com.sithappens.sithappens.model.Review;
import com.sithappens.sithappens.model.User;
import com.sithappens.sithappens.repository.AvailabilityRepository;
import com.sithappens.sithappens.repository.BookingRepository;
import com.sithappens.sithappens.repository.ReviewRepository;
import com.sithappens.sithappens.repository.UserRepository;

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

    // 🏠 Homepage
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "index";
    }

    // 🔹 Create User (currently creates SITTER for testing sitter page)
    @PostMapping("/create-user")
    public String createUser() {

        User user = new User();
        user.setEmail("testuser" + System.currentTimeMillis() + "@email.com");
        user.setFirstName("Frontend");
        user.setLastName("User");
        user.setPasswordHash("hashedpassword");
        user.setPhone("1234567890");

        // Change to "OWNER" if needed
        user.setRole("SITTER");

        userRepository.save(user);

        return "redirect:/";
    }

    // 🐶 View Sitters Page
    @GetMapping("/sitters")
    public String viewSitters(Model model) {

        List<User> allUsers = userRepository.findAll();
        List<User> sitters = new ArrayList<>();

        for (User user : allUsers) {
            if ("SITTER".equals(user.getRole())) {
                sitters.add(user);
            }
        }

        model.addAttribute("sitters", sitters);

        return "sitters";
    }

    // 🐾 Book Specific Sitter
    @PostMapping("/book-sitter/{id}")
    public String bookSitter(@PathVariable Long id) {

        // For now we assume owner ID = 1 (temporary test logic)
        User owner = userRepository.findById(1L).orElse(null);
        User sitter = userRepository.findById(id).orElse(null);

        if (owner == null || sitter == null) {
            return "error";
        }

        Booking booking = new Booking();
        booking.setOwner(owner);
        booking.setSitter(sitter);
        booking.setStartDate(LocalDate.now());
        booking.setEndDate(LocalDate.now().plusDays(2));
        booking.setStatus("REQUESTED");

        bookingRepository.save(booking);

        return "redirect:/sitters";
    }

    // 🔹 Confirm Booking
    @GetMapping("/confirm-booking/{id}")
    public String confirmBooking(@PathVariable Long id) {

        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return "error";
        }

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        return "redirect:/";
    }

    // 🔹 Complete Booking
    @GetMapping("/complete-booking/{id}")
    public String completeBooking(@PathVariable Long id) {

        Booking booking = bookingRepository.findById(id).orElse(null);

        if (booking == null) {
            return "error";
        }

        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);

        return "redirect:/";
    }

    // ⭐ Add Review (Only If Booking Completed)
    @GetMapping("/add-review")
    public String addReview() {

        User owner = userRepository.findById(1L).orElse(null);
        User sitter = userRepository.findById(2L).orElse(null);
        Booking booking = bookingRepository.findById(1L).orElse(null);

        if (owner == null || sitter == null || booking == null) {
            return "error";
        }

        if (!"COMPLETED".equals(booking.getStatus())) {
            return "error";
        }

        Review review = new Review();
        review.setRating(5);
        review.setComment("Amazing sitter, would book again!");
        review.setOwner(owner);
        review.setSitter(sitter);
        review.setBooking(booking);

        reviewRepository.save(review);

        return "redirect:/";
    }

    // ⭐ Get Sitter Average Rating
    @GetMapping("/sitter-rating/{id}")
    public String getSitterRating(@PathVariable Long id, Model model) {

        List<Review> reviews = reviewRepository.findAll();

        double total = 0;
        int count = 0;

        for (Review review : reviews) {
            if (review.getSitter() != null &&
                review.getSitter().getId().equals(id)) {

                total += review.getRating();
                count++;
            }
        }

        if (count == 0) {
            model.addAttribute("ratingMessage", "No reviews yet.");
        } else {
            double average = total / count;
            model.addAttribute("ratingMessage",
                    "Average Rating: " + String.format("%.2f", average));
        }

        model.addAttribute("users", userRepository.findAll());

        return "index";
    }

    // 📅 Add Availability (Test)
        @GetMapping("/add-availability")
    public String addAvailability() {

        User sitter = userRepository.findById(2L).orElse(null);

        if (sitter == null) {
            return "error";
        }

        Availability availability = new Availability();
        availability.setAvailableDate(LocalDate.now().plusDays(5));
        availability.setSitter(sitter);

        availabilityRepository.save(availability);

        return "redirect:/";
    }

    // 📅 Bookings Page
    @GetMapping("/bookings")
    public String getBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "bookings";
    }

    // ⚙ Admin Page
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }
    

}


