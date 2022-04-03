package com.app.OnboardingSystem.controller;

import com.app.OnboardingSystem.model.*;
import com.app.OnboardingSystem.model.dto.ResponseFile;
import com.app.OnboardingSystem.repository.AddressRepository;
import com.app.OnboardingSystem.repository.EducationRepository;
import com.app.OnboardingSystem.repository.UserRepository;
import com.app.OnboardingSystem.repository.WorkExperienceRepository;
import com.app.OnboardingSystem.service.FileService;
import com.app.OnboardingSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@Controller
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationRepository educationRepository;
    private final FileService storageService;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, AddressRepository addressRepository, WorkExperienceRepository workExperienceRepository, EducationRepository educationRepository, FileService storageService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.workExperienceRepository = workExperienceRepository;
        this.educationRepository = educationRepository;
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userWithPhoneNumber = userRepository.findUserByPhoneNumber(username);
        model.addAttribute("user", userWithPhoneNumber.get());

        List<File> files = userWithPhoneNumber.get().getFileList();
        List<ResponseFile> responseFilesList = new ArrayList<>();
        for (File file : files
        ) {
            String fileDownload = ServletUriComponentsBuilder.fromCurrentContextPath().path("/files/").path(file.getId() + "").toUriString();
            ResponseFile responseFile = new ResponseFile();
            responseFile.setId(file.getId());
            responseFile.setUrl(fileDownload);
            responseFile.setName(file.getName());
            responseFile.setSize(file.getData().length);
            responseFile.setType(file.getType());
            responseFile.setDocumentName(file.getDocumentName());
            responseFilesList.add(responseFile);
        }

        model.addAttribute("filesList", responseFilesList);
        return "application";
    }


    @GetMapping("/login-error")
    public String loginError(Model model) {
        LoginDTO loginDTO = new LoginDTO();
        model.addAttribute(loginDTO);
        model.addAttribute("message", "Alert: This phone number doesn't exists");
        return "login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        LoginDTO loginDTO = new LoginDTO();
        model.addAttribute(loginDTO);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/users/create")
    public String create(@Valid User user, BindingResult result, Model model) {
        Optional<User> userWithPhoneNumber = userRepository.findUserByPhoneNumber(user.getPhoneNumber());
        if (userWithPhoneNumber.isPresent()) {
            result.rejectValue("phoneNumber", null, "There is already an account registered with that phone number");
        }
        Optional<User> userWithEmail = userRepository.findUserByEmail(user.getEmail());
        if (userWithEmail.isPresent()) {
            result.rejectValue("email", null, "There is already an user account registered with that email");
        }

        if (result.hasErrors()) {
            return "register";
        }
        System.out.println("User created");
        userService.createUser(user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("address", new Address());
        return "address";
    }

    @PostMapping("/users/address/{id}")
    public String addAddress(Address address, @PathVariable(name = "id") Integer userId, Model model) {
        System.out.println("Address created");
        User user = userService.getUserById(userId);
        user.setAddress(address);
        userService.createUser(user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("work", new WorkExperience());
        return "work";
    }

    @PostMapping("/users/work/{id}")
    public String addWork(WorkExperience workExperience, @PathVariable(name = "id") Integer userId, Model model) {
        System.out.println("work experience created");
        User user = userService.getUserById(userId);
        user.getWorkExperienceList().add(workExperience);
        userService.createUser(user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("education", new EducationalDetail());
        return "education";
    }

    @PostMapping("/users/educational/{id}")
    public String addEducation(EducationalDetail educationalDetail, @PathVariable(name = "id") Integer userId, Model model) {
        System.out.println("EducationalDetail created");
        User user = userService.getUserById(userId);
        user.getEducationalDetails().add(educationalDetail);
        userService.createUser(user);

        String documentName = "";

        model.addAttribute("userId", user.getId());
        model.addAttribute("documentName", documentName);
        return "upload-file";
    }

    // Files Api while registering
    @GetMapping("/upload")
    public String getUploadFileOption() {
        return "upload-file";
    }

    @PostMapping("/upload/{id}")
    public String uploadFile(@PathVariable(name = "id") Integer userId, @RequestParam(name = "documentName") String nameOfDocument, @RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            model.addAttribute("message", "Kindly select any file before submission");
        } else {
            storageService.store(file, userId, nameOfDocument);
            model.addAttribute("message", nameOfDocument + " is successfully uploaded");
        }
        String documentName = "";
        model.addAttribute("userId", userId);
        model.addAttribute("documentName", documentName);
        return "upload-file";
    }

    // Files Api after login
    @GetMapping("/file/delete/{id}")
    public String deleteFile(@PathVariable(name = "id") Integer fileId) {
        storageService.deleteFile(fileId);
        return "redirect:/";
    }

    @GetMapping("/user/upload/file")
    public String uploadMoreFilesPage() {
        return "add-more-files";
    }

    @PostMapping("/user/upload")
    public String uploadMoreFiles(@RequestParam(name = "documentName") String nameOfDocument, @RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            model.addAttribute("message", "Kindly select any file before submission");
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            Optional<User> userWithPhoneNumber = userRepository.findUserByPhoneNumber(username);

            storageService.store(file, userWithPhoneNumber.get().getId(), nameOfDocument);
            model.addAttribute("message", nameOfDocument + " is successfully uploaded");
        }
        return "add-more-files";
    }

    // From here the fetching things starts
    @GetMapping("/application")
    public String applicationPage(Model model) {
        model.addAttribute("address", new Address());
        model.addAttribute("work", new WorkExperience());
        model.addAttribute("education", new EducationalDetail());
        model.addAttribute("file", new File());
        return "application";
    }

    @GetMapping("/users/work/add-more-work-form/{id}")
    public String addMoreWorkDetails(@PathVariable(name = "id") Integer userId, Model model) {
        System.out.println("Add more work form has been showed");
        model.addAttribute("userId", userId);
        model.addAttribute("work", new WorkExperience());
        return "add-more-work";
    }

    @PostMapping("/users/add-more-work-form/{id}")
    public String addMoreWorkDetails(WorkExperience workExperience, @PathVariable(name = "id") Integer userId, Model model) {
        System.out.println("work experience created");
        workExperience.setId(null);
        User user = userService.getUserById(userId);
        List<WorkExperience> workExperienceList = user.getWorkExperienceList();
        workExperienceList.add(workExperience);
        userService.update(user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("education", new EducationalDetail());
        return "redirect:/";
    }

    @GetMapping("/users/education/add-more/{id}")
    public String addMoreEducationDetails(@PathVariable(name = "id") Integer userId, Model model) {
        System.out.println("Add more work form has been showed");
        model.addAttribute("userId", userId);
        model.addAttribute("education", new EducationalDetail());
        return "add-more-education";
    }

    @PostMapping("/users/education/add-more/{id}")
    public String addMoreEducationDetails(EducationalDetail educationalDetail, @PathVariable(name = "id") Integer userId, Model model) {
        System.out.println("EducationalDetail created");
        educationalDetail.setId(null);
        User user = userService.getUserById(userId);
        List<EducationalDetail> educationalDetails = user.getEducationalDetails();
        educationalDetails.add(educationalDetail);
        userService.update(user);
        model.addAttribute("userId", user.getId());
        model.addAttribute("education", new EducationalDetail());
        return "redirect:/";
    }

    @GetMapping("/application/{id}")
    public String applicationPageExtra(Model model, @PathVariable(name = "id") Integer userId) {
        Optional<User> userWithId = userRepository.findById(userId);
        model.addAttribute("address", new Address());
        model.addAttribute("work", new WorkExperience());
        model.addAttribute("education", new EducationalDetail());
        model.addAttribute("user", userWithId.get());
        return "application";
    }

    /**
     * From here the update sections starts
     */

    @GetMapping("/user/personal-details/update")
    public String userUpdateForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userWithPhoneNumber = userRepository.findUserByPhoneNumber(username);

        model.addAttribute("user", userService.getUserById(userWithPhoneNumber.get().getId()));
        return "user-personal-detail-update";
    }

    @PostMapping("/user/personal-details/update/{id}")
    public String userUpdateFormSubmission(@PathVariable(name = "id") Integer userId, @Valid User user, BindingResult result, Model model) {
        user.setId(userId);
        String oldPhoneNumber = "";
        Optional<User> userWithId = userRepository.findById(userId);
        if (userWithId.isPresent()) {
            oldPhoneNumber = userWithId.get().getPhoneNumber();
        }
        Optional<User> userWithPhoneNumber = userRepository.findUserByPhoneNumber(user.getPhoneNumber());
        if (userWithPhoneNumber.isPresent() && !user.getId().equals(userWithPhoneNumber.get().getId())) {
            result.rejectValue("phoneNumber", null, "There is already an account registered with that phone number");
        }

        Optional<User> userWithEmail = userRepository.findUserByEmail(user.getEmail());
        if (userWithEmail.isPresent() && !user.getId().equals(userWithEmail.get().getId())) {
            result.rejectValue("email", null, "There is already an user account registered with that email");
        }

        if (result.hasErrors()) {
            return "user-personal-detail-update";
        }

        Optional<User> userOptional = userRepository.findById(user.getId());
        user.setAddress(userOptional.get().getAddress());
        user.setEducationalDetails(userOptional.get().getEducationalDetails());
        user.setWorkExperienceList(userOptional.get().getWorkExperienceList());
        user.setFileList(userOptional.get().getFileList());
        System.out.println("Personal details updated");
        User user1 = userService.createUser(user);
        model.addAttribute("userId", user.getId());
        if (oldPhoneNumber.equalsIgnoreCase(user1.getPhoneNumber())) {
            model.addAttribute("user", user1);
            return "personal-detail";
        } else {
            return "redirect:/logout";
        }
    }

    @GetMapping("/user/address/update/{id}")
    public String userAddressForm(Model model, @PathVariable(name = "id") Integer userId) {
        model.addAttribute("userId", userId);
        model.addAttribute("address", userService.getUserById(userId).getAddress());
        return "user-address-update";
    }

    @PostMapping("/user/address/update/{addressId}")
    public String addAddress(@PathVariable(name = "addressId") Integer addressId, Address address, BindingResult result) {
        System.out.println("Address Updated");
        if (result.hasErrors()) {
            return "user-address-update";
        }
        address.setId(addressId);
        addressRepository.save(address);
        return "redirect:/";
    }

    @GetMapping("/user/work/delete/{workId}/{userId}")
    public String deleteWorkExperience(@PathVariable(value = "workId") Integer workId, @PathVariable(value = "userId") Integer userId) {
        Optional<WorkExperience> workExperience = workExperienceRepository.findById(workId);
        if (workExperience.isPresent()) {
            workExperienceRepository.delete(workExperience.get());
        }
        return "redirect:/";
    }

    @GetMapping("/user/work/update/{workId}")
    public String updateWorkExperienceForm(@PathVariable(value = "workId") Integer workId, Model model) {
        Optional<WorkExperience> workExperience = workExperienceRepository.findById(workId);
        if (workExperience.isPresent()) {
            model.addAttribute("work", workExperience.get());
        }
        return "user-work-update";
    }

    @PostMapping("/user/work/update/{workId}")
    public String updateWorkExperienceFormSubmission(@PathVariable(value = "workId") Integer workId, WorkExperience work, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "user-work-update";
        }

        Optional<WorkExperience> workExperience = workExperienceRepository.findById(workId);
        if (workExperience.isPresent()) {
            workExperience.get().setCompanyName(work.getCompanyName());
            workExperience.get().setDesignationAtJoining(work.getDesignationAtJoining());
            workExperience.get().setDesignationAtLeaving(work.getDesignationAtLeaving());
            workExperience.get().setJoiningDate(work.getJoiningDate());
            workExperience.get().setLeavingDate(work.getLeavingDate());
            workExperience.get().setYearsOfService(work.getYearsOfService());
            workExperienceRepository.save(workExperience.get());
        }
//        model.addAttribute("work", workExperience.get());
        return "redirect:/";
    }

    @GetMapping("/user/education/update/{educationId}")
    public String updateEducationForm(@PathVariable(value = "educationId") Integer educationId, Model model) {
        Optional<EducationalDetail> educationalDetail = educationRepository.findById(educationId);
        if (educationalDetail.isPresent()) {
            model.addAttribute("education", educationalDetail.get());
        }
        return "user-education-update";
    }

    @PostMapping("/user/education/update/{educationId}")
    public String updateEducationFormSubmission(@PathVariable(value = "educationId") Integer educationId, EducationalDetail education, BindingResult result) {
        if (result.hasErrors()) {
            return "user-education-update";
        }
        Optional<EducationalDetail> educationalDetail = educationRepository.findById(educationId);
        if (educationalDetail.isPresent()) {
            educationalDetail.get().setCourse(education.getCourse());
            educationalDetail.get().setStartingDate(education.getStartingDate());
            educationalDetail.get().setEndingDate(education.getEndingDate());
            educationalDetail.get().setInstitution(education.getInstitution());
            educationalDetail.get().setPercentageAchieved(education.getPercentageAchieved());
            educationRepository.save(educationalDetail.get());
        }
        return "redirect:/";
    }

    @GetMapping("/user/files/update/{fileId}")
    public String updateFileUploadForm(@PathVariable(value = "fileId") Integer fileId, Model model) {
        File file = storageService.getFile(fileId);
        if (file != null) {
            String documentName = file.getDocumentName();
            model.addAttribute("documentName", documentName);
            model.addAttribute("file", file);
        }
        return "file-upload-update";
    }

    @PostMapping("/user/files/updateFormSubmission/{fileId}")
    public String updateFileUploadFormSubmission(@PathVariable(value = "fileId") Integer fileId, @RequestParam(name = "documentName") String nameOfDocument, @RequestParam("file") MultipartFile file) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userWithPhoneNumber = userRepository.findUserByPhoneNumber(username);
        storageService.updateFile(file, fileId, userWithPhoneNumber.get().getId(), nameOfDocument);
        return "redirect:/";
    }


    @GetMapping("/user/education/delete/{educationId}/{userId}")
    public String deleteEducation(@PathVariable(value = "educationId") Integer educationId, @PathVariable(value = "userId") Integer userId) {
        Optional<EducationalDetail> educationalDetail = educationRepository.findById(educationId);
        if (educationalDetail.isPresent()) {
            educationRepository.delete(educationalDetail.get());
        }
        return "redirect:/";
    }

}
