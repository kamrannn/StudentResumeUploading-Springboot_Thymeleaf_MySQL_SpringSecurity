package com.app.OnboardingSystem.service;

import com.app.OnboardingSystem.model.File;
import com.app.OnboardingSystem.model.User;
import com.app.OnboardingSystem.repository.FileRepository;
import com.app.OnboardingSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class FileService {
    FileRepository fileRepository;
    UserRepository userRepository;

    @Autowired
    public FileService(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    public File store(MultipartFile file, Integer userId, String documentName) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        File FileDB = new File(fileName, file.getContentType(), file.getBytes());
        FileDB.setDocumentName(documentName);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().getFileList().add(FileDB);
            userRepository.save(user.get());
        }
        return FileDB;
    }

    public File updateFile(MultipartFile file, Integer fileId, Integer userId, String documentName) throws IOException {
        Optional<File> existingFile = fileRepository.findById(fileId);
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        File FileDB = new File(fileName, file.getContentType(), file.getBytes());
        FileDB.setId(fileId);
        FileDB.setDocumentName(documentName);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().getFileList().remove(existingFile.get());
            userRepository.save(user.get());
            user.get().getFileList().add(FileDB);
            userRepository.save(user.get());
        }
        return FileDB;
    }

    public File getFile(Integer id) {
        return fileRepository.findById(id).get();
    }

    public Stream<File> getAllFiles() {
        return fileRepository.findAll().stream();
    }

    public List<File> getAllFilesFromDb() {
        return fileRepository.findAll();
    }

    public List<File> getAllFilesOfASingleUser(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            List<File> fileList = user.get().getFileList();
            return fileList;
        } else {
            return null;
        }
    }

/*    public List<File> getFilesByNameContaining(String fileName) {
        return fileRepository.findAllByNameContaining(fileName);
    }*/

    public void deleteFile(Integer fileId) {
        Optional<File> file = fileRepository.findById(fileId);
        if (file.isPresent()) {
            fileRepository.delete(file.get());
        } else {
            throw new RuntimeException();
        }
    }
}