package com.sithappens.sithappens;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sithappens.sithappens.model.Availability;
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
        // 🔥 GET REVIEWS ABOUT THIS OWNER
        List<Review> ownerReviews = reviewRepository.findByRevieweeId(user.getId());
        model.addAttribute("ownerReviews", ownerReviews);

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


        // 🔽 KEEP YOUR EXISTING RETURN LOGIC
        if ("OWNER".equals(user.getRole())) {
            return "owner-dashboard";
        } else {
            return "sitter-dashboard";
        }
    }

    @GetMapping("/edit-pet/{id}")
    public String editPet(@PathVariable Long id, Model model, HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Pet pet = petRepository.findById(id).orElse(null);

        if (pet == null || !pet.getOwner().getId().equals(user.getId())) {
            return "error";
        }

        model.addAttribute("pet", pet);

        return "edit-pet";
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

    @PostMapping("/update-pet")
    public String updatePet(@RequestParam Long id,
                            @RequestParam String name,
                            @RequestParam String type,
                            @RequestParam(required = false) Integer age,
                            @RequestParam(required = false) String breed,
                            @RequestParam(required = false) String notes,
                            @RequestParam("photo") MultipartFile photo,
                            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Pet pet = petRepository.findById(id).orElse(null);

        if (pet == null || !pet.getOwner().getId().equals(user.getId())) {
            return "error";
        }

        // update fields
        pet.setName(name);
        pet.setType(type);
        pet.setAge(age);
        pet.setBreed(breed);
        pet.setNotes(notes);
    
        System.out.println("PHOTO EMPTY? " + photo.isEmpty());

        // 🖼️ update image ONLY if new one uploaded
        // 🖼️ update image ONLY if new one uploaded
    System.out.println("PHOTO EMPTY? " + photo.isEmpty());

    if (!photo.isEmpty()) {
        try {
            String uploadDir = new File("src/main/resources/static/uploads/").getAbsolutePath() + "/";

            // ensure folder exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();

            System.out.println("SETTING IMAGE PATH: " + "/uploads/" + fileName);

            File file = new File(uploadDir + fileName);
            photo.transferTo(file);

            pet.setImagePath("/uploads/" + fileName);

            System.out.println("Saved file to: " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        petRepository.save(pet);

        System.out.println("SAVED PET IMAGE PATH: " + pet.getImagePath());

        return "redirect:/dashboard";
    }

    @PostMapping("/save-pet")
    public String savePet(@RequestParam String name,
                        @RequestParam String type,
                        @RequestParam(required = false) Integer age,
                        @RequestParam(required = false) String breed,
                        @RequestParam(required = false) String notes,
                        @RequestParam("photo") MultipartFile photo,
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

        // 🖼️ HANDLE IMAGE UPLOAD
        if (!photo.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/uploads/";

                // ✅ MAKE SURE FOLDER EXISTS
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }

                // create unique filename
                String fileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();

                File file = new File(uploadFolder, fileName);

                // save file
                photo.transferTo(file);

                // save path to database
                pet.setImagePath("/uploads/" + fileName);

                System.out.println("Saved file to: " + file.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

        System.out.println("CLICKED SITTER ID: " + id);
        System.out.println("SITTER FOUND: " + sitter);

        // Handle sitter not found (but DO NOT break page)
        if (sitter == null) {
            model.addAttribute("errorMessage", "Sitter not found.");
        }

        // Always send sitter
        model.addAttribute("sitter", sitter);

        // ===== GET OWNER PETS =====
        List<Pet> ownerPets = new ArrayList<>();
        for (Pet pet : petRepository.findAll()) {
            if (pet.getOwner() != null &&
                pet.getOwner().getId().equals(owner.getId())) {
                ownerPets.add(pet);
            }
        }
        model.addAttribute("pets", ownerPets);

        // ===== CALENDAR =====
        Map<String, Map<LocalDate, String>> calendarByMonth = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();

        // Generate 90 days (3 months)
        for (int i = 0; i < 90; i++) {
            LocalDate date = today.plusDays(i);
            String month = date.getMonth().toString() + " " + date.getYear();

            calendarByMonth.putIfAbsent(month, new TreeMap<>());
            calendarByMonth.get(month).put(date, null);
        }

        // ===== AVAILABILITY =====
        for (Availability a : availabilityRepository.findAll()) {
            if (sitter != null &&
                a.getSitter() != null &&
                a.getSitter().getId().equals(sitter.getId())) {

                String month = a.getAvailableDate().getMonth().toString() + " " + a.getAvailableDate().getYear();

                if (calendarByMonth.containsKey(month)) {
                    calendarByMonth.get(month).put(a.getAvailableDate(), "AVAILABLE");
                }
            }
        }

        // ===== BOOKINGS =====
        for (Booking b : bookingRepository.findAll()) {
            if (sitter != null &&
                b.getSitter() != null &&
                b.getSitter().getId().equals(sitter.getId())) {

                LocalDate d = b.getStartDate();

                while (!d.isAfter(b.getEndDate())) {

                    String month = d.getMonth().toString() + " " + d.getYear();

                    if (calendarByMonth.containsKey(month)) {
                        if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                            calendarByMonth.get(month).put(d, "BOOKED");
                        } else if ("REQUESTED".equalsIgnoreCase(b.getStatus())) {
                            calendarByMonth.get(month).put(d, "PENDING");
                        }
                    }

                    d = d.plusDays(1);
                }
            }
        }

        // 🔥 THIS WAS MISSING — VERY IMPORTANT
        model.addAttribute("calendarByMonth", calendarByMonth);

        return "request-booking";
    }

        @PostMapping("/request-booking")
        public String requestBooking(@RequestParam Long sitterId,
                                    @RequestParam Long petId,
                                    @RequestParam String serviceType,
                                    @RequestParam String startDate,
                                    @RequestParam String endDate,
                                    @RequestParam String requestMessage,
                                    @RequestParam(required = false) String contactInfo,
                                    HttpSession session,
                                    Model model) {
            
            // 🔥 ADD THIS RIGHT HERE
            System.out.println("===== REQUEST BOOKING HIT =====");
            System.out.println("CONTACT INFO: " + contactInfo);

        
            User owner = (User) session.getAttribute("loggedInUser");

            if (owner == null) {
                return "redirect:/login";
            }

            User sitter = userRepository.findById(sitterId).orElse(null);
            Pet pet = petRepository.findById(petId).orElse(null);

            // 🔍 DEBUG LOGS (super helpful)
            System.out.println("===== BOOKING DEBUG =====");
            System.out.println("OWNER: " + owner);
            System.out.println("SITTER: " + sitter);
            System.out.println("PET: " + pet);
            System.out.println("START: " + startDate);
            System.out.println("END: " + endDate);
            System.out.println("CONTACT INFO: " + contactInfo);

            // 🚨 PREVENT CRASHES
            if (sitter == null) {
                model.addAttribute("error", "Sitter not found.");
                return "error";
            }

            if (pet == null) {
                model.addAttribute("error", "Pet not found.");
                return "error";
            }

            try {
                Booking booking = new Booking();
        

                booking.setOwner(owner);
                booking.setSitter(sitter);   // 🔥 IMPORTANT
                booking.setPet(pet);

                booking.setServiceType(serviceType);

                // ✅ Safe date parsing
                booking.setStartDate(LocalDate.parse(startDate));
                booking.setEndDate(LocalDate.parse(endDate));

                booking.setRequestMessage(requestMessage);
                booking.setStatus("REQUESTED");

                // ✅ NEW FIELD (safe)
                booking.setContactInfo(contactInfo);

                bookingRepository.save(booking);

                System.out.println("✅ BOOKING SAVED SUCCESSFULLY");

            } catch (Exception e) {
                System.out.println("❌ ERROR SAVING BOOKING");
                e.printStackTrace();

                model.addAttribute("error", "Something went wrong creating the booking.");
                return "error";
            }

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

        // ✅ Validate review direction (clean + reliable)
        boolean isOwnerReview = booking.getOwner() != null
                && booking.getSitter() != null
                && booking.getOwner().getId().equals(reviewer.getId())
                && booking.getSitter().getId().equals(reviewee.getId());

        boolean isSitterReview = booking.getOwner() != null
                && booking.getSitter() != null
                && booking.getSitter().getId().equals(reviewer.getId())
                && booking.getOwner().getId().equals(reviewee.getId());

        if (!isOwnerReview && !isSitterReview) {
            model.addAttribute("error", "This review is not valid for this booking.");
            return "error";
        }

        // ✅ Prevent duplicate review
        Review existing = reviewRepository.findByBookingIdAndReviewerIdAndRevieweeId(
                bookingId, reviewer.getId(), revieweeId
        );

        if (existing != null) {
            return "redirect:/dashboard";
        }

        // ✅ Create review
        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(rating);
        review.setComment(comment);

        // ✅ Set review type (FIXED LOGIC)
        if (isOwnerReview) {
            review.setReviewType("OWNER_TO_SITTER");
        } else {
            review.setReviewType("SITTER_TO_OWNER");
        }

        reviewRepository.save(review);

        if ("OWNER".equals(reviewer.getRole())) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/dashboard";
        }
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

        // average rating (what you already have)
        Double avg = reviewRepository.getAverageRatingForUser(id);

        // 🔥 NEW: get all reviews for this sitter
        List<Review> reviews = reviewRepository.findByRevieweeId(id);

        model.addAttribute("sitter", sitter);
        model.addAttribute("averageRating", avg);
        model.addAttribute("reviews", reviews);   // 🔥 IMPORTANT
       

        return "sitter-rating";
    }

    // 📅 Toggle Availability (click date)
    @GetMapping("/toggle-availability/{date}")
    public String toggleAvailability(@PathVariable String date, HttpSession session) {

        // get logged-in user (sitter)
        User sitter = (User) session.getAttribute("loggedInUser");

        if (sitter == null) {
            return "redirect:/login";
        }

        LocalDate selectedDate = LocalDate.parse(date);

        // check if date already exists
        List<Availability> all = availabilityRepository.findAll();

        for (Availability a : all) {
            if (a.getSitter().getId().equals(sitter.getId()) &&
                a.getAvailableDate().equals(selectedDate)) {

                // ❌ remove availability
                availabilityRepository.delete(a);
                return "redirect:/availability";
            }
        }

        // ✅ add availability
        Availability availability = new Availability();
        availability.setSitter(sitter);
        availability.setAvailableDate(selectedDate);

        availabilityRepository.save(availability);

        return "redirect:/availability";
    }


    @GetMapping("/availability")
    public String availabilityPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        if (!user.getRole().equalsIgnoreCase("SITTER")) {
            return "redirect:/dashboard";
        }

        Map<String, Map<LocalDate, String>> calendarByMonth = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();

        // generate 3 months
        for (int i = 0; i < 90; i++) {
            LocalDate date = today.plusDays(i);
            String month = date.getMonth().toString() + " " + date.getYear();

            calendarByMonth.putIfAbsent(month, new TreeMap<>());
            calendarByMonth.get(month).put(date, null);
        }

        // availability
        List<Availability> allAvailability = availabilityRepository.findAll();
        for (Availability a : allAvailability) {
            if (a.getSitter().getId().equals(user.getId())) {

                String month = a.getAvailableDate().getMonth().toString() + " " + a.getAvailableDate().getYear();

                if (calendarByMonth.containsKey(month)) {
                    calendarByMonth.get(month).put(a.getAvailableDate(), "AVAILABLE");
                }
            }
        }

        // bookings overlay
        List<Booking> allBookings = bookingRepository.findAll();
        for (Booking b : allBookings) {
            if (b.getSitter() != null && b.getSitter().getId().equals(user.getId())) {

                LocalDate date = b.getStartDate();

                while (!date.isAfter(b.getEndDate())) {

                    String month = date.getMonth().toString() + " " + date.getYear();

                    if (calendarByMonth.containsKey(month)) {
                        if ("CONFIRMED".equalsIgnoreCase(b.getStatus())) {
                            calendarByMonth.get(month).put(date, "BOOKED");
                        } else if ("REQUESTED".equalsIgnoreCase(b.getStatus())) {
                            calendarByMonth.get(month).put(date, "PENDING");
                        }
                    }

                    date = date.plusDays(1);
                }
            }
        }

        model.addAttribute("calendarByMonth", calendarByMonth);
        model.addAttribute("user", user);

        return "availability";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // clears login session
        return "redirect:/";
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

    @PostMapping("/delete-account")
    public String deleteAccount(HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        Long userId = user.getId();

        // ✅ 1. Delete reviews FIRST
        reviewRepository.deleteAll(
            reviewRepository.findAll().stream()
                .filter(r -> r.getReviewer().getId().equals(userId) ||
                            r.getReviewee().getId().equals(userId))
                .toList()
        );

        // ✅ 2. Delete bookings
        bookingRepository.deleteAll(
            bookingRepository.findAll().stream()
                .filter(b -> b.getOwner().getId().equals(userId) ||
                            b.getSitter().getId().equals(userId))
                .toList()
        );

        // ✅ 3. Delete availability
        availabilityRepository.deleteAll(
            availabilityRepository.findAll().stream()
                .filter(a -> a.getSitter().getId().equals(userId))
                .toList()
        );

        // ✅ 4. Delete pets
        petRepository.deleteAll(
            petRepository.findAll().stream()
                .filter(p -> p.getOwner().getId().equals(userId))
                .toList()
        );

        // ✅ 5. Delete user LAST
        userRepository.deleteById(userId);

        session.invalidate();

        return "redirect:/";
    }
}


