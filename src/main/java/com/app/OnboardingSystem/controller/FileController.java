package com.app.OnboardingSystem.controller;

import com.app.OnboardingSystem.model.File;
import com.app.OnboardingSystem.model.dto.ResponseFile;
import com.app.OnboardingSystem.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin
public class FileController {

    @Autowired
    private FileService storageService;

    @GetMapping("/files/single/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable(name = "id") Integer id) {
        File fileDB = storageService.getFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDB.getName() + "\"")
                .body(fileDB.getData());
    }

    @GetMapping("/files")
    public String getListFiles(Model model) {
        List<File> files = storageService.getAllFilesFromDb();
        List<ResponseFile> responseFilesList = new ArrayList<>();
        for (File file : files
        ) {
            String fileDownload = ServletUriComponentsBuilder.fromCurrentContextPath().path("/files/").path(file.getId()+"").toUriString();
            ResponseFile responseFile = new ResponseFile();
            responseFile.setId(file.getId());
            responseFile.setUrl(fileDownload);
            responseFile.setName(file.getName());
            responseFile.setSize(file.getData().length);
            responseFile.setType(file.getType());
            responseFilesList.add(responseFile);
        }

        model.addAttribute("filesList", responseFilesList);
        return "filesList";
    }

/*    @GetMapping("/search")
    public String getFilesByNameContaining(@RequestParam(name = "fileName") String fileName, Model model) {
        List<File> files = storageService.getFilesByNameContaining(fileName);
        List<ResponseFile> responseFilesList = new ArrayList<>();
        for (File file : files
        ) {
            String fileDownload = ServletUriComponentsBuilder.fromCurrentContextPath().path("/files/").path(file.getId()).toUriString();
            ResponseFile responseFile = new ResponseFile();
            responseFile.setId(file.getId());
            responseFile.setUrl(fileDownload);
            responseFile.setName(file.getName());
            responseFile.setSize(file.getData().length);
            responseFile.setType(file.getType());
            responseFilesList.add(responseFile);
        }

        model.addAttribute("filesList", responseFilesList);
        return "filesList";
    }*/

/*    @GetMapping("/file/delete/{id}")
    public String deleteFile(@PathVariable(name = "id") String fileId) {
        storageService.deleteFile(fileId);
        return "redirect:/files";
    }*/

}