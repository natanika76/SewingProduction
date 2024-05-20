package ru.vilas.sewing.controller.admin;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

@Controller
public class DumpDownloadController {

    private static final String BACKUP_FOLDER = "/var/backups/db/";

    @GetMapping("/download-latest-backup")
    public ResponseEntity<Resource> downloadLatestBackup() {
        try {
            Path backupFolderPath = Paths.get(BACKUP_FOLDER);
            File latestFile = Files.walk(backupFolderPath)
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .max(Comparator.comparingLong(File::lastModified))
                    .orElse(null);

            if (latestFile == null) {
                return ResponseEntity.notFound().build();
            }

            Resource fileResource = new FileSystemResource(latestFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + latestFile.getName() + "\"")
                    .body(fileResource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}


