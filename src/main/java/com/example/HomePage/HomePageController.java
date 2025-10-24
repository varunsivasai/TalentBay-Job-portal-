
package com.example.HomePage;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomePageController {

    @Autowired
    private RegisterJobSeekerRepository jobSeekerRepository;

    @Autowired
    private RegisterJobRecruiterRepository recruiterRepository;

    // ---------------- HOME ----------------
    @GetMapping("/")
    public String home() {
        return "HomePage"; // ✅ HomePage.html
    }

    // ---------------- LOGIN PAGES ----------------
    @GetMapping("/login/jobseeker")
    public String loginJobseeker(@ModelAttribute("msg") String msg,
                                 @ModelAttribute("usernameValue") String usernameValue,
                                 Model model) {
        if (msg != null && !msg.isEmpty()) model.addAttribute("msg", msg);
        if (usernameValue != null && !usernameValue.isEmpty()) {
            model.addAttribute("usernameValue", usernameValue);
        }
        return "LoginJobseekerPage"; // ✅ LoginJobseekerPage.html
    }

    @GetMapping("/login/recruiter")
    public String loginRecruiter(@ModelAttribute("msg") String msg,
                                 @ModelAttribute("usernameValue") String usernameValue,
                                 Model model) {
        if (msg != null && !msg.isEmpty()) model.addAttribute("msg", msg);
        if (usernameValue != null && !usernameValue.isEmpty()) {
            model.addAttribute("usernameValue", usernameValue);
        }
        return "LoginRecuiterPage"; // ✅ LoginRecuiterPage.html
    }

    // ---------------- LOGIN PROCESSING ----------------
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam(required = false) String role,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        username = (username != null) ? username.trim() : "";
        password = (password != null) ? password.trim() : "";
        role = (role != null) ? role.trim().toLowerCase() : "";

        if (username.isEmpty() || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "Username and password cannot be empty.");
            redirectAttributes.addFlashAttribute("usernameValue", username);
            return "recruiter".equals(role)
                    ? "redirect:/login/recruiter"
                    : "redirect:/login/jobseeker";
        }

        // Default to jobseeker if role is not specified
        if (role.isEmpty()) {
            role = "jobseeker";
        }

        if ("jobseeker".equals(role)) {
            RegisterJobSeeker user = jobSeekerRepository.findByUsername(username);
            if (user != null && password.equals(user.getPassword())) {
                session.setAttribute("jobseekerUsername", username);
                
                // Check for a redirect URL from a previous action (like applying for a job)
                String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
                if (redirectAfterLogin != null) {
                    session.removeAttribute("redirectAfterLogin");
                    return "redirect:" + redirectAfterLogin;
                }
                
                return "redirect:/profile"; // Correct: Redirect to the profile page
            } else {
                redirectAttributes.addFlashAttribute("msg", "Invalid username or password.");
                redirectAttributes.addFlashAttribute("usernameValue", username);
                return "redirect:/login/jobseeker";
            }
        } else if ("recruiter".equals(role)) {
            RegisterRecruiter user = recruiterRepository.findByUsername(username);
            if (user != null && password.equals(user.getPassword())) {
                session.setAttribute("recruiterUsername", username);
                return "redirect:/recruiter/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("msg", "Invalid username or password.");
                redirectAttributes.addFlashAttribute("usernameValue", username);
                return "redirect:/login/recruiter";
            }
        }

        // This part will now only be reached if the role is something other than "jobseeker" or "recruiter"
        redirectAttributes.addFlashAttribute("msg", "Invalid role specified. Please log in again.");
        redirectAttributes.addFlashAttribute("usernameValue", username);
        return "redirect:/login/jobseeker";
    }

    // ---------------- REGISTRATION PAGES ----------------
    @GetMapping("/register/jobseeker")
    public String registerJobseekerPage() {
        return "Register-jobseeker"; // ✅ Register-jobseeker.html
    }

    @GetMapping("/register/recruiter")
    public String registerRecruiterPage() {
        return "Register-recruiter"; // ✅ Register-recuiter.html
    }

    // ---------------- REGISTER JOB SEEKER ----------------
    @PostMapping("/register/jobseeker")
    public String registerJobseeker(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

        username = (username != null) ? username.trim() : "";
        password = (password != null) ? password.trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("msg", "Username and password cannot be empty.");
            model.addAttribute("usernameValue", username);
            return "Register-jobseeker";
        }

        var existing = jobSeekerRepository.findByUsername(username);
        if (existing != null) {
            Integer attempts = (Integer) session.getAttribute("jobseekerRegisterAttempts");
            attempts = (attempts == null) ? 1 : attempts + 1;
            session.setAttribute("jobseekerRegisterAttempts", attempts);

            if (attempts >= 2) {
                model.addAttribute("msg", "Account already exists. Use 'Forgot Password' to recover.");
                model.addAttribute("showForgotPassword", true);
            } else {
                model.addAttribute("msg", "Username already exists. Try a different username.");
            }
            model.addAttribute("usernameValue", username);
            return "Register-jobseeker";
        }

        RegisterJobSeeker js = new RegisterJobSeeker();
        js.setUsername(username);
        js.setPassword(password);

        try {
        jobSeekerRepository.save(js);
            session.removeAttribute("jobseekerRegisterAttempts");
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute("msg", "Could not create account. Please try a different username.");
            model.addAttribute("usernameValue", username);
            return "Register-jobseeker";
        }

        redirectAttributes.addFlashAttribute("username", username);
        return "redirect:/register/jobseeker/success";
    }

    @GetMapping("/register/jobseeker/success")
    public String registerJobseekerSuccess(@ModelAttribute("username") String username, Model model) {
        model.addAttribute("username", username);
        return "registerJobSeekerSuccesfully"; // ✅ registerJobSeekerSuccesfully.html
    }

    // ---------------- REGISTER RECRUITER ----------------
    @PostMapping("/register/recruiter")
    public String registerRecruiter(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {

        username = (username != null) ? username.trim() : "";
        password = (password != null) ? password.trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("msg", "Username and password cannot be empty.");
            return "Register-recruiter"; // ✅ fixed typo
        }

        var existing = recruiterRepository.findByUsername(username);
        if (existing != null) {
            Integer attempts = (Integer) session.getAttribute("recruiterRegisterAttempts");
            attempts = (attempts == null) ? 1 : attempts + 1;
            session.setAttribute("recruiterRegisterAttempts", attempts);

            if (attempts >= 2) {
                model.addAttribute("msg", "Account already exists. Use 'Forgot Password' to recover.");
                model.addAttribute("showForgotPassword", true);
            } else {
                model.addAttribute("msg", "Username already exists. Try a different username.");
            }
            return "Register-recruiter"; // ✅ fixed
        }

        session.removeAttribute("recruiterRegisterAttempts");

        RegisterRecruiter rec = new RegisterRecruiter();
        rec.setUsername(username);
        rec.setPassword(password);
        recruiterRepository.save(rec);

        redirectAttributes.addFlashAttribute("username", username);
        return "redirect:/register/recruiter/success";
    }

    @GetMapping("/register/recruiter/success")
    public String registerRecruiterSuccess(@ModelAttribute("username") String username, Model model) {
        model.addAttribute("username", username);
        return "registerRecuiterSuccessfully"; // ✅ matches existing file
    }

    // ---------------- FORGOT PASSWORD ----------------
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "ForgotPasswordPage"; // ✅ ForgotPasswordPage.html
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, Model model) {

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("message", "Please enter your username.");
            return "ForgotPasswordPage";
        }

        var js = jobSeekerRepository.findByUsername(username);
        if (js != null) {
            return "redirect:/reset-password?username=" + username + "&type=jobseeker";
        }

        var rec = recruiterRepository.findByUsername(username);
        if (rec != null) {
            return "redirect:/reset-password?username=" + username + "&type=recruiter";
        }

        model.addAttribute("message", "Username not found. Please check and try again.");
        return "ForgotPasswordPage";
    }

    // ---------------- RESET PASSWORD ----------------
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String username,
                                    @RequestParam(required = false) String type,
                                    Model model) {
        model.addAttribute("username", username);
        model.addAttribute("type", type);
        return "ResetPasswordPage"; // ✅ ResetPasswordPage.html
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String username,
                                       @RequestParam String newPassword,
                                       @RequestParam(required = false) String type,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("msg", "Username cannot be empty.");
            model.addAttribute("username", username);
            model.addAttribute("type", type);
            return "ResetPasswordPage";
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            model.addAttribute("msg", "Password cannot be empty.");
            model.addAttribute("username", username);
            model.addAttribute("type", type);
            return "ResetPasswordPage";
        }

        if (newPassword.length() < 4) {
            model.addAttribute("msg", "Password must be at least 4 characters long.");
            model.addAttribute("username", username);
            model.addAttribute("type", type);
            return "ResetPasswordPage";
        }

        boolean updated = false;
        boolean isRecruiter = false;

        var js = jobSeekerRepository.findByUsername(username);
        if (js != null) {
            js.setPassword(newPassword);
            jobSeekerRepository.save(js);
            updated = true;
        }

        var rec = recruiterRepository.findByUsername(username);
        if (rec != null) {
            rec.setPassword(newPassword);
            recruiterRepository.save(rec);
            updated = true;
            isRecruiter = true;
        }

        if (updated) {
            redirectAttributes.addFlashAttribute("msg", "Password updated successfully. Please log in.");
            return isRecruiter ? "redirect:/login/recruiter" : "redirect:/login/jobseeker";
        } else {
            model.addAttribute("msg", "Username not found. Please check and try again.");
            model.addAttribute("username", username);
            model.addAttribute("type", type);
            return "ResetPasswordPage";
        }
    }
}
